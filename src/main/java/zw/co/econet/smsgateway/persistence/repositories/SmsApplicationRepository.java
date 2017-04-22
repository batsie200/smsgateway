package zw.co.econet.smsgateway.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.econet.smsgateway.persistence.model.SmsApplication;

/**
 * Created by oswin on 8/12/2016.
 */
public interface SmsApplicationRepository  extends JpaRepository<SmsApplication, String>{
    SmsApplication findByDestinationCode(String serviceCode);

    SmsApplication findByDestinationCodeAndUsername(String serviceCode, String username);
}
