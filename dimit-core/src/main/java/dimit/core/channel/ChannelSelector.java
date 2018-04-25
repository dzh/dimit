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
 * List&lt;ChannelWrapper&gt; channels = selector.select(...); // tags
 * V result = null;
 * for(ChannelWrapper channel : channels) {
 *     try{
 *         result = channel.call(new Callable&lt;V&gt;{
 *             // TODO request
 *             
 *         });
 *         //TODO parse result 
 *         
 *         break;
 *     }catch(RateLimiterException e){
 *         LOG.info(e.getMessage(), e);
 *     }catch(InvalidChannelException e){
 *         LOG.error(e.getMessage(), e);
 *     }catch(Exception e){ // or break
 *         LOG.error(e.getMessage(), e);
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
