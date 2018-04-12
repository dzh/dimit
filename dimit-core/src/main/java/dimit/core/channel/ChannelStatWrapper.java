package dimit.core.channel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.StoreConst;
import dimit.store.ChannelStat;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.StoreAttribute;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Apr 10, 2018 5:29:08 PM
 * @version 0.0.1
 */
public class ChannelStatWrapper extends StatWrapper<ChannelStat> {

    static Logger LOG = LoggerFactory.getLogger(ChannelStatWrapper.class);

    private AtomicLong count;
    private AtomicLong time;
    private AtomicLong succCount;
    private AtomicLong succTime;

    private ChannelWrapper channel;

    private ChannelStat[] ringBuf;

    private volatile int ringIdx = 0; // only SnapshotThread update value

    protected ChannelStatWrapper(String id, ChannelStat stat) {
        super(id, stat);
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        // ringBuf = null;
    }

    public static ChannelStatWrapper init(ChannelWrapper channel) {
        long ct = System.currentTimeMillis();
        ChannelStat stat = ChannelStat.newBuilder().setAvgTime(-1).setChannel(channel.id()).setCount(0).setCt(ct)
                .setId(IDUtil.storeID(MagicFlag.CHANNEL_STAT)).setMt(ct).setSuccCount(0).setSuccRate(-1).setSuccTime(0).setTime(0)
                .setTps(-1).setV(Const.V).build();

        ChannelStatWrapper statWrapper = new ChannelStatWrapper(stat.getId(), stat);
        statWrapper.channel = channel;
        statWrapper.count = new AtomicLong(stat.getCount());
        statWrapper.time = new AtomicLong(stat.getTime());
        statWrapper.succCount = new AtomicLong(stat.getSuccCount());
        statWrapper.succTime = new AtomicLong(stat.getSuccTime());

        statWrapper.ringBuf = new ChannelStat[5]; // TODO config buffer size
        return statWrapper;
    }

    public long incrCount() {
        return count.incrementAndGet();
    }

    public long count() {
        return count.get();
    }

    public long addTime(long delta) {
        return time.addAndGet(delta);
    }

    public long time() {
        return time.get();
    }

    public long incrSuccCount() {
        return succCount.incrementAndGet();
    }

    public long addSuccTime(long delta) {
        return succTime.addAndGet(delta);
    }

    public long succTime() {
        return succTime.get();
    }

    public long succCount() {
        return succCount.get();
    }

    /**
     * @return 0 if 正常的发送 or 负值 if 异常值
     */
    public int calcPriority() { // TODO conf
        int priority = 0;

        ChannelStat stat = ringBuf[preIndex(ringIdx)];
        if (stat != null) {
            double tps = stat.getTps();
            if (tps > 0) {
                double avgTime = stat.getAvgTime(); // ms
                if (avgTime > 100) priority -= 1;
                if (avgTime > 1000) priority -= 1;
                if (avgTime > 3000) priority -= 2;
                if (avgTime > 5000) priority -= 6;
            }

            double succRate = stat.getSuccRate();
            if (succRate > 0) {
                succRate *= 100;
                if (succRate < 90) priority -= 1;
                if (succRate < 70) priority -= 1;
                if (succRate < 50) priority -= 2;
                if (succRate < 30) priority -= 6;
            }
        }
        LOG.debug("calcPriority {} {}", channel.id(), priority);
        return priority;
    }

    /**
     * NotThreadSafe
     * 
     */
    public void snapshot() {
        long ct = System.currentTimeMillis();
        long count = count();
        long time = time();
        long succCount = succCount();
        long succTime = succTime();

        // snapshot
        ChannelStat stat = ChannelStat.newBuilder().setAvgTime(0).setChannel(channel.id()).setCount(count).setCt(ct).setAvgTime(-1)
                .setId(IDUtil.storeID(MagicFlag.CHANNEL_STAT)).setMt(ct).setSuccCount(succCount).setSuccRate(-1).setSuccTime(succTime)
                .setTime(time).setTps(-1).setV(Const.V).build();

        ChannelStat preStat = ringBuf[preIndex(ringIdx)];
        ringBuf[ringIdx] = calcIntervalMean(preStat, stat); // save
        ringIdx = nextIndex(ringIdx);  // update ringIdx
    }

    // TODO
    public void writeChannelStat() throws IOException {
        ChannelStat stat = ringBuf[preIndex(ringIdx)];
        if (stat != null) {
            // write store/cid/stat*/channel
            DimitStoreSystem dss = channel.dimiter().storeSystem();
            DimitPath statPath = dss.getPath(StoreConst.PATH_STORE, channel.conf().getId(), "stat" + channel.store().getType().getNumber(),
                    channel.id());
            dss.<ChannelStat> io().write(statPath, stat, StoreAttribute.EPHEMERAL);
            LOG.info("create EPHEMERAL stat {}", statPath);
        }
    }

    /**
     * double tps = 8;
     * double avgTime = 11;
     * double succRate = 12;
     * 
     * @param preStat
     * @param stat
     */
    private ChannelStat calcIntervalMean(ChannelStat preStat, ChannelStat stat) {
        if (preStat == null) return stat;

        long intervalMs = stat.getCt() - preStat.getCt();
        long intervalCount = stat.getCount() - preStat.getCount();

        // IntervalMean
        double tps = intervalCount / (intervalMs / 1000.0);

        double avgTime = -1;
        if (intervalCount > 0) {
            avgTime = (stat.getTime() - preStat.getTime()) * 1.0 / intervalCount;
        }

        double succRate = -1;
        if (stat.getSuccCount() > 0) {
            succRate = (stat.getSuccCount() - preStat.getSuccCount()) * 1.0 / intervalCount;
        }

        // LOG.info("{}/{}={} {}/{}={} {}", intervalCount, intervalMs, tps, (stat.getTime() - preStat.getTime()),
        // intervalCount, avgTime,
        // succRate);

        long mt = System.currentTimeMillis();
        return stat.toBuilder().setTps(tps).setAvgTime(avgTime).setSuccRate(succRate).setMt(mt).build();
    }

    private int preIndex(int idx) {
        if (--idx >= 0) return idx;
        return ringBuf.length - 1;
    }

    private int nextIndex(int idx) {
        int size = ringBuf.length;
        if (++idx >= size) return 0;
        return idx;
    }

    @Override
    public String toString() {
        ChannelStat stat = ringBuf[preIndex(ringIdx)];
        return ringBuf.length + "_" + ringIdx + (stat == null ? "" : "_" + stat.toString());
    }

    /**
     * for test
     * 
     * @return
     */
    @Deprecated
    public ChannelStat current() {
        return ringBuf[preIndex(ringIdx)];
    }

}
