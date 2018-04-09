package dimit.demo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.Dimiter;
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
        group.newChannel("21001", ChannelType.SEND);

        group.newChannel("21002", ChannelType.SEND);

        group.newChannel("21003", ChannelType.SEND);
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
        List<ChannelWrapper> selected = null;
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
        List<ChannelWrapper> selected = null;
        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); // 21001
        if (!selected.isEmpty()) {
            LOG.info("tps {}", selected.get(0).tps()); // 2.0
        }

        // tps/2
        DimitPath path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        ChannelConf conf21001 = path21001.toStore(ChannelConf.class);
        float oldTps = conf21001.getTps();
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(conf21001.getTps() / 2).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001); // 1.0
        Thread.sleep(1000L);

        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); // 21001
        if (!selected.isEmpty()) {
            LOG.info("tps {} {}", selected.get(0).tps(), selected.get(0).isValid()); // 1.0
        }

        path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        conf21001 = path21001.toStore(ChannelConf.class);
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(conf21001.getTps() / 2).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001); // 0.5
        Thread.sleep(1000L);

        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
        LOG.info("select only mobile {}", selected); //
        if (!selected.isEmpty()) {
            LOG.info("tps {} {}", selected.get(0).tps(), selected.get(0).isValid());
        }

        // restore
        path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
        conf21001 = path21001.toStore(ChannelConf.class);
        conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(oldTps).build();
        demo.dimiter().getStoreSystem().io().write(path21001, conf21001);
        LOG.info("restore {}", path21001.toStore(ChannelConf.class));
    }

    @Test
    @Ignore
    public void testChannelCall() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(4);

        // select channel
        List<ChannelWrapper> selected = null;
        selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);

        if (!selected.isEmpty()) {
            final ChannelWrapper ch = selected.get(0);
            final AtomicInteger execCount = new AtomicInteger(0);
            int forCount = 0;

            float oldTps = 2.0f;
            while (true) {
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ch.call(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    Thread.sleep(10);
                                    LOG.info("exec {}", execCount.incrementAndGet());
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            LOG.info(e.getMessage(), e);
                        }
                    }
                });

                if (++forCount == 100) break;

                if (forCount == 20) {
                    // tps/2
                    DimitPath path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
                    ChannelConf conf21001 = path21001.toStore(ChannelConf.class);
                    oldTps = conf21001.getTps();
                    conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(conf21001.getTps() / 2).build();
                    demo.dimiter().getStoreSystem().io().write(path21001, conf21001); // 1.0
                    Thread.sleep(1000L);

                    selected = demo.dimiter().getDimit().group(group.id()).select(DemoConst.TAG_FIXED, DemoConst.TAG_MOBILE);
                    LOG.info("select only mobile {}", selected); // 21001
                    if (!selected.isEmpty()) {
                        LOG.info("tps {} {}", selected.get(0).tps(), selected.get(0).isValid()); // 1.0
                    }
                }
                if (forCount == 80) {
                    // restore
                    DimitPath path21001 = demo.dimiter().getStoreSystem().getPath("conf", "voice", "vcode", "21001");
                    ChannelConf conf21001 = path21001.toStore(ChannelConf.class);
                    conf21001 = conf21001.toBuilder().setMt(System.currentTimeMillis()).setTps(oldTps).build();
                    demo.dimiter().getStoreSystem().io().write(path21001, conf21001);
                    LOG.info("restore {}", path21001.toStore(ChannelConf.class));
                }

            }

            Thread.sleep(6000);
            LOG.info("for-{} exec-{}", forCount, execCount.get());
        }
    }

    @AfterClass
    public static void close() {
        try {
            Thread.sleep(2000L);
            demo.close();
        } catch (Exception e) {}
    }

}
