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
public class ChannelWrapper<T> implements StoreWrapper<Channel, ChannelConf>, Callable<T> {

    static Logger LOG = LoggerFactory.getLogger(ChannelWrapper.class);

    private volatile ChannelConf conf;
    private volatile Channel store;
    private ChannelGroupWrapper group;

    private ChannelCallable<T> callable;

    private Dimiter dimiter;

    private List<WatchKey> keys;

    private ChannelWrapper(Dimiter dimiter, ChannelCallable<T> callable) {
        this.dimiter = dimiter;
        this.callable = callable;
        this.keys = new LinkedList<>();

        if (callable != null) callable.setChannel(this);
    }

    @Override
    public void close() throws IOException {
        if (keys != null) {
            for (WatchKey key : keys) {
                key.cancel();
            }
        }
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
    public T call() throws Exception {
        return callable.call();
    }

    @Override
    public String id() {
        return store.getId();
    }

    public static <T> ChannelWrapper<T> init(Dimiter dimiter, String gid, String cid, ChannelType type, ChannelCallable<T> callable)
            throws IOException {
        DimitStoreSystem dss = dimiter.getStoreSystem();
        DimitPath groupPath = dss.getPath(StoreConst.PATH_CONF, dimiter.getDimit().conf().getId(), gid);

        // create ChannelConf
        ChannelWrapper<T> channel = new ChannelWrapper<T>(dimiter, callable);
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
                .setTps(tps / childrenSize).setType(type).setV(Const.V).build();
        channel.store = store;
        // create store/cid/0|1/id
        DimitPath channelPath = channelParentPath.newPath(store.getId());
        channelPath = dss.<Channel> io().write(channelPath, store, StoreAttribute.EPHEMERAL);
        LOG.info("create EPHEMERAL channel {}", channelPath);

        if (type == ChannelType.SEND) {
            // watch store/cid/0|1/
            WatchKey key = channelPath.getParent().register(dimiter.watch(), new Kind<?>[] { StoreEventKind.CHILDREN }, channel);
            channel.addWatchKey(key);

            // watch conf/did/gid/cid
            key = groupPath.newPath(cid).register(dimiter.watch(), new Kind<?>[] { StoreEventKind.UPDATE }, channel);
            channel.addWatchKey(key);
        }

        callable.updateLimiter();
        return channel;
    }

    private ChannelConf newChannelConf(Dimiter dimiter, String gid, String cid) throws IOException {
        DimitStoreSystem dss = dimiter.getStoreSystem();
        DimitPath groupPath = dss.getPath(StoreConst.PATH_CONF, dimiter.getDimit().conf().getId(), gid);

        return groupPath.newPath(cid).<ChannelConf> toStore(ChannelConf.class);
    }

    @Override
    public Dimiter dimiter() {
        return dimiter;
    }

    @Override
    public void handle(WatchKey key, WatchEvent<?> event) {
        Kind<?> k = event.kind();
        // watch conf/did/gid/cid
        if (k == StoreEventKind.UPDATE) {
            // update conf
            ChannelConf oldConf = this.conf;
            try {
                this.conf = newChannelConf(dimiter, oldConf.getGid(), oldConf.getId());
                // LOG.info("new conf {}", this.conf);
                updateTps();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        // watch store/cid/0|1/
        else if (k == StoreEventKind.CHILD_ADD || k == StoreEventKind.CHILD_DELETE || k == StoreEventKind.CHILD_UPDATE) {
            try {
                // DimitPath path = (DimitPath) key.watchable();
                // List<DimitPath> children = path.children();
                // float maxTps = this.conf.getTps();
                // this.store.toBuilder().setTps(children.isEmpty() ? maxTps : maxTps / children.size());
                updateTps();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void updateTps() throws IOException {
        DimitStoreSystem dss = dimiter.getStoreSystem();

        // update tps
        DimitPath channelPath =
                dss.getPath(StoreConst.PATH_STORE, conf.getId(), String.valueOf(store.getType().getNumber()), store.getId());
        List<DimitPath> children = channelPath.getParent().children();
        float maxTps = this.conf.getTps();
        this.store.toBuilder().setTps(children.isEmpty() ? maxTps : maxTps / children.size());

        callable.updateLimiter();
    }

    public float tps() {
        return this.store.getTps();
    }

    public boolean isValid() {
        if (conf.getStatus().equals(ChannelStatus.CLOSED) || conf.getStatus().equals(ChannelStatus.INVALID)) return false;

        return tps() >= 1 && priority() >= 1;
    }

    public int priority() {
        return conf.getPriority();// TODO runtime calculation
    }

    public boolean cantainTags(String... tags) {
        if (tags == null) return true;
        return conf.getTagList().containsAll(Arrays.asList(tags));

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ChannelWrapper) { return ((ChannelWrapper<?>) obj).id() == id(); }
        return false;
    }

    @Override
    public String toString() {
        return store.getId() + "_" + conf.toString();
    }

}
