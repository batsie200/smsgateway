package zw.co.econet.smsgateway.util;

import com.cloudhopper.smpp.SmppConstants;

/**
 * Created by oswin on 12/4/2017.
 */
public class MessageStateUtil {

    public static MessageState getState(byte state){
        switch (state) {
            case SmppConstants.STATE_DELIVERED:
                return MessageState.DELIVERED;
            case SmppConstants.STATE_EXPIRED:
                return MessageState.EXPIRED;
            case SmppConstants.STATE_DELETED:
                return MessageState.DELETED;
            case SmppConstants.STATE_UNDELIVERABLE:
                return MessageState.UNDELIVERABLE;
            case SmppConstants.STATE_ACCEPTED:
                return MessageState.ACCEPTED;
            case SmppConstants.STATE_UNKNOWN:
                return MessageState.UNKNOWN;
            case SmppConstants.STATE_REJECTED:
                return MessageState.REJECTED;
            case SmppConstants.STATE_ENROUTE:
                return MessageState.ENROUTE;
            default:
                return MessageState.UNKNOWN;
        }

    }
}
