package dimit.store.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ThreadLocalRandom;

import dimit.store.conf.MagicFlag;
import dimit.store.sys.Const;

/**
 * @author dzh
 * @date Apr 2, 2018 4:30:17 PM
 * @version 0.0.1
 */
public class IDUtil {

    public static final String storeID(int version, MagicFlag mf) {
        // version 2 hex
        String v = Integer.toHexString(version); //
        if (v.length() == 1) v = "0" + v;
        // MagicFlag 2 hex
        String f = Integer.toHexString(mf.getNumber());
        if (f.length() == 1) f = "0" + f;

        String ts = Long.toHexString(System.currentTimeMillis());
        String hash = Integer.toHexString(ManagementFactory.getRuntimeMXBean().getName().hashCode());
        int ranlen = 32 - ts.length() - hash.length();

        StringBuilder buf = new StringBuilder(32);
        buf.append(v);
        buf.append(f);
        buf.append(ts);
        // buf.append("-");
        buf.append(hash);
        // buf.append("-");
        for (int i = 0; i < ranlen; i++) {
            buf.append(Integer.toHexString(ThreadLocalRandom.current().nextInt(0, 16)));
        }
        return buf.toString();
    }

    public static final String storeID(MagicFlag mf) {
        return storeID(Const.V, mf);
    }

    public static final MagicFlag toMagicFlag(String storeID) {
        // TODO validate storeID

        // String v = storeID.substring(0, 2);
        String f = storeID.charAt(2) == '0' ? storeID.substring(3, 4) : storeID.substring(2, 4);

        return MagicFlag.forNumber(Integer.parseInt(f, 16));
    }

    public static final String uuid3() {
        String ts = Long.toHexString(System.currentTimeMillis());
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
        return buf.toString();
    }

}
