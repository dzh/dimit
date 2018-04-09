package dimit.core.channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dimit.store.ChannelType;
import dimit.store.conf.ChannelStatus;

/**
 * @author dzh
 * @date Apr 4, 2018 12:29:21 PM
 * @version 0.0.1
 */
public class SimpleChannelSelector extends ChannelSelector {

    static Logger LOG = LoggerFactory.getLogger(SimpleChannelSelector.class);

    public SimpleChannelSelector(ChannelGroupWrapper group) {
        super(group);
    }

    /*
     * (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {

    }

    @Override
    List<ChannelWrapper> select(ChannelType type, String... tags) {
        // PRIMARY
        TreeSet<ChannelWrapper> primary = new TreeSet<>(new PriorityComparator());
        // STANDBY
        TreeSet<ChannelWrapper> standby = new TreeSet<>(new PriorityComparator());

        for (ChannelWrapper ch : group().channel()) {
            if (ch.store().getType().getNumber() != type.getNumber())  // only SEND
                continue;
            if (!ch.isValid()) continue;

            if (!ch.cantainTags(tags)) continue;

            ChannelStatus status = ch.conf().getStatus();
            if (ChannelStatus.PRIMARY.equals(status)) {
                primary.add(ch);
            } else if (ChannelStatus.STANDBY.equals(status)) {
                standby.add(ch);
            }
        }

        List<ChannelWrapper> selected = new ArrayList<>(primary.size() + standby.size());
        Iterator<ChannelWrapper> iter = primary.descendingIterator();
        while (iter.hasNext()) {
            selected.add(iter.next());
        }

        iter = standby.descendingIterator();
        while (iter.hasNext()) {
            selected.add(iter.next());
        }

        return selected;
    }

    class PriorityComparator implements Comparator<ChannelWrapper> {

        @Override
        public int compare(ChannelWrapper o1, ChannelWrapper o2) {
            int p = o1.priority() - o2.priority();
            if (p == 0) {
                p = (int) (o1.tps() - o2.tps());
            }
            return p == 0 ? o1.id().compareTo(o2.id()) : p; // TODO
        }

    }

}
