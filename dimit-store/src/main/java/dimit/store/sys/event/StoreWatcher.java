package dimit.store.sys.event;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.WatchEvent.Kind;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import dimit.store.sys.DimitStoreSystem;

/**
 * @author dzh
 * @date Apr 3, 2018 8:25:15 PM
 * @version 0.0.1
 */
public abstract class StoreWatcher implements Closeable, Runnable {

    private Collection<Kind<?>> kinds;

    private StoreWatchKey key;

    private DimitStoreSystem dss;

    public StoreWatcher(DimitStoreSystem dss, StoreWatchKey key) {
        this.key = key;
        this.dss = dss;
        kinds = Collections.<Kind<?>> synchronizedList(new LinkedList<Kind<?>>());
        // kinds = Collections.<Kind<?>> synchronizedList(Arrays.asList(key.getKinds()));
    }

    public DimitStoreSystem getStoreSystem() {
        return dss;
    }

    public StoreWatchKey getWatchKey() {
        return key;
    }

    public Collection<Kind<?>> kind() {
        return kinds;
    }

    public boolean contain(Kind<?> kind) {
        return kinds.contains(kind);
    }

    public boolean add(Kind<?>... kind) {
        for (Kind<?> k : kind)
            kinds.add(k);
        return true;
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
