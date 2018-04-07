package dimit.zk;

import java.util.List;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
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
 * @date Apr 2, 2018 5:47:58 PM
 * @version 0.0.1
 */
public class TestCurator {

    static Logger LOG = LoggerFactory.getLogger(TestCurator.class);

    private CuratorFramework client;

    @Before
    public void init() {
        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(2000, 3);
            client = CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy);
            client.start();
            client.blockUntilConnected();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore
    public void testAdd() {
        DimitConf dimit = DimitConf.newBuilder().setCt(System.currentTimeMillis()).setMt(System.currentTimeMillis())
                .setId(IDUtil.storeID(Const.V, MagicFlag.DIMIT_CONF)).setV(Const.V).setName("voice").build();
        try {
            String path = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath("/dimit/local/conf/" + dimit.getId(), dimit.toByteArray());
            LOG.info("create {}", path);

            client.delete().deletingChildrenIfNeeded().forPath(path);
            LOG.info("delete {}", path);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore
    public void testChildren() throws Exception {
        List<String> children = client.getChildren().forPath("/");
        LOG.info("children {}", children);
    }

    @Test
    public void testGet() throws Exception {
        byte[] bytes = client.getData().forPath("/xxx");
        LOG.info("get {}", bytes);
    }

    @After
    public void stop() {
        try {
            client.close();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
