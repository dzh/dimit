package dimit.demo.store;

import java.io.IOException;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.demo.DemoConst;
import dimit.store.conf.ChannelStatus;
import dimit.store.conf.DimitConf;
import dimit.store.sys.DimitPath;

/**
 * 初始化测试数据
 * 
 * @author dzh
 * @date Apr 9, 2018 12:17:04 PM
 * @version 0.0.1
 */
public class TestZkStoreConfDemo {

    static Logger LOG = LoggerFactory.getLogger(TestZkStoreConfDemo.class);

    private static ZkStoreConfDemo zkStore;

    @BeforeClass
    public static void init() {
        zkStore = new ZkStoreConfDemo("dimit-zk://dzh/dimit?host=127.0.0.1:2181&sleep=1000&retry=3");

    }

    @Test
    public void createDimitConf() throws IOException {
        DimitPath path = zkStore.createDimitConf("voice", "语音流控");

        DimitConf dimitConf = path.<DimitConf> toStore(DimitConf.class);
        LOG.info("read dimit {} {}", path, dimitConf);
    }

    @Test
    public void createChannelGroupConf() throws IOException {
        DimitPath path = zkStore.createChannelGroupConf("voice", "vcode", "验证码通道组");

        DimitConf dimitConf = path.<DimitConf> toStore(DimitConf.class);
        LOG.info("read dimit {} {}", path, dimitConf);
    }

    @Test
    public void createChannelConf_1() throws IOException {
        DimitPath path = zkStore.createChannelConf("voice", "vcode", "21001", "ch1", ChannelStatus.PRIMARY, 10, 100f,
                Arrays.asList(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE));

        DimitConf dimitConf = path.<DimitConf> toStore(DimitConf.class);
        LOG.info("read dimit {} {}", path, dimitConf);
    }

    @Test
    public void createChannelConf_2() throws IOException {
        DimitPath path = zkStore.createChannelConf("voice", "vcode", "21002", "ch2", ChannelStatus.PRIMARY, 8, 100f,
                Arrays.asList(DemoConst.TAG_FIXED));

        DimitConf dimitConf = path.<DimitConf> toStore(DimitConf.class);
        LOG.info("read dimit {} {}", path, dimitConf);
    }

    @Test
    public void createChannelConf_3() throws IOException {
        DimitPath path = zkStore.createChannelConf("voice", "vcode", "21003", "ch3", ChannelStatus.STANDBY, 8, 100f,
                Arrays.asList(DemoConst.TAG_MOBILE));

        DimitConf dimitConf = path.<DimitConf> toStore(DimitConf.class);
        LOG.info("read dimit {} {}", path, dimitConf);
    }

    @AfterClass
    public static void close() {
        try {
            zkStore.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
