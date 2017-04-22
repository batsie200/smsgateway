package zw.co.econet.smsgateway.services.rest.exceptions;

/**
 * Created by oswin on 9/12/2016.
 */
public class MissingDestinationNumberException extends RestException {
    public MissingDestinationNumberException(String message) {
        super(message);
    }
}
