package zw.co.econet.smsgateway.util;

import java.util.UUID;

/**
 * Created by oswin on 9/12/2016.
 */
public class ReferenceGenerator {

    /**
     * for lack of a better method returing a UUID.
     * TODO: look for a better means of generating a unique reference
     *
     * @return
     */
    public static String generateReference() {
        return UUID.randomUUID().toString();
    }
}
