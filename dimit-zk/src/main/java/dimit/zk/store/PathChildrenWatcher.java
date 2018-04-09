package dimit.zk.store;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.event.StoreEventKind;
import dimit.store.sys.event.StoreWatchEvent;
import dimit.store.sys.event.StoreWatchKey;
import dimit.store.sys.event.StoreWatcher;

/**
 * @author dzh
 * @date Apr 4, 2018 2:32:12 AM
 * @version 0.0.1
 */
public class PathChildrenWatcher extends StoreWatcher {

    static Logger LOG = LoggerFactory.getLogger(NodeCacheWatcher.class);

    private PathChildrenCache cache;

    public PathChildrenWatcher(DimitStoreSystem dss, StoreWatchKey key) {
        super(dss, key);
        add(StoreEventKind.CHILDREN, StoreEventKind.CHILD_ADD, StoreEventKind.CHILD_DELETE, StoreEventKind.CHILD_UPDATE);
    }

    @Override
    public void run() {
        ZkStoreSystem zss = (ZkStoreSystem) getStoreSystem();
        final StoreWatchKey key = getWatchKey();
        try {
            cache = new PathChildrenCache(zss.zkCli(), key.getPath().toAbsolutePath().getPath(), false);
            cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    ChildData child = event.getData();
                    LOG.info("childEvent {}", event);
                    Type t = event.getType();

                    StoreWatchEvent e = null;
                    if (t == Type.CHILD_ADDED) { // TODO
                        e = new StoreWatchEvent(StoreEventKind.CHILD_ADD, key.getPath().newPath(child.getPath()), 1);
                    } else if (t == Type.CHILD_REMOVED) {
                        e = new StoreWatchEvent(StoreEventKind.CHILD_DELETE, key.getPath().newPath(child.getPath()), 1);
                    } else if (t == Type.CHILD_UPDATED) {
                        e = new StoreWatchEvent(StoreEventKind.CHILD_UPDATE, key.getPath().newPath(child.getPath()), 1);
                    }

                    // TODO filter event !PathChildrenWatcher.this.getWatchKey().getKinds()
                    if (e == null) {
                        LOG.warn("discard {}", event);
                        return;
                    }

                    key.putEvent(e);
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
