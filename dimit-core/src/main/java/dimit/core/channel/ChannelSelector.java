/**
 * 
 */
package dimit.core.channel;

import java.io.Closeable;
import java.util.List;

import dimit.store.ChannelType;

/**
 * <pre>
 * e.g. how to use {@link ChannelSelector}
 * List<ChannelWrapper> channels = selector.select(...)
 * for(ChannelWrapper channel : channels) {
 *     try{
 *         V result = channel.call();
 *         //TODO parse result
 *         
 *         break;
 *     }catch(RateLimiterException e){
 *         LOG.info(e.getMessage(), e);
 *         continue;  // next channel
 *     }catch(Exception e){
 *         LOG.error(e.getMessage(), e)
 *         continue; // or break
 *     }
 * }
 * 
 * </pre>
 * 
 * @author dzh
 * @date Mar 21, 2018 4:34:07 PM
 * @version 0.0.1
 */
public abstract class ChannelSelector implements Closeable {

    private ChannelGroupWrapper group;

    public ChannelSelector(ChannelGroupWrapper group) {
        this.group = group;
    }

    public List<ChannelWrapper> select(String... tags) {
        return select(ChannelType.SEND, tags);
    }

    abstract List<ChannelWrapper> select(ChannelType type, String... tags);

    public ChannelGroupWrapper group() {
        return this.group;
    }

}
