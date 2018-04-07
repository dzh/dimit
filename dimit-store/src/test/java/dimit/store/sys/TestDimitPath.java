package dimit.store.sys;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dzh
 * @date Mar 26, 2018 5:12:59 PM
 * @version 0.0.1
 */
public class TestDimitPath {

    static Logger LOG = LoggerFactory.getLogger(TestDimitPath.class);

    @Test
    public void testPath() {
        Path p = Paths.get("a", "b", "c");
        LOG.info(p.toString());
        LOG.info("" + p.getNameCount());
        LOG.info("" + p.getName(1));

        URI uri = URI.create("http://google.com/u/index.html?host=localhost:2181");
        LOG.info(uri.getHost());
        LOG.info(uri.getPath());
        LOG.info(uri.getQuery());
    }

}
