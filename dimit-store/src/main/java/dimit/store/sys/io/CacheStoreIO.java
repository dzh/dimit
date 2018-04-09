package dimit.store.sys.io;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Message;

import dimit.store.sys.DimitPath;
import dimit.store.sys.DimitStoreSystem;
import dimit.store.sys.StoreAttribute;

/**
 * @author dzh
 * @date Apr 3, 2018 1:33:56 AM
 * @version 0.0.1
 */
public class CacheStoreIO extends ProtoStoreIO {

    static Logger LOG = LoggerFactory.getLogger(CacheStoreIO.class);

    private Cache<String, Message> msgCache;

    private DimitStoreSystem dss;

    public CacheStoreIO(DimitStoreSystem dss) {
        this.dss = dss;
        this.msgCache = initCache();
    }

    protected Cache<String, Message> initCache() { // TODO config
        return CacheBuilder.newBuilder().concurrencyLevel(8).maximumSize(10000).expireAfterWrite(30, TimeUnit.SECONDS).build();
    }

    /*
     * (non-Javadoc)
     * @see dimit.store.sys.io.StoreIO#read(dimit.store.sys.DimitPath)
     */
    @Override
    public Message read(final DimitPath path, final Class<Message> clazz) throws IOException {
        try {
            return msgCache.get(path.toAbsolutePath().getPath(), new Callable<Message>() {
                @Override
                public Message call() throws Exception {
                    byte[] data = dss.read(path.toAbsolutePath());
                    if (data == null || data.length == 0) throw new IOException("data is null. path:" + path.toString());
                    return ProtoStoreIO.<Message> read(data, clazz);
                }
            });
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    /*
     * (non-Javadoc)
     * @see dimit.store.sys.io.StoreIO#write(dimit.store.sys.DimitPath, java.lang.Object)
     */
    @Override
    public DimitPath write(DimitPath path, Message store, StoreAttribute<?>... attributes) throws IOException {
        // TODO Async

        // try {
        // final byte[] result = new byte[store.getSerializedSize()];
        // final CodedOutputStream output = CodedOutputStream.newInstance(result);
        // writeTo(output);
        // output.checkNoSpaceLeft();
        // return result;
        // } catch (IOException e) {
        // throw new RuntimeException(getSerializingExceptionMessage("byte array"), e);
        // }
        msgCache.invalidate(path.toAbsolutePath().getPath());

        return dss.write(path, store.toByteArray(), attributes);
    }

    @Override
    public void close() throws IOException {
        msgCache.cleanUp();
        dss = null;
    }

}
