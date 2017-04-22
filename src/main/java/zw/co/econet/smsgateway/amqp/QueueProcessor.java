package zw.co.econet.smsgateway.amqp;

import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.cloudhopper.smpp.util.DeliveryReceiptException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import zw.co.econet.smsgateway.json.JsonUtil;
import zw.co.econet.smsgateway.persistence.model.Sms;
import zw.co.econet.smsgateway.persistence.model.SmsApplication;
import zw.co.econet.smsgateway.persistence.services.SmsApplicationService;
import zw.co.econet.smsgateway.persistence.services.SmsService;
import zw.co.econet.smsgateway.services.rest.InboundRequest;
import zw.co.econet.smsgateway.services.rest.OutboundResponse;
import zw.co.econet.smsgateway.smpp.SmppService;
import zw.co.econet.smsgateway.util.DateUtil;
import zw.co.econet.smsgateway.util.MessageStateUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpMethod.POST;
import static zw.co.econet.smsgateway.util.DateUtil.localDateTimeToDate;
import static zw.co.econet.smsgateway.util.MessageState.*;


/**
 * Created by oswin on 26/11/2016.
 */
@Slf4j
@Service
public class QueueProcessor {
    private final SmppService smppService;
    private final SmsService smsService;
    private final RestTemplate restTemplate;
    private final SmsApplicationService smsApplicationService;

    @Autowired
    public QueueProcessor(SmppService smppService, SmsService smsService, JsonUtil jsonUtil, RestTemplate restTemplate, SmsApplicationService smsApplicationService) {
        this.smppService = smppService;
        this.smsService = smsService;
        this.restTemplate = restTemplate;
        this.smsApplicationService = smsApplicationService;
    }

    @RabbitListener(queues = {"mobile.terminated"})
    public void sendSMS(Sms sms) {
        try {
            if (sms != null) {
                sms.setTimeReceived(LocalDateTime.now());
                log.info(">>> Sending sms to {}", sms.getDestinationNumber());
                if (smppService != null) {
                    Optional<String> submitId = smppService.sendSms(sms.getDestinationNumber(), sms.getSourceAddress(), sms.getMessage());
                    if (submitId.isPresent()) {
                        sms.setSubmitId(submitId.get());
                        sms.setDeliveryState(ENROUTE);
                    } else {
                        sms.setDeliveryState(FAILED);
                        sms.setSubmitId(null);
                    }
                    sms.setTimeRouted(LocalDateTime.now());
                    log.info("Saving sms {}", sms);
                    smsService.save(sms);
                }
            }
        } catch (Exception e) {
            log.error("Caught an exception {}", e);
        }
    }

    /**
     * Used to listen to incoming messages
     * incoming messages are routed to a specific endpoint
     * TODO: implement logic to predefine rest endpoint to receive messages. This removes the need to hard code the delivery endpoint
     *
     * @param sms
     */

    @RabbitListener(queues = {"mobile.originated"})
    public void receiveSMS(Sms sms) {
        log.info("Received an incoming message {}", sms);
        SmsApplication smsApplication = smsApplicationService.findByDestinationCode(sms.getDestinationNumber());
        sms.setTimeRouted(LocalDateTime.now());
        if (smsApplication != null && smsApplication.getDestinationCode().equalsIgnoreCase(sms.getDestinationNumber())) {
            InboundRequest inboundRequest = new InboundRequest();
            inboundRequest.setFrom(sms.getSourceAddress());
            inboundRequest.setMessage(sms.getMessage());
            inboundRequest.setTo(sms.getDestinationNumber());
            inboundRequest.setTimeReceived(localDateTimeToDate(sms.getTimeReceived()));
            inboundRequest.setState(ENROUTE.toString());
            final HttpEntity<InboundRequest> request = new HttpEntity<>(inboundRequest, getHttpHeaders());
            ResponseEntity<OutboundResponse> responseEntity = restTemplate.exchange(smsApplication.getNotificationUrl(), POST, request, OutboundResponse.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                OutboundResponse outboundResponse = responseEntity.getBody();
                sms.setDeliveryTime(LocalDateTime.now());
                if ("received".equalsIgnoreCase(outboundResponse.getState().toLowerCase())) {
                    sms.setDeliveryState(DELIVERED);
                    sms.setCollectionState(COLLECTED);
                } else {
                    sms.setDeliveryState(FAILED);
                    sms.setCollectionState(FAILED);
                }
            } else {
                sms.setDeliveryState(UNDELIVERABLE);
                sms.setCollectionState(FAILED);
            }

        } else {
            sms.setDeliveryState(UNDELIVERABLE);
            sms.setCollectionState(FAILED);
        }
        smsService.save(sms);
    }
    private HttpHeaders getHttpHeaders() {
//        final String base64Credentials = new String(Base64.encodeBase64((sureUsername + ":" + surePassword).getBytes()));
       final  HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Basic " + base64Credentials);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.add("username", profileUser);
        return headers;
    }


    /**
     * Used to process delivery receipts for sent messages
     * updates the sms table with the appropriate message
     *
     * @param shortMessage
     */

    @RabbitListener(queues = {"delivery.receipts"})
    public void deliveryReceiptsprocessor(String shortMessage) {
        log.info("Received delivery receipt :: ", shortMessage);
        try {
            DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(shortMessage, DateTimeZone.getDefault(), false);
//            work around for ZTE. it returns the Submitid as long not as hex
            String submitId= Long.toHexString(Long.parseLong(deliveryReceipt.getMessageId())).toUpperCase();
            TimeUnit.SECONDS.sleep(10l);
            Sms sms = smsService.findbySubmitId(submitId);
            if (sms != null && sms.getSubmitId().equalsIgnoreCase(deliveryReceipt.getMessageId())) {
                sms.setDeliveryTime(DateUtil.dateToLocalDateTime(deliveryReceipt.getDoneDate().toDate()));
                sms.setDeliveryState(MessageStateUtil.getState(deliveryReceipt.getState()));
                smsService.save(sms);
                return;
            }
            log.error("No enrty could be found for message with id :: {}", submitId);
        } catch (DeliveryReceiptException e) {
            log.error("Failed to extract delivery receipt from message ", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * sends a customer a notification of the arrival or delivery of an sms
     *
     * @param sms
     */
    @RabbitListener(queues = {"client.notifications"})
    public void cusotmerNotifications(Sms sms) {
// TODO: implement logic to route the message to the client
    }
}
