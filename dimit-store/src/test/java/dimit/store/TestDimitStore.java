package dimit.store;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;

import dimit.store.conf.DimitConf;
import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;
import dimit.store.sys.io.ProtoStoreIO;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Apr 2, 2018 4:21:49 PM
 * @version 0.0.1
 */
public class TestDimitStore {

    static Logger LOG = LoggerFactory.getLogger(TestDimitStore.class);

    @Test
    public void testID() {
        LOG.info("{}", ManagementFactory.getRuntimeMXBean().getName());
    }

    @Test
    public void testProtoBytes() throws IOException {
        Message dimit = DimitConf.newBuilder().setCt(System.currentTimeMillis()).setMt(System.currentTimeMillis())
                .setId(IDUtil.storeID(Const.V, MagicFlag.DIMIT_CONF)).setV(Const.V).setName("voice").build();
        String id = dimit.getField(dimit.getDescriptorForType().findFieldByName("id")).toString();
        LOG.info("v {}", id);
        LOG.info("mf {}", IDUtil.toMagicFlag(id));

        DimitConf dimitConf = (DimitConf) ProtoStoreIO.read(dimit.toByteArray(), DimitConf.class);
        id = dimitConf.getId();
        LOG.info("v {}", id);
        LOG.info("mf {}", IDUtil.toMagicFlag(id));

    }

}
