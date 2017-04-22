package zw.co.econet.smsgateway.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zw.co.econet.smsgateway.persistence.model.RestError;


public interface RestErrorRepository extends JpaRepository<RestError, String> {
    RestError findByExceptionClass(String errorClass);
}
