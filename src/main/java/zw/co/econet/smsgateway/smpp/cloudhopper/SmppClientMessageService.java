package zw.co.econet.smsgateway.smpp.cloudhopper;



import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduResponse;

@FunctionalInterface
public interface SmppClientMessageService {

    PduResponse received(OutboundClient client, DeliverSm deliverSm);
}
