package zw.co.econet.smsgateway.services.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zw.co.econet.smsgateway.amqp.AmqpSink;
import zw.co.econet.smsgateway.persistence.model.Sms;
import zw.co.econet.smsgateway.persistence.model.SmsApplication;
import zw.co.econet.smsgateway.persistence.services.SmsApplicationService;
import zw.co.econet.smsgateway.persistence.services.SmsService;
import zw.co.econet.smsgateway.services.rest.exceptions.MissingDestinationNumberException;
import zw.co.econet.smsgateway.services.rest.exceptions.NoPendingMessagesException;
import zw.co.econet.smsgateway.services.rest.exceptions.SmsApplicationUndefinedException;
import zw.co.econet.smsgateway.services.rest.exceptions.SmsNotFoundException;

import javax.validation.Valid;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static zw.co.econet.smsgateway.util.MessageState.COLLECTED;
import static zw.co.econet.smsgateway.util.MessageState.PENDING;
import static zw.co.econet.smsgateway.util.ReferenceGenerator.generateReference;
import static zw.co.econet.smsgateway.util.DateUtil.*;
//import org.springframework.security.core.Authentication;


@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsRestService {

    private final AmqpSink amqpSink;
    private final SmsService smsService;
    private final SmsApplicationService smsApplicationService;
    @Value("${mobile.terminated.queue}")
    private String MOBILE_TERMINATED_QUEUE;
    @Value("${server.port}")
    private int serverPort;


    public SmsRestService(AmqpSink amqpSink, SmsService smsService, SmsApplicationService smsApplicationService) {
        this.amqpSink = amqpSink;
        this.smsService = smsService;
        this.smsApplicationService = smsApplicationService;
    }

    /**
     * sends a message to a single or multiple numbers
     *
     * @param sender
     * @param outboundRequest
     * @return
     */
    @PostMapping(value = "/outbound/{sender}/requests", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<SmsResponse> sendSms(@PathVariable("sender") String sender, @Valid @RequestBody OutboundRequest outboundRequest) {
        log.info("Outbound sms request from {} -> {}", sender, "savings-club");
        if (outboundRequest != null && outboundRequest.getTo() != null && !outboundRequest.getTo().isEmpty()) {
            log.info("Found {} messages to process", outboundRequest.getTo().size());
            List<OutboundResponse> outboundResponses = new ArrayList<>();
            for (String destinationNumber : outboundRequest.getTo()) {
                Sms sms = new Sms();
                sms.setMessage(outboundRequest.getMessage());
                sms.setDestinationNumber(destinationNumber);
                sms.setSourceAddress(outboundRequest.getFrom());
                sms.setServerReference(generateReference());
                sms.setCollectionState(COLLECTED);
                sms.setSender(sender);
                sms.setTimeReceived(LocalDateTime.now());
                amqpSink.sendMessage("", MOBILE_TERMINATED_QUEUE, sms);
                OutboundResponse response = new OutboundResponse();
                response.setMessageId(sms.getServerReference());
                response.setState("RECEIVED");
                try {
                    response.setResourceReference("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort + "/outbound/" + sender + "/requests/" + sms.getServerReference() + "/deliveryInfos");
                } catch (UnknownHostException e) {
                    log.error("Failed to extract IP address >>> ", e);
                }
                outboundResponses.add(response);
            }
            SmsResponse smsResponse = new SmsResponse();
            smsResponse.setOutboundResponses(outboundResponses);
            return new ResponseEntity<>(smsResponse, HttpStatus.ACCEPTED);
        }
        throw new MissingDestinationNumberException("No destination number defined for the request");
    }


    /**
     * used to check the status of a single message to be delivered
     *
     * @param senderAddress
     * @param requestId
     * @return
     */
    @GetMapping(value = "/outbound/{senderAddress}/requests/{requestId}/deliveryInfos")
    public ResponseEntity<InboundRequest> smsQuery(@PathVariable("senderAddress") String senderAddress, @PathVariable("requestId") String requestId) {
        log.info("Searching for sms with request id {}", requestId);
        Sms sms = smsService.findByServerReferenceAndSender(requestId, senderAddress);
        if (sms != null) {
            InboundRequest inboundRequest = new InboundRequest();
            inboundRequest.setFrom(sms.getSourceAddress());
            inboundRequest.setMessage(sms.getMessage());
            inboundRequest.setTo(sms.getDestinationNumber());
            inboundRequest.setTimeReceived(localDateTimeToDate(sms.getTimeReceived()));
            inboundRequest.setState(sms.getDeliveryState().toString());
            inboundRequest.setTimeDelivered(localDateTimeToDate(sms.getDeliveryTime()));
            return new ResponseEntity<>(inboundRequest, HttpStatus.OK);
        }
        throw new SmsNotFoundException("No sms with reference " + requestId + " has been found");
    }

    /**
     * used to search for the pending messages for a registered consumer
     *
     * @param registrationId - this is the client key given to each customer application
     * @param maxBatchSize
     *
     * @return
     */
    @GetMapping(value = "/inbound/registrations/{registrationId}/messages", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<SmsResponse> fetchSmsBatch(@PathVariable("registrationId") String registrationId, @RequestParam(value = "maxBatchSize", defaultValue = "10") int maxBatchSize) {
        log.info("Searching for pending messages for {} with registration id {}. Batch size : {}", "savings-club", registrationId, maxBatchSize);
        SmsApplication smsApplication = smsApplicationService.findById(registrationId);
        if (smsApplication == null)
            throw new SmsApplicationUndefinedException("The supplied application registration key is not defined");
        final List<Sms> pendingSmsList = smsService.findApplicationMessagesByState(PENDING, smsApplication.getDestinationCode(), maxBatchSize);
        if (pendingSmsList.isEmpty()) {
            List<InboundRequest> requests = new ArrayList<>();
            for (Sms sms : pendingSmsList) {
                InboundRequest inboundRequest = new InboundRequest();
                inboundRequest.setCollectionState(COLLECTED);
                inboundRequest.setFrom(sms.getSourceAddress());
                inboundRequest.setMessage(sms.getMessage());
                inboundRequest.setTo(sms.getDestinationNumber());
                inboundRequest.setTimeReceived(localDateTimeToDate(sms.getTimeReceived()));
                sms.setCollectionState(COLLECTED);
                sms.setTimeRouted(LocalDateTime.now());
                requests.add(inboundRequest);
            }
            SmsResponse smsResponse = new SmsResponse();
            smsResponse.setInboundRequests(requests);
            smsService.save(pendingSmsList);
            return new ResponseEntity<>(smsResponse, HttpStatus.OK);
        }
        throw new NoPendingMessagesException("There are currently no pending messages for your service");
    }
}
