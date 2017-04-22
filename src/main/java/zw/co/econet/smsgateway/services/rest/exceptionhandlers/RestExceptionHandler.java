package zw.co.econet.smsgateway.services.rest.exceptionhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import zw.co.econet.smsgateway.persistence.model.RestError;
import zw.co.econet.smsgateway.persistence.services.RestErrorService;
import zw.co.econet.smsgateway.services.rest.exceptions.RestException;

import javax.servlet.ServletException;

/**
 * captures errors as they occur in the
 */
@Slf4j
@RestControllerAdvice(basePackages = {"zw.co.ift.messaging.sms.services.rest"})
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final RestErrorService restErrorService;

    @Autowired
    public RestExceptionHandler(RestErrorService restErrorService) {
        this.restErrorService = restErrorService;
    }

    @ExceptionHandler(value = {RestException.class})
    public ResponseEntity<Object> handleInvalidRestException(RuntimeException ex, WebRequest request) {
        log.error("Caught an exception {}", ex.getClass().getTypeName());
        RestError restError = restErrorService.findByExceptionClass(ex.getClass().getTypeName());
        if (restError == null) {
            log.error("Undefined error caught");
            restError = new RestError();
            restError.setErrorCode("UNKNOWN.001");
            restError.setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE);
            restError.setErrorMessage("Undefined error caught");
        }
        return handleExceptionInternal(ex, restError, new HttpHeaders(), restError.getHttpStatus(), request);
    }


}

