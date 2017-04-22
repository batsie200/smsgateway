package zw.co.econet.smsgateway.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.econet.smsgateway.persistence.model.SmppConfiguration;

import java.util.List;


public interface SmppConfigurationRepository extends JpaRepository<SmppConfiguration, String> {
    List<SmppConfiguration> findByState(boolean state);
}
