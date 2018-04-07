package dimit.store.sys.event;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dimit.store.sys.DimitStoreSystem;

/**
 * @author dzh
 * @date Apr 3, 2018 8:25:15 PM
 * @version 0.0.1
 */
public abstract class StoreWatcher implements Closeable, Runnable {

    private Set<StoreEventKind> kinds;

    private StoreWatchKey key;

    private DimitStoreSystem dss;

    public StoreWatcher(DimitStoreSystem dss, StoreWatchKey key) {
        this.key = key;
        this.dss = dss;
        kinds = Collections.<StoreEventKind> synchronizedSet(new HashSet<StoreEventKind>());
    }

    public DimitStoreSystem getStoreSystem() {
        return dss;
    }

    public StoreWatchKey getWatchKey() {
        return key;
    }

    public Collection<StoreEventKind> kind() {
        return kinds;
    }

    public boolean contain(StoreEventKind kind) {
        return kinds.contains(kind);
    }

    public boolean add(StoreEventKind kind) {
        return kinds.add(kind);
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        kinds.clear();
    }

}
