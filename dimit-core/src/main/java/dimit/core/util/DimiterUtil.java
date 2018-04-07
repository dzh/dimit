package dimit.core.util;

import java.lang.management.ManagementFactory;

/**
 * @author dzh
 * @date Apr 4, 2018 5:26:05 PM
 * @version 0.0.1
 */
public class DimiterUtil {

    public static final String[] pidAndHost() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int loc = name.indexOf('@');
        return new String[] { name, name.substring(0, loc), name.substring(loc + 1) };
    }

}
