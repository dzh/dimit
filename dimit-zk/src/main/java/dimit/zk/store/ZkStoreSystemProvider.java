package dimit.zk.store;

import java.util.Map;

import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.DimitStoreSystemProvider;

/**
 * @author dzh
 * @date Mar 26, 2018 3:55:53 PM
 * @version 0.0.1
 */
public class ZkStoreSystemProvider extends DimitStoreSystemProvider {

    @Override
    protected String storeType() {
        return "zk";
    }

    @Override
    public DimitStoreSystem createStoreSystem(Map<String, Object> env) {
        return new ZkStoreSystem(this, env);
    }

}
