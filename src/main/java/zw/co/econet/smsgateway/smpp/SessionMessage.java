package zw.co.econet.smsgateway.smpp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class SessionMessage implements Serializable {

    private String sourceNumber;
    private String destinationNumber;
    private String sessionId;
    private String channel;
    private String currentMessage;
    private Date startTime;

  }
