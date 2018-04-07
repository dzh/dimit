package dimit.core.channel;

/**
 * @author dzh
 * @date Apr 7, 2018 4:09:25 PM
 * @version 0.0.1
 */
public class RateLimiterException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RateLimiterException(String message) {
        super(message);
    }

}
