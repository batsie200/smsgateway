package zw.co.econet.smsgateway.persistence.services.implementation;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zw.co.econet.smsgateway.persistence.model.SmppConfiguration;
import zw.co.econet.smsgateway.persistence.repositories.SmppConfigurationRepository;
import zw.co.econet.smsgateway.persistence.services.SmppConfigurationService;

import java.util.List;


@Repository
@Transactional
public class SmppConfigurationServiceImpl implements SmppConfigurationService {
    private final SmppConfigurationRepository repository;

    @Autowired
    public SmppConfigurationServiceImpl(SmppConfigurationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<SmppConfiguration> findAll() {
        return repository.findAll();
    }

    @Override
    public void save(SmppConfiguration smppConfiguration) {
        repository.save(smppConfiguration);
    }

    public List<SmppConfiguration> findByState(boolean state) {
        return repository.findByState(state);
    }
}
