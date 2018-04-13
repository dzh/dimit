package dimit.core.channel;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import dimit.core.Dimiter;
import dimit.core.StoreConst;
import dimit.store.Channel;
import dimit.store.ChannelType;
import dimit.store.conf.ChannelConf;
import dimit.store.conf.ChannelStatus;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.StoreAttribute;
import dimit.store.sys.event.StoreEventKind;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Apr 4, 2018 11:53:57 AM
 * @version 0.0.1
 */
public class ChannelWrapper implements StoreWrapper<Channel, ChannelConf> {

    static Logger LOG = LoggerFactory.getLogger(ChannelWrapper.class);

    private volatile ChannelConf conf;
    private volatile Channel store;
    private ChannelGroupWrapper group;

    private Dimiter dimiter;

    private List<WatchKey> keys;

    private RateLimiter limiter;

    private ChannelStatWrapper stat;

    private ChannelWrapper(Dimiter dimiter, ChannelGroupWrapper group) {
        this.dimiter = dimiter;
        this.group = group;
        this.keys = new LinkedList<>();
    }

    public synchronized void updateLimiter() {
        LOG.info("{} {} updateLimiter:{}", conf.getId(), id(), tps());
        if (limiter == null) {
            limiter = RateLimiter.create(tps());
        } else {
            limiter.setRate(tps());
        }
    }

    @Override
    public void close() throws IOException {
        if (keys != null) {
            for (WatchKey key : keys) {
                key.cancel();
            }
        }
        stat.close();
    }

    private void addWatchKey(WatchKey key) {
        keys.add(key);
    }

    @Override
    public String name() {
        return conf.getName();
    }

    @Override
    public Channel store() {
        return store;
    }

    @Override
    public ChannelConf conf() {
        return conf;
    }

    public ChannelGroupWrapper group() {
        return group;
    }

    @Override
    public String id() {
        return store.getId();
    }

    /**
     * 
     * @param c
     * @return
     * @throws Exception
     */
    public <V> V call(Callable<V> c) throws Exception {
        if (!limiter.tryAcquire()) { throw new RateLimiterException("out of tps:" + limiter.getRate()); }

        long st = System.currentTimeMillis();
        try {
            return c.call();
        } finally {
            if (stat != null) {
                stat.incrCount();
                stat.addTime(System.currentTimeMillis() - st);
            }
        }
    }

    /**
     * 增加统计功能特性: 成功率
     * 
     * 
     * @param c
     * @return
     * @throws Exception
     */
    public <V> V call(ChannelCallable<V> c) throws Exception {
        if (!limiter.tryAcquire()) { throw new RateLimiterException("out of tps:" + limiter.getRate()); }

        long st = System.currentTimeMillis();
        V v = null;
        try {
            v = c.call();
            return v;
        } finally {
            if (stat != null) {
                long interval = System.currentTimeMillis() - st;

                stat.incrCount();
                stat.addTime(interval);

                switch (c.code(v)) {
                case ChannelCallable.CODE_SUCC: {
                    stat.incrSuccCount();
                    stat.addSuccTime(interval);
                    break;
                }
                case ChannelCallable.CODE_FATAL: {
                    invalid();
                    throw new InvalidChannelException("Invalid ChannelConf:" + conf.getId() + " code:" + ChannelCallable.CODE_FATAL);
                }
                }
            }
        }
    }

    public static ChannelWrapper init(Dimiter dimiter, ChannelGroupWrapper group, String cid, ChannelType type) throws IOException {
        DimitStoreSystem dss = dimiter.storeSystem();
        String gid = group.conf().getId();
        DimitPath groupPath = dss.getPath(StoreConst.PATH_CONF, dimiter.dimit().conf().getId(), gid);

        // create ChannelConf
        ChannelWrapper channel = new ChannelWrapper(dimiter, group);
        channel.conf = groupPath.newPath(cid).<ChannelConf> toStore(ChannelConf.class);

        if (channel.conf == null) return null;

        // find current children from store/cid/0|1
        DimitPath channelParentPath = dss.getPath(StoreConst.PATH_STORE, channel.conf.getId(), String.valueOf(type.getNumber()));
        List<DimitPath> children = channelParentPath.children();
        int childrenSize = children.size() + 1;
        // create Channel
        long ct = System.currentTimeMillis();
        float tps = channel.conf.getTps();
        Channel store = Channel.newBuilder().setId(IDUtil.storeID(MagicFlag.CHANNEL)).setCid(cid).setCt(ct).setMt(ct)
                .setTps(tps / childrenSize).setType(type).setV(Const.V).setDimit(dimiter.id()).build();
        channel.store = store;
        // create store/cid/0|1/id
        DimitPath channelPath = channelParentPath.newPath(store.getId());
        channelPath = dss.<Channel> io().write(channelPath, store, StoreAttribute.EPHEMERAL);
        LOG.info("create EPHEMERAL channel {}", channelPath);

        if (type == ChannelType.SEND) {
            // watch store/cid/0|1/
            WatchKey key = channelPath.getParent().register(dimiter.watch(),
                    // new Kind<?>[] { StoreEventKind.CHILD_ADD, StoreEventKind.CHILD_DELETE }, channel);
                    new Kind<?>[] { StoreEventKind.CHILDREN }, channel);
            channel.addWatchKey(key);

            // watch conf/did/gid/cid
            key = groupPath.newPath(cid).register(dimiter.watch(), new Kind<?>[] { StoreEventKind.UPDATE }, channel);
            channel.addWatchKey(key);
        }

        channel.updateLimiter();

        if (dimiter.statEnable()) { // enable stat
            channel.stat = ChannelStatWrapper.init(channel);
            dimiter.statChannel(channel);
        }

        return channel;
    }

    private ChannelConf newChannelConf(Dimiter dimiter, String gid, String cid) throws IOException {
        DimitStoreSystem dss = dimiter.storeSystem();
        DimitPath groupPath = dss.getPath(StoreConst.PATH_CONF, dimiter.dimit().conf().getId(), gid);

        return groupPath.newPath(cid).<ChannelConf> toStore(ChannelConf.class);
    }

    public ChannelStatWrapper stat() {
        return stat;
    }

    @Override
    public Dimiter dimiter() {
        return dimiter;
    }

    @Override
    public void handle(WatchKey key, WatchEvent<?> event) {
        if (!key.isValid()) {
            LOG.warn("{} {}", key, event);
            return;
        }

        Kind<?> k = event.kind();
        // watch conf/did/gid/cid
        if (k == StoreEventKind.UPDATE) {
            // update conf
            ChannelConf oldConf = this.conf;
            try {
                this.conf = newChannelConf(dimiter, oldConf.getGid(), oldConf.getId());
                LOG.debug("new conf {}", this.conf);
                if (oldConf.getTps() != this.conf.getTps()) {
                    updateTps(this.conf);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        // watch store/cid/0|1/ TODO ignore k == StoreEventKind.CHILD_UPDATE
        else if (k == StoreEventKind.CHILD_ADD || k == StoreEventKind.CHILD_DELETE) {
            try {
                // DimitPath path = (DimitPath) key.watchable();
                // List<DimitPath> children = path.children();
                // float maxTps = this.conf.getTps();
                // this.store.toBuilder().setTps(children.isEmpty() ? maxTps : maxTps / children.size());
                updateTps(this.conf);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            LOG.info("discard {} {}", key, event);
        }
    }

    private void updateTps(ChannelConf conf) throws IOException {
        DimitStoreSystem dss = dimiter.storeSystem();

        // update tps
        DimitPath channelPath =
                dss.getPath(StoreConst.PATH_STORE, conf.getId(), String.valueOf(store.getType().getNumber()), store.getId());
        List<DimitPath> children = channelPath.getParent().children();
        float maxTps = conf.getTps();
        this.store = this.store.toBuilder().setTps(children.isEmpty() ? maxTps : maxTps / children.size()).build();
        // write store
        dss.<Channel> io().write(channelPath, store, StoreAttribute.EPHEMERAL); // TODO merge
        // LOG.info("update EPHEMERAL channel {}", store);

        // LOG.info("update tps-{}", tps());
        updateLimiter();
    }

    public float tps() {
        return store.getTps();
    }

    /**
     * set channel ChannelStatus.INVALID
     */
    public void invalid() {
        DimitStoreSystem dss = dimiter.storeSystem();
        DimitPath path = dss.getPath(StoreConst.PATH_CONF, dimiter.dimit().conf().getId(), group.conf().getId(), conf.getId());
        LOG.info("Invalid {}", path);

        try {
            dss.io().write(path, conf.toBuilder().setStatus(ChannelStatus.INVALID).setMt(System.currentTimeMillis()).build(),
                    StoreAttribute.PERSISTENT);
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
        }

    }

    public boolean isValid() {
        if (conf.getStatus().equals(ChannelStatus.CLOSED) || conf.getStatus().equals(ChannelStatus.INVALID)) return false;

        // ChannelType
        return store.getType() == ChannelType.SEND ? (tps() > 0 && priority() > StoreConst.MIN_PRIORITY) : true;
    }

    public int priority() { // TODO priority cache
        int priority = conf.getPriority();
        if (stat != null) {
            priority += stat.calcPriority();
        }

        if (priority > StoreConst.MAX_PRIORITY) return StoreConst.MAX_PRIORITY;
        if (priority < StoreConst.MIN_PRIORITY) return StoreConst.MIN_PRIORITY;

        return priority;
    }

    public boolean cantainTags(String... tags) {
        if (tags == null) return true;
        return conf.getTagList().containsAll(Arrays.asList(tags));

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ChannelWrapper) { return ((ChannelWrapper) obj).id() == id(); }
        return false;
    }

    @Override
    public String toString() {
        return store.getId() + "_" + conf.toString();
    }

}
