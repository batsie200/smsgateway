package zw.co.econet.smsgateway.services.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import zw.co.econet.smsgateway.util.MessageState;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Request to get messages from server to application
 */
@Data
@XmlRootElement
public class InboundRequest {
    private String messageId;
    private String from;
    private String to;
    private String message;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date timeReceived;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date timeDelivered;
    private String state;
    private MessageState collectionState;

}