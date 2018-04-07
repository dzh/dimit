package dimit.core.channel;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dzh
 * @date Apr 7, 2018 1:20:36 PM
 * @version 0.0.1
 */
public class TestSimpleChannelSelector {

    static Logger LOG = LoggerFactory.getLogger(TestSimpleChannelSelector.class);

    @Test
    public void testPriority() {
        TreeSet<Integer> primary = new TreeSet<>(new PriorityComparator());
        primary.add(ThreadLocalRandom.current().nextInt(100));
        primary.add(ThreadLocalRandom.current().nextInt(100));
        primary.add(ThreadLocalRandom.current().nextInt(100));
        primary.add(ThreadLocalRandom.current().nextInt(100));
        primary.add(ThreadLocalRandom.current().nextInt(100));
        primary.add(ThreadLocalRandom.current().nextInt(100));

        Iterator<Integer> iter = primary.descendingIterator();
        while (iter.hasNext()) {
            LOG.info("{}", iter.next());
        }
    }

    class PriorityComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }

    }

}
