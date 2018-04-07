package dimit.store.sys;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.sys.event.StoreWatchKey;
import dimit.store.sys.event.StoreWatchService;
import dimit.store.sys.event.StoreWatcher;
import dimit.store.sys.io.CacheStoreIO;
import dimit.store.sys.io.StoreIO;

/**
 * 
 * <pre>
 * <em>URI specification</em>
 * scheme://domain/path?param=value
 * 
 * domain: type of {@link DimitStoreSystem}
 * root path: /path/domain
 * 
 * For Example:
 * dimit-zk://domain/path?host=192.168.0.1:2181,192.168.0.2:2181
 * </pre>
 * 
 * <pre>
 * <em>Store Structure:</em>
 * &#47;path
 *     domain
 *         conf                            
 *             DimitConf                 
 *                 ChannelGroupConf
 *                     ChannelConf          // zk watch node // TODO only master then broadcast event through coca
 *         store                            // runtime store
 * </pre>
 * 
 * @author dzh
 * @date Mar 24, 2018 5:57:34 PM
 * @version 0.0.1
 */
public abstract class DimitStoreSystem implements Closeable {

    private static Logger LOG = LoggerFactory.getLogger(DimitStoreSystem.class);

    private final DimitStoreSystemProvider provider;

    private volatile boolean open = false;

    private String domain;
    private DimitPath root; // path/domain
    private StoreIO<?> io;

    public DimitStoreSystem(DimitStoreSystemProvider provider, Map<String, Object> env) {
        this.provider = provider;
    }

    public void open(URI uri) throws IOException {
        try {
            this.domain = uri.getHost();
            this.root = getPath(uri.getPath(), domain);
            open = connect(uri);
            if (open) {
                io = new CacheStoreIO(this);// TODO config
            }
        } finally {
            LOG.info("open:{} uri:{}", open, toString());
        }
    }

    protected abstract boolean connect(URI uri) throws IOException;

    public DimitStoreSystemProvider provider() {
        return this.provider;
    }

    public String getDomain() {
        return domain;
    }

    public DimitPath getRoot() {
        return root;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void close() throws IOException {
        if (!isOpen()) return;

        try {
            if (io != null) io.close();

            dispose();
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            provider.removeSystemCache(this);
            LOG.info("close {}", toString());
        }
    }

    protected void dispose() throws IOException {
        // TODO
    }

    public boolean isOpen() {
        return open;
    }

    /**
     * 
     * @param first
     * @param more
     * @return
     * @throws IllegalArgumentException
     */
    public DimitPath getPath(String first, String... more) {
        if (first == null) return getRoot();

        StringBuilder buf = new StringBuilder();
        buf.append(first);
        for (String m : more) {
            buf.append(provider.pathSeperator());
            buf.append(m);
        }
        DimitPath path = new DimitPath(this, buf.toString());
        return path;
    }

    protected boolean isValidPath(DimitPath path) {
        return path.toAbsolutePath().startsWith(root);
    }

    public WatchService newWatchService() throws IOException {
        return new StoreWatchService(this); // TODO auto close
    }

    @SuppressWarnings("unchecked")
    public <T> StoreIO<T> io() {
        return (StoreIO<T>) this.io;
    }

    public abstract byte[] read(DimitPath path) throws IOException;

    public abstract DimitPath write(DimitPath path, byte[] data, StoreAttribute<?>... attributes) throws IOException;

    public abstract List<StoreWatcher> newStoreWatcher(StoreWatchKey key);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof DimitStoreSystem) {
            if (((DimitStoreSystem) obj).getRoot().equals(getRoot()) && ((DimitStoreSystem) obj).provider().equals(provider)) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return provider().getScheme() + "://" + getDomain() + getRoot();
    }

    /**
     * 
     * @param path
     * @return children node names
     * @throws IOException
     */
    public abstract List<String> children(DimitPath path) throws IOException;

}
