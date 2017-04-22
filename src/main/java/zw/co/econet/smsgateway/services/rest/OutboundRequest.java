package zw.co.econet.smsgateway.services.rest;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Returns a notification to the sender upon sending a message
 */

@XmlRootElement(name="outboundRequest")
public class OutboundRequest {
    private String from;
    private List<String> to;
    private String message;
    private String notifyUrl;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
}
