/**
 * 
 */
package dimit.core.channel;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dimit.core.Dimiter;
import dimit.core.StoreConst;
import dimit.store.ChannelGroup;
import dimit.store.ChannelType;
import dimit.store.conf.ChannelGroupConf;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Mar 21, 2018 3:58:31 PM
 * @version 0.0.1
 */
public class ChannelGroupWrapper implements StoreWrapper<ChannelGroup, ChannelGroupConf> {

    private List<ChannelWrapper<?>> channel;
    private ChannelGroupConf conf;
    private ChannelGroup store;

    private ChannelSelector selector;
    private Dimiter dimiter;

    private ChannelGroupWrapper(Dimiter dimiter) {
        this.dimiter = dimiter;
        channel = Collections.synchronizedList(new LinkedList<ChannelWrapper<?>>());
        selector = new SimpleChannelSelector(this);
    }

    public static final ChannelGroupWrapper init(Dimiter dimiter, String cid) throws IOException {
        DimitStoreSystem dss = dimiter.getStoreSystem();
        DimitPath dimitPath = dss.getPath(StoreConst.PATH_CONF, dimiter.getDimit().conf().getId());

        ChannelGroupWrapper group = new ChannelGroupWrapper(dimiter);
        group.conf = dimitPath.newPath(cid).<ChannelGroupConf> toStore();

        if (group.conf == null) return null;

        // create ChannelGroup
        long ct = System.currentTimeMillis();
        ChannelGroup store = ChannelGroup.newBuilder().setId(IDUtil.storeID(MagicFlag.CHANNEL_GROUP)).setCid(cid).setCt(ct).setMt(ct)
                .setV(Const.V).build();
        group.store = store;

        return group;
    }

    /**
     * 
     * @param gid
     *            ChannelGroupConf's id
     * @param cid
     *            ChannelConf's id
     * @throws IOException
     */
    public <T> void newChannel(String cid, ChannelType type, ChannelCallable<T> callable) throws IOException {
        ChannelWrapper<T> channel = ChannelWrapper.<T> init(dimiter, this.conf().getId(), cid, type, callable);
        if (channel == null) throw new IOException("Couldn't newChannel:" + cid);
        channel().add(channel);
    }

    public List<ChannelWrapper<?>> channel() {
        return channel;
    }

    @Override
    public void close() throws IOException {
        for (ChannelWrapper<?> ch : channel) {
            ch.close();
        }
    }

    public boolean contain(String id) {
        for (ChannelWrapper<?> ch : channel) {
            if (ch.id().equals(id)) return true;
        }
        return false;
    }

    @Override
    public ChannelGroup store() {
        return store;
    }

    @Override
    public ChannelGroupConf conf() {
        return conf;
    }

    public ChannelSelector selector() {
        return selector;
    }

    @Override
    public String name() {
        return conf.getName();
    }

    @Override
    public String id() {
        return store.getId();
    }

    @Override
    public Dimiter dimiter() {
        return this.dimiter;
    }

    @Override
    public void handle(WatchKey key, WatchEvent<?> event) {
        // TODO Auto-generated method stub

    }

}
