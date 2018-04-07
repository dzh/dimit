package dimit.store.sys;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dzh
 * @date Mar 24, 2018 6:16:57 PM
 * @version 0.0.1
 */
public abstract class DimitStoreSystemProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DimitStoreSystemProvider.class);

    // key:uri.getScheme() + "_" + uri.getHost() + "_" + uri.getPath()
    private ConcurrentMap<String, DimitStoreSystem> sysCache = new ConcurrentHashMap<>();

    private static final Object Lock = new Object();
    private static volatile List<DimitStoreSystemProvider> InstalledProviders;

    /**
     * store system type, for example zk or redis and so on
     * 
     * @return
     */
    protected abstract String storeType();

    protected char pathSeperator() {
        return '/'; // 47 = '/'
    }

    public Charset charset() {
        return StandardCharsets.UTF_8;
    }

    public static List<DimitStoreSystemProvider> installedProviders() {
        if (InstalledProviders == null) {
            synchronized (Lock) {
                if (InstalledProviders == null) {
                    List<DimitStoreSystemProvider> list = initProviders(DimitStoreSystemProvider.class.getClassLoader());
                    InstalledProviders = Collections.unmodifiableList(list);
                }
            }
        }

        if (InstalledProviders == null) {
            LOG.warn("Not found any {}", DimitStoreSystemProvider.class.getName());
        }
        return InstalledProviders;
    }

    private static final List<DimitStoreSystemProvider> initProviders(ClassLoader cl) {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        ServiceLoader<DimitStoreSystemProvider> sl = ServiceLoader.load(DimitStoreSystemProvider.class, cl);
        List<DimitStoreSystemProvider> list = new LinkedList<>();
        for (DimitStoreSystemProvider p : sl) {
            list.add(p);

            LOG.info("{} installedProvider {}", cl.getClass().getName(), p.getScheme());
        }
        return list;
    }

    public static void appendProvider(ClassLoader cl) {
        if (cl == null) cl = ClassLoader.getSystemClassLoader();

        installedProviders(); // initialize default providers

        synchronized (Lock) {
            List<DimitStoreSystemProvider> list = new LinkedList<>();

            if (InstalledProviders != null) {
                list.addAll(InstalledProviders);
            }

            for (DimitStoreSystemProvider p : initProviders(cl)) {
                if (!list.contains(p)) {
                    list.add(p);
                }
            }

            InstalledProviders = Collections.unmodifiableList(list);
        }
    }

    protected static DimitStoreSystemProvider findProvider(String scheme) {
        if (InstalledProviders == null) {
            installedProviders();
        }

        if (InstalledProviders != null) {
            synchronized (Lock) {
                for (DimitStoreSystemProvider p : InstalledProviders) {
                    if (p.getScheme().equalsIgnoreCase(scheme)) return p;
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof DimitStoreSystemProvider) { return ((DimitStoreSystemProvider) obj).getScheme()
                .equalsIgnoreCase(this.getScheme()); }
        return false;
    }

    /**
     * 
     * 
     * scheme: dimit-{@link #storeType}
     */
    public String getScheme() {
        return "dimit-" + storeType();
    }

    public DimitStoreSystem openStoreSystem(URI uri, Map<String, Object> env) throws Exception {
        String sysCacheKey = cacheKey(uri);
        DimitStoreSystem dss = sysCache.get(sysCacheKey);
        if (dss != null) return dss;

        dss = createStoreSystem(env);
        if (sysCache.putIfAbsent(sysCacheKey, dss) == null) {
            dss.open(uri);
            return dss;
        } else {
            dss.close();
        }

        return sysCache.get(sysCacheKey);
    }

    public void removeSystemCache(DimitStoreSystem dss) {
        sysCache.remove(cacheKey(dss.getRoot().toUri()));
    }

    /**
     * 
     * @param uri
     * @return key for {@link #sysCache}
     */
    protected static final String cacheKey(URI uri) {
        StringBuilder buf = new StringBuilder();
        buf.append(uri.getScheme());
        buf.append('_');
        buf.append(uri.getHost());
        buf.append('_');
        buf.append(uri.getPath());
        return buf.toString();
    }

    public abstract DimitStoreSystem createStoreSystem(Map<String, Object> env);

    public DimitStoreSystem getStoreSystem(URI uri) {
        return sysCache.get(cacheKey(uri));
    }

}
