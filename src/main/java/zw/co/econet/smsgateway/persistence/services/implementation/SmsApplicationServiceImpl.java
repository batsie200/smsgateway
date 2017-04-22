package zw.co.econet.smsgateway.persistence.services.implementation;

import groovy.util.logging.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.init.RepositoriesPopulatedEvent;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zw.co.econet.smsgateway.persistence.model.SmsApplication;
import zw.co.econet.smsgateway.persistence.repositories.SmsApplicationRepository;
import zw.co.econet.smsgateway.persistence.services.SmsApplicationService;

@Slf4j
@Transactional
@Repository
public class SmsApplicationServiceImpl implements SmsApplicationService {
    private final SmsApplicationRepository repository;

    public SmsApplicationServiceImpl(SmsApplicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public SmsApplication findById(String id) {
        return repository.findOne(id);
    }

    @Override
    public void save(SmsApplication smsApplication) {
        repository.save(smsApplication);
    }

    @Override
    @Cacheable(cacheNames = "app_code_cache", key = "#root.method.name.concat('.').concat(#serviceCode)")
    public SmsApplication findByDestinationCode(String serviceCode) {
        return repository.findByDestinationCode(serviceCode);
    }

    @Override
    @Cacheable(cacheNames = "application_cache", key = "#root.method.name.concat('.').concat(#serviceCode).concat(#username)")
    public SmsApplication findByDestinationCode(String serviceCode, String username) {
        return repository.findByDestinationCodeAndUsername(serviceCode, username);
    }
}
