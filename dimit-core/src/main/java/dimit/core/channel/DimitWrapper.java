package dimit.core.channel;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.Dimiter;
import dimit.core.StoreConst;
import dimit.core.util.DimiterUtil;
import dimit.store.Dimit;
import dimit.store.Dimit.DimitRole;
import dimit.store.Dimit.DimitStatus;
import dimit.store.conf.DimitConf;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.StoreAttribute;
import dimit.store.util.IDUtil;

/**
 * <pre>
 * TODO 选举 {@link DimitRole#MASTER MASTER}
 * TODO 如何减少持久代的内存
 * </pre>
 * 
 * @author dzh
 * @date Apr 4, 2018 11:54:30 AM
 * @version 0.0.1
 */
public class DimitWrapper implements StoreWrapper<Dimit, DimitConf> {
    static Logger LOG = LoggerFactory.getLogger(DimitWrapper.class);

    private DimitConf conf;
    private Dimit store;
    private List<ChannelGroupWrapper> group;
    private Dimiter dimiter;

    private DimitWrapper(Dimiter dimiter) {
        this.dimiter = dimiter;
        group = Collections.synchronizedList(new LinkedList<ChannelGroupWrapper>());
    }

    public static DimitWrapper init(Dimiter dimiter, String cid) throws IOException {
        // find DimitConf, under conf/DimitConf
        DimitStoreSystem dss = dimiter.getStoreSystem();
        DimitPath pathConf = dss.getPath(StoreConst.PATH_CONF);
        DimitWrapper dimit = new DimitWrapper(dimiter);

        dimit.conf = pathConf.newPath(cid).<DimitConf> toStore();
        // List<DimitPath> children = conf.children();
        // for (DimitPath child : children) {
        // DimitConf dimitConf = child.<DimitConf> toStore();
        // if (dimitConf.getName().equals(name)) {
        // dw.conf = dimitConf;
        // break;
        // }
        // }
        if (dimit.conf == null) return null;

        // create Dimit
        long ct = System.currentTimeMillis();
        String[] pidHost = DimiterUtil.pidAndHost();
        Dimit store = Dimit.newBuilder().setId(IDUtil.storeID(MagicFlag.DIMIT)).setCid(cid).setCt(ct).setMt(ct).setHost(pidHost[1])
                .setIpv4("").setIpv6("").setName(dimit.conf.getName()).setPid(pidHost[0]).setRole(DimitRole.PARTNER).setV(Const.V)
                .setStatus(DimitStatus.ONLINE).build(); // TODO IP
        dimit.store = store;

        // create store/dimitConf/dimit
        DimitPath dimitPath = dss.getPath(StoreConst.PATH_STORE, dimit.conf.getId(), dimit.store.getId());
        dimitPath = dss.<Dimit> io().write(dimitPath, store, StoreAttribute.EPHEMERAL);
        LOG.info("create EPHEMERAL dimit {}", dimitPath);

        return dimit;
    }

    /**
     * 
     * @param cid
     *            ChannelGroupConf's id
     */
    public void newChannelGroup(String cid) throws IOException {
        ChannelGroupWrapper group = ChannelGroupWrapper.init(this.dimiter, cid);
        if (group == null) { throw new IOException("Not found ChannelGroupConf:" + cid); }

        group().add(group);
    }

    /**
     * 
     * @param id
     * @return
     */
    public boolean containGroup(String id) {
        for (ChannelGroupWrapper g : group) {
            if (g.id().equals(id)) return true;
        }
        return false;
    }

    public ChannelGroupWrapper group(String id) {
        for (ChannelGroupWrapper g : group) {
            if (g.id().equals(id)) return g;
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        for (ChannelGroupWrapper g : group) {
            try {
                g.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public List<ChannelGroupWrapper> group() {
        return group;
    }

    public void addChannelGroup(ChannelGroupWrapper g) {
        this.group.add(g);
    }

    @Override
    public Dimit store() {
        return store;
    }

    @Override
    public DimitConf conf() {
        return conf;
    }

    @Override
    public String name() {
        return conf.getName();
    }

    @Override
    public String id() {
        return store.getId();
    }

    public Dimiter dimiter() {
        return this.dimiter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof DimitWrapper) { return ((DimitWrapper) obj).id().equals(id()); }
        return false;
    }

    @Override
    public void handle(WatchKey key, WatchEvent<?> event) {
        // TODO Auto-generated method stub
    }

}
