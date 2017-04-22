package zw.co.econet.smsgateway.smpp;

import java.util.Optional;

/**
 * Created by oswin on 1/8/16.
 */
public interface SmppService {

    void createSmppSession();

    Optional<String> sendSms(String destinationNumber, String sourceNumber, String message);

}
