package dimit.core.util;

import java.lang.management.ManagementFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dzh
 * @date Apr 7, 2018 2:40:41 PM
 * @version 0.0.1
 */
public class TestDimiterUtil {

    static Logger LOG = LoggerFactory.getLogger(TestDimiterUtil.class);

    @Test
    public void testHost() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int loc = name.indexOf('@');
        LOG.info("{} pid-{} host-{}", name, name.substring(0, loc), name.substring(loc + 1));
    }
    

}
