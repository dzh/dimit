package dimit.store.sys;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dzh
 * @date Mar 30, 2018 3:33:26 PM
 * @version 0.0.1
 */
public class TestLocalStoreSystem {

    static Logger LOG = LoggerFactory.getLogger(TestLocalStoreSystem.class);

    DimitStoreSystem lss;

    @Before
    public void init() throws Exception {
        Map<String, Object> env = new HashMap<>();

        lss = DimitStores.newStoreSystem(URI.create("dimit-local://local/dimit"), env);
    }

    @Test
    public void testSysStat() {
        LOG.info("scheme:{} domain:{} root:{} open:{}", lss.provider().getScheme(), lss.getDomain(), lss.getRoot(), lss.isOpen());
    }

    @After
    public void close() {
        try {
            if (lss != null) lss.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
