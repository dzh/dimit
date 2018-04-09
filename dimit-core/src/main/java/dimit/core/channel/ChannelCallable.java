package dimit.core.channel;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.RateLimiter;

/**
 * @author dzh
 * @date Apr 7, 2018 3:30:50 PM
 * @version 0.0.1
 */
@Deprecated
public abstract class ChannelCallable<V> implements Callable<V> {

    private ChannelWrapper channel;

    private RateLimiter limiter;

    public void updateLimiter() {
        if (limiter == null) {
            limiter = RateLimiter.create(channel.tps());
        } else {
            limiter.setRate(channel.tps());
        }
    }

    public ChannelWrapper getChannel() {
        return channel;
    }

    public void setChannel(ChannelWrapper channel) {
        this.channel = channel;
    }

    @Override
    public V call() throws Exception {
        if (!limiter.tryAcquire()) { throw new RateLimiterException("out of tps:" + limiter.getRate()); }

        return toCall();
    }

    protected abstract V toCall();
}
