package dimit.core.channel;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.RateLimiter;

/**
 * @author dzh
 * @date Apr 7, 2018 3:30:50 PM
 * @version 0.0.1
 */
public abstract class ChannelCallable<V> implements Callable<V> {

    private ChannelWrapper<V> channel;

    private RateLimiter limiter;

    public void updateLimiter() {
        if (limiter == null) {
            limiter = RateLimiter.create(channel.getTps());
        } else {
            limiter.setRate(channel.getTps());
        }
    }

    public ChannelWrapper<V> getChannel() {
        return channel;
    }

    public void setChannel(ChannelWrapper<V> channel) {
        this.channel = channel;
        updateLimiter();
    }

    @Override
    public V call() throws Exception {
        if (!limiter.tryAcquire()) { throw new RateLimiterException("out of tps:" + limiter.getRate()); }

        return toCall();
    }

    protected abstract V toCall();
}
