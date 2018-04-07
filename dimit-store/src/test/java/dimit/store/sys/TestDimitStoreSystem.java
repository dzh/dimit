package dimit.store.sys;

import java.net.URI;
import java.util.ServiceLoader;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dzh
 * @date Mar 26, 2018 3:37:40 PM
 * @version 0.0.1
 */
public class TestDimitStoreSystem {

    static Logger LOG = LoggerFactory.getLogger(TestDimitStoreSystem.class);

    @Test
    @Ignore
    public void testDimitUri() {
        String fsType = "zk";
        String dfsPath = "yp";

        URI dfsUri = URI.create("dimit-" + fsType + "://dimit/" + dfsPath + "?host=192.168.1.1:2181,192.168.1.2:2181");
        LOG.info("scheme:{}", dfsUri.getScheme());
        LOG.info("path:{}", dfsUri.getPath());
        LOG.info("rawPath:{}", dfsUri.getRawPath());
        LOG.info("host:{}", dfsUri.getHost());
        LOG.info("query:{}", dfsUri.getQuery());
        LOG.info("authority:{}", dfsUri.getAuthority());
        LOG.info("fragment:{}", dfsUri.getFragment());
    }

    @Test
    public void testServiceLoader() {
        ServiceLoader<DimitStoreSystemProvider> sl = ServiceLoader.<DimitStoreSystemProvider> load(DimitStoreSystemProvider.class,
                Thread.currentThread().getContextClassLoader());
        for (DimitStoreSystemProvider p : sl) {
            LOG.info("{}", p.getScheme());
        }

    }

}
