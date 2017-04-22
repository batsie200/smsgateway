package zw.co.econet.smsgateway.smpp.cloudhopper;


import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.cloudhopper.smpp.util.DeliveryReceiptException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import zw.co.econet.smsgateway.amqp.AmqpSink;
import zw.co.econet.smsgateway.json.JsonUtil;
import zw.co.econet.smsgateway.persistence.model.Sms;
import zw.co.econet.smsgateway.smpp.SessionMessage;


import java.time.LocalDateTime;
import java.util.Date;

import static zw.co.econet.smsgateway.util.ReferenceGenerator.generateReference;

@Slf4j
@Service
@Profile("cloudhopper")
public class SmppClientMessageServiceBean implements SmppClientMessageService {
    private final AmqpSink amqpSink;
    private final JsonUtil jsonUtil;

    @Autowired
    public SmppClientMessageServiceBean(JsonUtil jsonUtil, AmqpSink amqpSink) {
        this.jsonUtil = jsonUtil;
        this.amqpSink = amqpSink;
    }

    /**
     * delivery receipt, or MO
     */
    @Override
    public PduResponse received(OutboundClient client, DeliverSm deliverSm) {
        log.info("Received delivery {}", deliverSm);
        String shortMessage = new String(deliverSm.getShortMessage());
        if (isDeliveryReceipt(shortMessage)) {
            try {
                DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(shortMessage, DateTimeZone.getDefault(), false);
                amqpSink.sendMessage("", "delivery.receipts", deliveryReceipt.toShortMessage());
            } catch (DeliveryReceiptException e) {
                log.error("Failed to extract delivery message: ", e);

            }
        } else {
            Sms  sms = new Sms();
            sms.setDestinationNumber(deliverSm.getDestAddress().getAddress());
            sms.setSourceAddress(deliverSm.getSourceAddress().getAddress());
            sms.setMessage(shortMessage);
            sms.setTimeReceived(LocalDateTime.now());
            sms.setServerReference(generateReference());
            amqpSink.sendMessage("", "mobile.originated", sms);
        }
        return deliverSm.createResponse();
    }

    public boolean isDeliveryReceipt(String shortMessage) {
        try {
            DeliveryReceipt deliveryReceipt = DeliveryReceipt.parseShortMessage(shortMessage, DateTimeZone.getDefault(), false);
            return (deliveryReceipt.getMessageId() != null);
        } catch (DeliveryReceiptException e) {
            log.error("Failed to extract delivery message: ", e);
            return false;
        }

    }
}
