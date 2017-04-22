package zw.co.econet.smsgateway.smpp.jsmpp;

import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import zw.co.econet.smsgateway.amqp.AmqpSink;
import zw.co.econet.smsgateway.json.JsonUtil;
import zw.co.econet.smsgateway.smpp.SessionMessage;


import java.util.Date;

@Service
@Scope("prototype")
@Profile("jsmpp")
@Slf4j
public class JsmppSmsReceiver implements MessageReceiverListener {
    private final AmqpSink amqpSink;
    private final JsonUtil jsonUtil;

    @Autowired
    public JsmppSmsReceiver(AmqpSink amqpSink, JsonUtil jsonUtil) {
        this.amqpSink = amqpSink;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
            try {
                DeliveryReceipt deliveryReceipt = deliverSm.getShortMessageAsDeliveryReceipt();
                String messageId = deliveryReceipt.getId();
                log.info("Delivery receipt for message '" + messageId + "' from " + deliverSm.getSourceAddr() + " to " + deliverSm.getDestAddress() + " : " + deliveryReceipt);
                // update message deliver state
            } catch (InvalidDeliveryReceiptException ex) {
                log.error("Failed to get delivery report ", ex);
            }
        } else {

            SessionMessage sessionMessage = new SessionMessage();
            sessionMessage.setDestinationNumber(deliverSm.getDestAddress());
            sessionMessage.setSourceNumber(deliverSm.getSourceAddr());
            sessionMessage.setStartTime(new Date());
            sessionMessage.setChannel("SMS");
            sessionMessage.setCurrentMessage(new String(deliverSm.getShortMessage()));
            amqpSink.sendMessage("", "mobile-originated", jsonUtil.convertToJson(sessionMessage));
        }
    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {
        throw new UnsupportedOperationException("Method implementation not yet relevant");
    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
        return null;
    }
}
