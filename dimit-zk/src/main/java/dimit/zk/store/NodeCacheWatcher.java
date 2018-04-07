package dimit.zk.store;

import java.io.IOException;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.event.StoreEventKind;
import dimit.store.sys.event.StoreWatchEvent;
import dimit.store.sys.event.StoreWatchKey;
import dimit.store.sys.event.StoreWatcher;

/**
 * @author dzh
 * @date Apr 4, 2018 2:30:32 AM
 * @version 0.0.1
 */
public class NodeCacheWatcher extends StoreWatcher {

    static Logger LOG = LoggerFactory.getLogger(NodeCacheWatcher.class);
    private NodeCache cache;

    public NodeCacheWatcher(DimitStoreSystem dss, StoreWatchKey key) {
        super(dss, key);
        add(StoreEventKind.UPDATE);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        ZkStoreSystem zss = (ZkStoreSystem) getStoreSystem();
        final StoreWatchKey key = getWatchKey();
        try {
            cache = new NodeCache(zss.zkCli(), key.getPath().toAbsolutePath().getPath());
            cache.start();
            cache.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    LOG.info("nodeChanged {}", cache.getCurrentData());

                    StoreWatchEvent event = new StoreWatchEvent(StoreEventKind.UPDATE, key.getPath(), 1);
                    key.putEvent(event);
                }
            });
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (cache != null) cache.close();
    }

}
