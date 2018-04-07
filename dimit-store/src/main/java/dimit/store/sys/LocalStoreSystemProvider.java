package dimit.store.sys;

import java.util.Map;

/**
 * 
 * 
 * @author dzh
 * @date Mar 30, 2018 3:31:05 PM
 * @version 0.0.1
 */
public class LocalStoreSystemProvider extends DimitStoreSystemProvider {

    /*
     * (non-Javadoc)
     * @see dimit.store.sys.DimitStoreSystemProvider#storeType()
     */
    @Override
    protected String storeType() {
        return "local";
    }

    /*
     * (non-Javadoc)
     * @see dimit.store.sys.DimitStoreSystemProvider#createStoreSystem(java.util.Map)
     */
    @Override
    public DimitStoreSystem createStoreSystem(Map<String, Object> env) {
        return new LocalStoreSystem(this, env);
    }

}
