/**
 * 
 */
package dimit.zk.store;

import java.io.IOException;
import java.net.URI;

import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.conf.DimitConf;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.DimitStores;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Mar 26, 2018 3:58:41 PM
 * @version 0.0.1
 */
public class TestZkStoreSystem {

    static final Logger LOG = LoggerFactory.getLogger(TestZkStoreSystem.class);

    private ZkStoreSystem dss;

    @Before
    public void testZkStoreSystem() throws Exception {
        dss = (ZkStoreSystem) DimitStores.newStoreSystem(URI.create("dimit-zk://yp/dimit?host=127.0.0.1:2181&sleep=1000&retry=3"), null);
        LOG.info("{}", dss.toString());
    }

    @Test
    public void testCreateDimitConf() {
        DimitConf dimit = DimitConf.newBuilder().setCt(System.currentTimeMillis()).setMt(System.currentTimeMillis())
                .setId(IDUtil.storeID(Const.V, MagicFlag.DIMIT_CONF)).setV(Const.V).setName("voice").build();
        try {
            String path = dss.zkCli().create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath("/dimit/yp/conf/" + dimit.getId(), dimit.toByteArray());
            LOG.info("create {}", path);

            dss.zkCli().delete().deletingChildrenIfNeeded().forPath(path);
            LOG.info("delete {}", path);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @After
    public void close() {
        try {
            dss.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
