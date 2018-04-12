package dimit.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.channel.ChannelWrapper;
import dimit.core.channel.DimitWrapper;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.DimitStores;
import dimit.store.sys.event.EventHandler;
import dimit.store.sys.event.StoreWatchKey;

/**
 * 
 * 流控器Dimiter实现一个DimitConf实例
 * 
 * <pre>
 * TODO
 * master dimit clean store/ conf/
 * </pre>
 * 
 * @author dzh
 * @date Apr 4, 2018 11:37:38 AM
 * @version 0.0.1
 */
public class Dimiter implements Closeable {

    final static Logger LOG = LoggerFactory.getLogger(Dimiter.class);

    private DimitStoreSystem storeSystem;
    private WatchService watch;
    private Thread watchThread;

    private DimitWrapper dimit;

    private Map<String, Object> env;

    private CountDownLatch watchLatch = new CountDownLatch(1);

    private boolean statEnable;
    private ChannelStatWorker[] workers;
    private int idxWorker = 0;

    private Dimiter(DimitStoreSystem dss) throws IOException {
        this.storeSystem = dss;
    }

    public void start() {
        try {
            watch = storeSystem.newWatchService();
            startWatchThread();

            // stat worker
            statEnable = Boolean.parseBoolean(String.valueOf(env(StoreConst.P_STAT_ENABLE, "true")));
            LOG.info("statEnable {}", statEnable);
            if (statEnable) {
                int workCount = Integer.parseInt(String.valueOf(env(StoreConst.P_STAT_WORKER_COUNT, "1"))); // TODO
                long snapshotMs = Long.parseLong(String.valueOf(env(StoreConst.P_STAT_WORKER_SNAPSHOT_INTERVAL, "1000")));
                long syncMs = Long.parseLong(String.valueOf(env(StoreConst.P_STAT_WORKER_SYNC_INTERVAL, "5000")));
                workers = new ChannelStatWorker[workCount];
                for (int i = 0; i < workCount; ++i) {
                    workers[i] = new ChannelStatWorker("ChannelStatWorker-" + i, snapshotMs, syncMs);
                    workers[i].start();
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public boolean statEnable() {
        return statEnable;
    }

    public ChannelStatWorker nextWorker() {
        if (statEnable && workers != null) {
            synchronized (workers) {
                if (idxWorker >= workers.length) {
                    idxWorker = 0;
                }
                return workers[idxWorker++];
            }
        }
        return null;
    }

    /**
     * channel负载到ChannelStatWorker中
     * 
     * @param channel
     */
    public void statChannel(ChannelWrapper channel) {
        if (!statEnable || channel == null) return;
        ChannelStatWorker worker = nextWorker();
        if (worker == null) {
            LOG.error("statChannel but not found worker. {}", channel);
            return;
        }
        worker.addChannel(channel);
    }

    private void startWatchThread() {
        if (watch != null) {
            final String name = "WatchThread-" + id();
            watchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            StoreWatchKey key = (StoreWatchKey) watch.poll(6, TimeUnit.SECONDS); // TODO
                            if (key == null) continue;
                            Object attachment = key.getAttachment(); // EventHandler

                            List<WatchEvent<?>> events = key.pollEvents();
                            if (events != null) {
                                for (WatchEvent<?> event : events) {
                                    if (attachment != null && attachment instanceof EventHandler) {
                                        ((EventHandler) attachment).handle(key, event);
                                    }
                                }
                            }
                            key.reset();
                        } catch (ClosedWatchServiceException e) {
                            LOG.warn(e.getMessage(), e);
                            break;
                        } catch (InterruptedException e) {
                            // LOG.warn(e.getMessage(), e);
                            break;
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    watchLatch.countDown();
                    LOG.info("{} closed", name);
                }
            }, name);
            watchThread.setDaemon(true);
            watchThread.start();
            LOG.info("{} start", watchThread.getName());
        }

    }

    public DimitWrapper dimit() {
        return this.dimit;
    }

    /**
     * 
     * @param key
     * @param defVal
     * @return value or defVal if value is null
     */
    public Object env(String key, Object defVal) {
        if (env == null) return defVal;

        Object val = env.get(key);
        return val == null ? defVal : val;
    }

    /**
     * 
     * @param uri
     *            DimitStoreSystem URI
     * @param env
     * @param cid
     *            DimitConf's id
     * @return
     * @throws Exception
     */
    public static final Dimiter newDimiter(URI uri, Map<String, Object> env, String cid) throws Exception {
        DimitStoreSystem dss = DimitStores.newStoreSystem(uri, env);

        Dimiter d = new Dimiter(dss);
        DimitWrapper dimit = DimitWrapper.init(d, cid);
        d.dimit = dimit;
        d.env = env == null ? Collections.<String, Object> emptyMap() : Collections.unmodifiableMap(env);
        d.start();
        return d;
    }

    public WatchService watch() {
        return watch;
    }

    public DimitStoreSystem storeSystem() {
        return storeSystem;
    }

    public String id() {
        return dimit.id();
    }

    @Override
    public String toString() {
        return dimit.id() + "_" + dimit.name() + "_" + storeSystem;
    }

    @Override
    public void close() throws IOException { // TODO IOException
        if (workers != null) {
            for (ChannelStatWorker worker : workers) {
                worker.close();
            }
        }

        if (dimit != null) dimit.close();

        if (watch != null) {
            watch.close();
            watchThread.interrupt();
            try {
                watchLatch.await(10, TimeUnit.MICROSECONDS);
            } catch (InterruptedException e) {}
        }

        if (storeSystem != null) storeSystem.close();
    }

}
