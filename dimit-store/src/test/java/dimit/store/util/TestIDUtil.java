package dimit.store.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;

/**
 * @author dzh
 * @date Apr 3, 2018 10:10:52 AM
 * @version 0.0.1
 */
public class TestIDUtil {

    static final Logger LOG = LoggerFactory.getLogger(TestIDUtil.class);

    @Test
    public void testUuid3() {
        long t = System.currentTimeMillis();
        String ts = Long.toHexString(t);
        LOG.info("{} {} {}", t, ts, Long.parseLong(ts, 16));
        String hash = Integer.toHexString(ManagementFactory.getRuntimeMXBean().getName().hashCode());
        int ranlen = 32 - ts.length() - hash.length();

        StringBuilder buf = new StringBuilder(32);
        buf.append(ts);
        // buf.append("-");
        buf.append(hash);
        // buf.append("-");
        for (int i = 0; i < ranlen; i++) {
            buf.append(Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 16)));
        }
        LOG.info("{} {}", ranlen, buf.toString());

        LOG.info("{}", Integer.toHexString(1));
    }

    @Test
    public void testStoreID() {
        String id = IDUtil.storeID(Const.V, MagicFlag.DIMIT_CONF);
        LOG.info("{} {}", id, IDUtil.toMagicFlag(id));
    }

}
