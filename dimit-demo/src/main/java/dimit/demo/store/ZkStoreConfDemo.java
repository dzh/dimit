package dimit.demo.store;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.conf.ChannelConf;
import dimit.store.conf.ChannelGroupConf;
import dimit.store.conf.ChannelStatus;
import dimit.store.conf.DimitConf;
import dimit.store.sys.Const;
import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.DimitStores;
import dimit.store.sys.StoreAttribute;

/**
 * 示例操作dimit相关zk里的配置, 详见{@link TestZkStoreConfDemo}
 * 
 * @author dzh
 * @date Apr 9, 2018 11:11:10 AM
 * @version 0.0.1
 */
public class ZkStoreConfDemo implements Closeable {

    static Logger LOG = LoggerFactory.getLogger(ZkStoreConfDemo.class);

    private DimitStoreSystem dss;

    private DimitPath confPath;

    public ZkStoreConfDemo(String uri) {
        try {
            dss = DimitStores.newStoreSystem(URI.create(uri), null);

            confPath = dss.getPath("conf");
            LOG.info("{}", dss.toString());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public DimitStoreSystem storeSystem() {
        return dss;
    }

    public DimitPath createDimitConf(String id, String name) throws IOException {
        long ct = System.currentTimeMillis();
        DimitConf storeConf = DimitConf.newBuilder().setCt(ct).setId(id).setMt(ct).setName(name).setV(Const.V).build();

        DimitPath path = confPath.newPath(id);
        path = dss.io().write(path, storeConf, StoreAttribute.PERSISTENT);
        LOG.info("create DimitConf {} {} {}", id, name, path);
        return path;
    }

    public DimitPath createChannelGroupConf(String did, String id, String name) throws IOException {
        long ct = System.currentTimeMillis();
        ChannelGroupConf storeConf =
                ChannelGroupConf.newBuilder().setCt(ct).setDid(did).setId(id).setMt(ct).setName(name).setV(Const.V).build();

        DimitPath path = confPath.newPath(did).newPath(id);
        path = dss.io().write(path, storeConf, StoreAttribute.PERSISTENT);
        LOG.info("create ChannelGroupConf {} {} {}", id, name, path);
        return path;
    }

    public DimitPath createChannelConf(String did, String gid, String id, String name, ChannelStatus status, int priority, float tps,
            List<String> tags) throws IOException {
        long ct = System.currentTimeMillis();
        ChannelConf storeConf = ChannelConf.newBuilder().setCt(ct).setGid(gid).setId(id).setMt(ct).setName(name).setPriority(priority)
                .setStatus(status).setTps(tps).setV(Const.V).addAllTag(tags).build();

        DimitPath path = confPath.newPath(did).newPath(gid).newPath(id);
        path = dss.io().write(path, storeConf, StoreAttribute.PERSISTENT);
        LOG.info("create ChannelConf {} {} {}", id, name, path);
        return path;
    }

    public void removeDimitConf(String id) {

    }

    @Override
    public void close() throws IOException {
        if (dss != null) dss.close();
    }

    /**
     * @param args
     */
    // public static void main(String[] args) {
    // // open DimitStoreSystem
    // ZkStoreConfDemo zkStore = new ZkStoreConfDemo("dimit-zk://yp/dimit?host=127.0.0.1:2181&sleep=1000&retry=3");
    //
    // // create dimit
    // try {
    // } catch (Exception e) {
    // LOG.error(e.getMessage(), e);
    // }
    // }

}
