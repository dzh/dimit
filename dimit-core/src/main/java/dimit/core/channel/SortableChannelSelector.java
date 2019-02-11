package dimit.core.channel;

import dimit.store.ChannelType;
import dimit.store.conf.ChannelStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * select channel for channel group with the specified order.
 *
 * This selector will exclude the channel which is not marked with hitTags then sort other channels
 * with tag in sortTags.The sort order will be same as the declare order in sortTags.If channels
 * were marked with same sortTags,the selector will use the default order strategy.
 *
 * @author created by jiangt on 2018/04/24
 * @see dimit.core.channel.SimpleChannelSelector
 * @since 0.0.4
 */
public class SortableChannelSelector extends ChannelSelector {

  public SortableChannelSelector(ChannelGroupWrapper group) {
    super(group);
  }


  @Override
  public List<ChannelWrapper> select(ChannelSelectQuery query) {
    ChannelType type = query.getChannelType();
    String[] tags = query.buildQuery();
    String[] hitTag = tags[0].split(",");
    String[] sortTag = tags[1].split(",");
    TreeSet<ChannelWrapper> filtered = new TreeSet<>(new SortTagsComparator(sortTag));
    List<ChannelWrapper> chans = group().channel();
    for (ChannelWrapper channel : chans) {
      if (channel.store().getType().getNumber() != type.getNumber()) {
        continue;
      }
      if (!channel.isValid())
        continue;

      if (!channel.cantainTags(hitTag))
        continue;

      filtered.add(channel);
    }
    return new ArrayList<>(filtered);
  }

  @Override
  public void close() throws IOException {

  }

  /**
   * declare tag sort order strategy
   */
  class SortTagsComparator implements Comparator<ChannelWrapper> {
    private String[] sortTag;

    public SortTagsComparator(String... sortTag) {
      this.sortTag = sortTag;
    }

    @Override
    public int compare(ChannelWrapper o1, ChannelWrapper o2) {
      if (sortTag != null) {
        for (String tag : sortTag) {
          if (o1.cantainTags(tag) && !o2.cantainTags(tag)) {
            return -1;
          }
          if (!o1.cantainTags(tag) && o2.cantainTags(tag)) {
            return 1;
          }
        }
      }
      ChannelStatus status2 = o2.conf().getStatus();
      ChannelStatus status1 = o1.conf().getStatus();
      if (status1 != status2) {
        return ChannelStatus.PRIMARY == status1 ? -1 : 1;
      }
      int p = o1.priority() - o2.priority();
      if (p == 0) {
        p = (int) (o1.tps() - o2.tps());
      }
      return p == 0 ? o1.id().compareTo(o2.id()) : p;
    }
  }

}
