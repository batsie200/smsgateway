package zw.co.econet.smsgateway.amqp;

import lombok.Data;

/**
 * Created by oswin on 3/14/17.
 */
@Data
public class OldSms {
    private String id;
    private int version;
    private String sourceAddress;
    private String destinationNumber;
    private String notificationUrl;
    private String submitId;
    private String status;
    private String timeReceived;
    private String timeRouted;
    private String deliveryTime;
    private String message;
    private String sender;
    private String clientCorrelator;
}
