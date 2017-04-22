package zw.co.econet.smsgateway.persistence.services;


import zw.co.econet.smsgateway.persistence.model.SmsApplication;

/**
 * Created by oswin on 14/12/2016.
 */
public interface SmsApplicationService {
    SmsApplication findById(String id);
    void save(SmsApplication smsApplication);
    SmsApplication findByDestinationCode(String serviceCode);
    SmsApplication findByDestinationCode(String serviceCode, String username);

}
