package zw.co.econet.smsgateway.services.rest;


import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used to define an outgoing sms
 */
@Data
@XmlRootElement
public class OutboundResponse {

    private String messageId;
    private String state;
    private String resourceReference;
}
