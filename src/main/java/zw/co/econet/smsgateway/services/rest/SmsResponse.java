package zw.co.econet.smsgateway.services.rest;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement
public class SmsResponse {
    private List<OutboundResponse> outboundResponses;
    private List<InboundRequest> inboundRequests;
}
