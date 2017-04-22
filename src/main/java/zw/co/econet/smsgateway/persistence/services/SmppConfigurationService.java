package zw.co.econet.smsgateway.persistence.services;


import zw.co.econet.smsgateway.persistence.model.SmppConfiguration;

import java.util.List;


public interface SmppConfigurationService {
    List<SmppConfiguration> findAll();

    void save(SmppConfiguration smppConfiguration);

    List<SmppConfiguration> findByState(boolean state);
}
