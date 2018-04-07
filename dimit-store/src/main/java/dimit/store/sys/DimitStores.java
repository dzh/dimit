package dimit.store.sys;

import java.net.URI;
import java.util.Map;

import dimit.store.conf.ChannelConf;
import dimit.store.conf.ChannelGroupConf;
import dimit.store.conf.DimitConf;

/**
 * @author dzh
 * @date Mar 28, 2018 12:00:21 PM
 * @version 0.0.1
 */
public final class DimitStores {

    public static DimitStoreSystem newStoreSystem(URI uri, Map<String, Object> env) throws Exception {
        DimitStoreSystemProvider p = DimitStoreSystemProvider.findProvider(uri.getScheme());
        if (p == null) { throw new NullPointerException("Not found DimitStoreSystemProvider scheme:" + uri.getScheme()); }

        return p.openStoreSystem(uri, env);
    }

    public static boolean writeDimitConf(DimitPath path, DimitConf dimit) {
        
        return true;
    }

    public static DimitConf readDimitConf(DimitPath path) {

        return null;
    }

    public static boolean writeChannelGroupConf(DimitPath path, ChannelGroupConf groupConf) {

        return true;
    }

    public static boolean writeChannelConf(DimitPath path, ChannelConf ch) {

        return true;
    }

}
