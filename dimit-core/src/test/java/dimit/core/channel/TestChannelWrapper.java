package dimit.core.channel;

import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.conf.ChannelConf;

/**
 * @author dzh
 * @date Apr 7, 2018 3:02:18 PM
 * @version 0.0.1
 */
public class TestChannelWrapper {

    static Logger LOG = LoggerFactory.getLogger(TestChannelWrapper.class);

    @Test
    public void testContainTags() {
        ChannelConf conf = ChannelConf.newBuilder().addTag("1").addTag("2").build();
        LOG.info("{}", conf.getTagList().contains("2"));
        LOG.info("{}", conf.getTagList().containsAll(Arrays.asList("1", "2")));
        LOG.info("{}", conf.getTagList().containsAll(Arrays.asList("1", "2", "3")));
    }

}
