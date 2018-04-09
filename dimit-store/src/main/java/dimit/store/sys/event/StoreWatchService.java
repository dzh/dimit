package dimit.store.sys.event;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.sys.DimitStoreSystem;

/**
 * TODO add name
 * 
 * @author dzh
 * @date Apr 3, 2018 6:46:24 PM
 * @version 0.0.1
 */
public class StoreWatchService implements WatchService {

    static final Logger LOG = LoggerFactory.getLogger(StoreWatchService.class);

    private DimitStoreSystem dss;

    private ConcurrentMap<StoreWatchKey, List<StoreWatcher>> watchKeys = new ConcurrentHashMap<>();

    private BlockingQueue<StoreWatchKey> keys;

    private volatile boolean closed = false;

    public StoreWatchService(DimitStoreSystem dss) {
        this.dss = dss;
        this.keys = new LinkedBlockingQueue<>(10000); // TODO
    }

    public DimitStoreSystem getStoreSystem() {
        return dss;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchService#close()
     */
    @Override
    public void close() throws IOException {
        for (StoreWatchKey w : watchKeys.keySet()) {
            w.cancel();
        }
        watchKeys.clear();
        closed = true;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchService#poll()
     */
    @Override
    public WatchKey poll() {
        if (closed && keys.isEmpty()) throw new ClosedWatchServiceException();
        return keys.poll();
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchService#poll(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (closed && keys.isEmpty()) throw new ClosedWatchServiceException();
        return keys.poll(timeout, unit);
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchService#take()
     */
    @Override
    public WatchKey take() throws InterruptedException {
        if (closed && keys.isEmpty()) throw new ClosedWatchServiceException();
        return keys.take();
    }

    public boolean putKey(StoreWatchKey key) {
        if (closed) return false;

        try {
            int i = 0;
            while (!keys.offer(key)) { // TODO
                Thread.sleep(1000L);
                if (++i > 3) throw new IllegalStateException("putKey to retry more than 3s");
            }
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean newStoreWatcher(StoreWatchKey key) {
        List<StoreWatcher> watchers = watchKeys.putIfAbsent(key, Collections.<StoreWatcher> emptyList());
        if (watchers != null) {
            // TODO add others kinds and return true
            return false;
        }

        watchers = dss.newStoreWatcher(key);
        watchKeys.put(key, watchers);
        return true;
    }

    public void closeStoreWatcher(StoreWatchKey key) {
        List<StoreWatcher> watchers = watchKeys.remove(key);
        if (watchers != null) {
            for (StoreWatcher w : watchers) {
                try {
                    w.close();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

}
