package dimit.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.channel.ChannelWrapper;

/**
 * 
 * @author dzh
 * @date Apr 12, 2018 12:36:26 PM
 * @version 0.0.1
 */
class ChannelStatWorker implements Closeable {

    static Logger LOG = LoggerFactory.getLogger(ChannelStatWorker.class);

    private List<ChannelWrapper> channels;

    // private ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile boolean closed = false;

    private long snapshotMs = 1000L;
    private long syncMs = 3000L;

    private String name;

    ChannelStatWorker() {
        this(1000L, 3000L);
    }

    ChannelStatWorker(long snapshotMs, long syncMs) {
        this(ChannelStatWorker.class.getSimpleName(), snapshotMs, syncMs);
    }

    ChannelStatWorker(String name, long snapshotMs, long syncMs) {
        this.name = name;
        this.channels = Collections.synchronizedList(new LinkedList<ChannelWrapper>());

        if (snapshotMs <= 0) snapshotMs = 1000L; // default
        if (syncMs <= 0) syncMs = 3000L;

        this.snapshotMs = snapshotMs;
        this.syncMs = syncMs;
    }

    public String name() {
        return this.name;
    }

    public void start() {
        LOG.info("{} start snapshot-{} sync-{}", name, snapshotMs, syncMs);
        SnapshotThread snapshotT = new SnapshotThread(name);
        snapshotT.start();

        SyncThread syncT = new SyncThread(name);
        syncT.start();
    }

    public void addChannel(ChannelWrapper ch) {
        if (closed) return;
        channels.add(ch);
    }

    @Override
    public void close() throws IOException {
        closed = true;
        channels.clear();
        LOG.info("{} closed", name);
    }

    class SnapshotThread extends Thread {
        SnapshotThread(String name) {
            setName(name + "-snapshot");
            setDaemon(true);
        }

        @Override
        public void run() {
            LOG.info("{} start", getName());
            while (true) {
                if (closed) break;

                try {
                    List<ChannelWrapper> list = ChannelStatWorker.this.channels;
                    if (list != null && !list.isEmpty()) {
                        for (ChannelWrapper ch : list) {
                            if (ch.isValid()) ch.stat().snapshot(); // only valid channel
                        }
                    }

                    Thread.sleep(ChannelStatWorker.this.snapshotMs); // sleep
                } catch (InterruptedException e) {
                    LOG.warn(e.getMessage(), e); // TODO
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            LOG.info("{} closed", getName());
        }
    }

    class SyncThread extends Thread {

        SyncThread(String name) {
            setName(name + "-sync");
            setDaemon(true);
        }

        @Override
        public void run() {
            LOG.info("{} start", getName());
            while (true) {
                if (closed) break;

                try {
                    List<ChannelWrapper> list = ChannelStatWorker.this.channels;
                    if (list != null && !list.isEmpty()) {
                        for (ChannelWrapper ch : list) {
                            if (ch.isValid()) ch.stat().writeChannelStat(); // only valid channel
                        }
                    }

                    Thread.sleep(ChannelStatWorker.this.syncMs); // sleep
                } catch (InterruptedException e) {
                    LOG.warn(e.getMessage(), e); // TODO
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            LOG.info("{} closed", getName());
        }
    }

}
