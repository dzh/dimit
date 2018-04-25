package dimit.demo.dimiter;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.core.Dimiter;
import dimit.core.channel.ChannelGroupWrapper;
import dimit.core.channel.DimitWrapper;

/**
 * @author dzh
 * @date Apr 9, 2018 11:21:55 AM
 * @version 0.0.1
 */
public class DimiterDemo implements Closeable {

    static Logger LOG = LoggerFactory.getLogger(DimiterDemo.class);

    private Dimiter dimiter;

    public DimiterDemo(String uri, Map<String, Object> env, String dimitConfId) {
        try {
            dimiter = Dimiter.newDimiter(URI.create(uri), env, dimitConfId);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public Dimiter dimiter() {
        return this.dimiter;
    }

    public ChannelGroupWrapper initChannelGroup(String gid) throws IOException {
        DimitWrapper dimit = dimiter.dimit();
        return dimit.newChannelGroup(gid);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

    @Override
    public void close() throws IOException {
        dimiter.close();
    }

}
