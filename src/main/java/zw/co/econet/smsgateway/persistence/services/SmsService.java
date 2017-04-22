package zw.co.econet.smsgateway.persistence.services;


import zw.co.econet.smsgateway.persistence.model.Sms;
import zw.co.econet.smsgateway.util.MessageState;

import java.util.List;

/**
 * Created by oswin on 8/12/2016.
 */
public interface SmsService {
    Sms findByServerReference(String serverReference);

    Sms findbySubmitId(String submitId);

    Sms findByServerReferenceAndSender(String submitId, String sender);

    List<Sms> findApplicationMessagesByState(MessageState state, String sender, int resultsSize);

    void save(Sms sms);

    void save(List<Sms> sms);

}
