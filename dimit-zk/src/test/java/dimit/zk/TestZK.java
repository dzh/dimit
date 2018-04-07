/**
 * 
 */
package dimit.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.conf.DimitConf;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Mar 23, 2018 11:22:08 AM
 * @version 0.0.1
 */
@Ignore
@Deprecated
public class TestZK {

    static Logger LOG = LoggerFactory.getLogger(TestZK.class);

    private ZooKeeper zk;

    @Before
    public void init() {
        try {
            zk = new ZooKeeper("localhost:2181", 6000, new SimpleWatcher(), true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore
    public void testAdd() {
        DimitConf dimit = DimitConf.newBuilder().setCt(System.currentTimeMillis()).setMt(System.currentTimeMillis())
                .setId(IDUtil.storeID(Const.V, MagicFlag.DIMIT_CONF)).setV(Const.V).setName("voice").build();
        byte[] data = dimit.toByteArray();
        try {
            String path = zk.create("/dimit/local/conf/" + dimit.getId(), data, null, CreateMode.PERSISTENT);
            LOG.info("create {}", path);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @After
    public void stop() {
        try {
            zk.close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public class SimpleWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            LOG.info(event.toString());
        }

    }

}
