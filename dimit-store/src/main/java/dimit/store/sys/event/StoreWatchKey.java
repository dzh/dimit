package dimit.store.sys.event;

import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.sys.DimitPath;

/**
 * @author dzh
 * @date Apr 3, 2018 6:41:27 PM
 * @version 0.0.1
 */
public class StoreWatchKey implements WatchKey {

    static Logger LOG = LoggerFactory.getLogger(StoreWatchKey.class);

    private DimitPath path;

    private AtomicBoolean valid;

    static int READY = 0;
    static int SIGNALLED = 1;

    private AtomicInteger status; // 0-ready 1-signalled

    private BlockingQueue<WatchEvent<?>> events;

    private Kind<?>[] kinds;

    private StoreWatchService service;

    private Object attachment;

    public StoreWatchKey(StoreWatchService service, DimitPath path, Object attachment, Kind<?>... kinds) {
        this.service = service;
        this.path = path;
        this.attachment = attachment;
        this.kinds = kinds;

        this.valid = new AtomicBoolean(true);
        this.status = new AtomicInteger(READY);

        this.events = new LinkedBlockingQueue<>(100000); // TODO
    }

    public DimitPath getPath() {
        return path;
    }

    public Kind<?>[] getKinds() {
        return kinds;
    }

    public StoreWatchService getService() {
        return service;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchKey#isValid()
     */
    @Override
    public boolean isValid() {
        return valid.get();
    }

    // TODO merge events
    public boolean putEvent(WatchEvent<?> event) {
        if (!isValid()) return false;

        try {
            int i = 0;
            while (!events.offer(event)) {
                WatchEvent<?> e = events.poll();
                LOG.error("discard {}", e.toString());
                // TODO
                if (++i > 10) throw new IllegalStateException("putEvent to retry more than 10 times");
            }

            if (status.compareAndSet(READY, SIGNALLED)) {
                service.putKey(this);
            }

            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchKey#pollEvents()
     */
    @Override
    public List<WatchEvent<?>> pollEvents() {
        if (status.get() == READY) return Collections.emptyList();

        int size = events.size();
        if (size == 0) return Collections.emptyList();

        List<WatchEvent<?>> list = new LinkedList<>();
        events.drainTo(list, size);
        return list;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchKey#reset()
     */
    @Override
    public boolean reset() {
        if (!isValid()) return false;

        int size = events.size();
        if (size > 0) {
            service.putKey(this);
        } else {
            status.compareAndSet(SIGNALLED, READY);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchKey#cancel()
     */
    @Override
    public void cancel() {
        // close StoreWatcher
        service.closeStoreWatcher(this);

        valid.set(false);
    }

    /*
     * (non-Javadoc)
     * @see java.nio.file.WatchKey#watchable()
     */
    @Override
    public DimitPath watchable() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof StoreWatchKey) {
            StoreWatchKey key = (StoreWatchKey) obj;
            return key.watchable().toAbsolutePath().getPath().equals(watchable().toAbsolutePath().getPath());
        }

        return false;
    }

    @Override
    public String toString() {
        return watchable().toString() + "_" + valid.get();
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

}
