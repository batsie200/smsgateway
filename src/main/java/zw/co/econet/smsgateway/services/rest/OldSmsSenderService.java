package zw.co.econet.smsgateway.services.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import zw.co.econet.smsgateway.amqp.AmqpSink;
import zw.co.econet.smsgateway.json.JsonUtil;
import zw.co.econet.smsgateway.persistence.model.Sms;

/**
 * Created by oswin on 3/7/17.
 * this class and service should be terminated in favour of the more informativer version
 */
@Slf4j
@Deprecated
@RestController
@RequestMapping("/smsgateway")
public class OldSmsSenderService {
    private final AmqpSink amqpSink;
    private final JsonUtil jsonUtil;

    @Autowired
    public OldSmsSenderService(AmqpSink amqpSink, JsonUtil jsonUtil) {
        this.amqpSink = amqpSink;
        this.jsonUtil = jsonUtil;
    }

    @RequestMapping(
            value = {"/rest/sms/send/", "/rest/sms/send"},
            produces = {"application/json", "application/xml"},
            method = {RequestMethod.POST},
            consumes = {"application/json", "application/xml"}
    )
    public ResponseEntity<Sms> sendSms(@RequestBody Sms sms) {
        if(sms.getDestinationNumber() != null && !sms.getDestinationNumber().isEmpty()) {
            amqpSink.sendTextMessage("", "mobile.terminated", jsonUtil.convertToJson(sms));
            log.info("Sending message to queue Source {} | destination {} ", sms.getSourceAddress(), sms.getDestinationNumber());
            return new ResponseEntity<>(sms, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(sms, HttpStatus.BAD_REQUEST);
        }
    }
}
