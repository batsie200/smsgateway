package zw.co.econet.smsgateway.persistence.services.implementation;

import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zw.co.econet.smsgateway.persistence.model.RestError;
import zw.co.econet.smsgateway.persistence.repositories.RestErrorRepository;
import zw.co.econet.smsgateway.persistence.services.RestErrorService;


@Slf4j
@Transactional
@Repository
public class RestErrorServiceImpl implements RestErrorService {

    private final RestErrorRepository repository;

    @Autowired
    public RestErrorServiceImpl(RestErrorRepository repository) {
        this.repository = repository;
    }

    public void save(RestError restError) {
        repository.save(restError);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "exception_error_cache", key = "#root.method.name.concat('.').concat(#errorClass)")
    public RestError findByExceptionClass(String errorClass) {
        return repository.findByExceptionClass(errorClass);
    }
}
