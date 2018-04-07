package dimit.store.sys;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import dimit.store.sys.event.StoreWatchKey;
import dimit.store.sys.event.StoreWatcher;

/**
 * 
 * <pre>
 * <em>Store Structure:</em>
 * &#47;root
 *     domain
 *         conf                             // dimit configuration
 *             DimitConf                    // 
 *                 ChannelGroupConf
 *                     ChannelConf          // 
 *                         ChannelRecvStat  // 
 *         store                            // runtime store
 *             did         
 *                 Dimit   
 *                     ChannelGroup // 
 *                         Channel  // 
 *                            ChannelTotalStat 
 *                            ChannelSendStat
 * </pre>
 * 
 * @author dzh
 * @date Mar 30, 2018 3:32:22 PM
 * @version 0.0.1
 */
public class LocalStoreSystem extends DimitStoreSystem {

    public LocalStoreSystem(DimitStoreSystemProvider provider, Map<String, Object> env) {
        super(provider, env);
    }

    @Override
    protected boolean connect(URI uri) {
        return true;
    }

    @Override
    public byte[] read(DimitPath path) throws IOException {
        return null;
    }

    @Override
    public DimitPath write(DimitPath path, byte[] data, StoreAttribute<?>... attributes) throws IOException {
        return null;
    }

    @Override
    public List<StoreWatcher> newStoreWatcher(StoreWatchKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> children(DimitPath dimitPath) throws IOException {
        return null;
    }

}
