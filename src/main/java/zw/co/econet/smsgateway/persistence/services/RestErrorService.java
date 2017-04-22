package zw.co.econet.smsgateway.persistence.services;


import zw.co.econet.smsgateway.persistence.model.RestError;

/**
 * Created by oswin on 12/12/2016.
 */
public interface RestErrorService {
    RestError findByExceptionClass(String errorClass);
    void save(RestError restError);
}
