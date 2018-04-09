package dimit.demo;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.Dimiter;
import dimit.core.channel.ChannelCallable;
import dimit.core.channel.ChannelGroupWrapper;
import dimit.core.channel.ChannelWrapper;
import dimit.demo.dimiter.DimiterDemo;
import dimit.demo.store.TestZkStoreConfDemo;
import dimit.store.ChannelType;
import dimit.store.conf.ChannelConf;
import dimit.store.conf.ChannelStatus;
import dimit.store.sys.DimitPath;

/**
 * @author dzh
 * @date Apr 9, 2018 2:53:59 PM
 * @version 0.0.1
 */
public class TestDimiterDemo {

    static Logger LOG = LoggerFactory.getLogger(TestZkStoreConfDemo.class);

    private static DimiterDemo demo;

    static ChannelGroupWrapper group;

    @BeforeClass
    public static void init() throws IOException {
        demo = new DimiterDemo("dimit-zk://yp/dimit?host=127.0.0.1:2181&sleep=1000&retry=3", "voice");

        // 初始化通道组
        group = demo.initChannelGroup("vcode");
        // 初始化需要的通道
        group.<Result> newChannel("21001", ChannelType.SEND, new ChannelCallable<Result>() {
            @Override
            protected Result toCall() {
                Result r = new Result();

                LOG.info("21001 result-{}", r);
                return r;
            }
        });

        group.<Result> newChannel("21002", ChannelType.SEND, new ChannelCallable<Result>() {
            @Override
            protected Result toCall() {

                Result r = new Result();
                LOG.info("21002 result-{}", r);
                return r;
            }
        });

        group.<Result> newChannel("21003", ChannelType.SEND, new ChannelCallable<Result>() {
            @Override
            protected Result toCall() {
                Result r = new Result();

                LOG.info("21003 result-{}", r);
                return r;
            }
        });
    }

    @Test
    @Ignore
    public void testDimiterInfo() {
        Dimiter d = demo.dimiter();
        LOG.info("dimiter info {}", d.toString());
        LOG.info("result succ-{} fail-{}", Result.CODE.SUCC.ordinal(), Result.CODE.FAIL.ordinal());
    }

    /**
     * 配置通过{@link TestZkStoreConfDemo}初始化
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void testSelectChannel() throws IOException, InterruptedException {
        // select channel
        List<ChannelWrapper<?>> selected = null;
        // selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
        // LOG.info("select fixed and mobile {}", selected); // 21001
        // selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED);
        // LOG.info("select only fixed {}", selected); // 21002 21001
        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); // 21001 21003

        // invalid 21001
        DimitPath path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        ChannelConf conf21001 = path21001.toStore(ChannelConf.class);
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setStatus(ChannelStatus.INVALID).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001);

        LOG.info("invalid {}", path21001.toStore(ChannelConf.class));
        Thread.sleep(1000L); // TODO

        // select channel
        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); // 21003

        // valid 21001
        path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        conf21001 = path21001.toStore(ChannelConf.class);
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setStatus(ChannelStatus.PRIMARY).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001);
        LOG.info("valid {}", path21001.toStore(ChannelConf.class));

    }

    @Test
    @Ignore
    public void testUpdateTps() throws IOException, InterruptedException {
        List<ChannelWrapper<?>> selected = null;
        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); // 21001
        LOG.info("tps {}", selected.get(0).tps());

        // tps/2
        DimitPath path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        ChannelConf conf21001 = path21001.toStore(ChannelConf.class);
        float oldTps = conf21001.getTps();
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(oldTps / 2).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001);
        LOG.info("tps half {}", path21001.toStore(ChannelConf.class));
        Thread.sleep(1000L);

        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); // 21001
        LOG.info("tps {}", selected.get(0).tps());

        // restore
        path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        conf21001 = path21001.toStore(ChannelConf.class);
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(oldTps).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001);
        LOG.info("restore {}", path21001.toStore(ChannelConf.class));
    }

    @AfterClass
    public static void close() {
        try {
            demo.close();
        } catch (IOException e) {}
    }

}
