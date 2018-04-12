package dimit.core.channel;

import java.io.IOException;

import dimit.store.ChannelTotalStat;

/**
 * @author dzh
 * @date Apr 10, 2018 5:33:06 PM
 * @version 0.0.1
 */
public class ChannelTotalStatWrapper extends StatWrapper<ChannelTotalStat> {

    protected ChannelTotalStatWrapper(String id, ChannelTotalStat stat) {
        super(id, stat);
    }

    @Override
    public void close() throws IOException {

    }

}
