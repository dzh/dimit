package dimit.zk.store;

import java.io.IOException;
import java.net.URI;
import java.nio.file.WatchEvent.Kind;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.DimitStoreSystemProvider;
import dimit.store.sys.StoreAttribute;
import dimit.store.sys.event.StoreEventKind;
import dimit.store.sys.event.StoreWatchKey;
import dimit.store.sys.event.StoreWatcher;
import dimit.store.util.URIUtil;
import dimit.zk.ZkConst;

/**
 * @author dzh
 * @date Mar 24, 2018 6:12:35 PM
 * @version 0.0.1
 */
public class ZkStoreSystem extends DimitStoreSystem {

    static Logger LOG = LoggerFactory.getLogger(ZkStoreSystem.class);

    private CuratorFramework zkCli;

    public ZkStoreSystem(DimitStoreSystemProvider provider, Map<String, Object> env) {
        super(provider, env);
    }

    /**
     * uri: dimit-zk://domain/path?host=connectString&retry=3&sleep=2000
     */
    @Override
    protected boolean connect(URI uri) throws IOException {
        Map<String, String> query;
        try {
            query = URIUtil.getQuery(uri);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        String connectString = query.get(ZkConst.URI_QUERY_HOST);
        if (invalidHost(connectString)) throw new IllegalArgumentException("Invalid connectString:" + connectString);

        int sleep = 2000;
        if (query.containsKey(ZkConst.URI_QUERY_SLEEP)) sleep = Integer.parseInt(query.get(ZkConst.URI_QUERY_SLEEP).trim());

        int retry = 3;
        if (query.containsKey(ZkConst.URI_QUERY_RETRY)) retry = Integer.parseInt(query.get(ZkConst.URI_QUERY_RETRY).trim());

        String ns = query.get(ZkConst.URI_QUERY_NAMESPACE);

        int connectionTimeoutMs = 3000;
        if (query.containsKey(ZkConst.URI_QUERY_CONNECT_TIMEOUT))
            retry = Integer.parseInt(query.get(ZkConst.URI_QUERY_CONNECT_TIMEOUT).trim());

        int sessionTimeoutMs = 6000;
        if (query.containsKey(ZkConst.URI_QUERY_SESSION_TIMEOUT))
            retry = Integer.parseInt(query.get(ZkConst.URI_QUERY_SESSION_TIMEOUT).trim());

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(sleep, retry);
        zkCli = CuratorFrameworkFactory.builder().connectString(connectString).retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs).sessionTimeoutMs(sessionTimeoutMs).namespace(ns).build();
        zkCli.start();
        return true;
    }

    private boolean invalidHost(String connectString) {
        // TODO
        return false;
    }

    public CuratorFramework zkCli() {
        return this.zkCli;
    }

    @Override
    protected void dispose() throws IOException {
        zkCli.close();
    }

    @Override
    public byte[] read(DimitPath path) throws IOException {
        // if (!isOpen()) throw new IllegalStateException(this.toString() + " closed!");

        try {
            return zkCli.getData().forPath(path.toAbsolutePath().getPath());
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public DimitPath write(DimitPath path, byte[] data, StoreAttribute<?>... attributes) throws IOException {
        // if (!isOpen()) throw new IllegalStateException(this.toString() + " closed!");

        CreateMode mode = CreateMode.PERSISTENT;
        if (attributes != null) {
            for (StoreAttribute<?> attr : attributes) {
                if (attr.equals(StoreAttribute.PERSISTENT_SEQUENTIAL)) {
                    mode = CreateMode.PERSISTENT_SEQUENTIAL;
                } else if (attr.equals(StoreAttribute.EPHEMERAL)) {
                    mode = CreateMode.EPHEMERAL;
                } else if (attr.equals(StoreAttribute.EPHEMERAL_SEQUENTIAL)) {
                    mode = CreateMode.EPHEMERAL_SEQUENTIAL;
                }
            }
        }

        String absulatePath = path.toAbsolutePath().getPath();
        try {
            if (zkCli.checkExists().forPath(absulatePath) == null) {
                String p = zkCli.create().creatingParentsIfNeeded().withMode(mode).forPath(absulatePath, data);
                return path.getStoreSystem().getPath(p);
            } else {
                zkCli.setData().forPath(absulatePath, data);
                return path.getStoreSystem().getPath(absulatePath);
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e.getCause());
        }

    }

    @Override
    public List<StoreWatcher> newStoreWatcher(StoreWatchKey key) {
        Kind<?>[] kinds = key.getKinds();
        if (kinds == null) return Collections.emptyList();

        List<StoreWatcher> list = new LinkedList<>();
        for (Kind<?> k : kinds) {
            if (k.equals(StoreEventKind.UPDATE)) {
                StoreWatcher watch = new NodeCacheWatcher(this, key);
                watch.run();
                list.add(watch);
            } else if (k.equals(StoreEventKind.CHILDREN)) {
                StoreWatcher watch = new PathChildrenWatcher(this, key);
                watch.run();
                list.add(watch);
            } else {
                LOG.warn("Not support {}", k);
            }
        }
        return list;
    }

    @Override
    public List<String> children(DimitPath path) throws IOException {
        try {
            return zkCli.getChildren().forPath(path.toAbsolutePath().getPath());
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

}
