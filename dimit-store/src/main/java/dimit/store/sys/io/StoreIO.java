package dimit.store.sys.io;

import java.io.Closeable;
import java.io.IOException;

import dimit.store.sys.DimitPath;
import dimit.store.sys.StoreAttribute;

/**
 * @author dzh
 * @date Apr 3, 2018 1:23:08 AM
 * @version 0.0.1
 */
public interface StoreIO<T> extends Closeable {

    T read(DimitPath path, Class<T> clazz) throws IOException;

    /**
     * 
     * @param path
     * @param store
     * @param attributes
     * @return the absolute path after written successfully or null if failure
     */
    DimitPath write(DimitPath path, T store, StoreAttribute<?>... attributes) throws IOException;

}
