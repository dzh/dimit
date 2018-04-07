package dimit.zk.store;

import org.apache.zookeeper.CreateMode;

import dimit.store.sys.StoreAttribute;

/**
 * @author dzh
 * @date Apr 4, 2018 11:04:10 AM
 * @version 0.0.1
 */
@Deprecated
public class ZkStoreAttributes {

    public static final StoreAttribute<CreateMode> PERSISTENT = new StoreAttribute<>("PERSISTENT", CreateMode.PERSISTENT);
    public static final StoreAttribute<CreateMode> PERSISTENT_SEQUENTIAL =
            new StoreAttribute<>("PERSISTENT_SEQUENTIAL", CreateMode.PERSISTENT_SEQUENTIAL);

    public static final StoreAttribute<CreateMode> EPHEMERAL = new StoreAttribute<>("EPHEMERAL", CreateMode.EPHEMERAL);
    public static final StoreAttribute<CreateMode> EPHEMERAL_SEQUENTIAL =
            new StoreAttribute<>("EPHEMERAL_SEQUENTIAL", CreateMode.EPHEMERAL_SEQUENTIAL);

}
