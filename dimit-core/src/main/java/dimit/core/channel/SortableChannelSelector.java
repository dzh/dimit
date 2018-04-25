package dimit.core.channel;

import dimit.store.ChannelType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * created by jiangt on 2018/04/24
 */
public class SortableChannelSelector extends ChannelSelector {

  public SortableChannelSelector(ChannelGroupWrapper group) {
    super(group);
  }

  @Override
  List<ChannelWrapper> select(ChannelType type, String... tags) {
    return select(type, tags, null);
  }

  @Override
  List<ChannelWrapper> select(ChannelType type, String[] limitTag, String[] sortTag) {
    TreeSet<ChannelWrapper> filtered = new TreeSet<>(new SortTagsComparator(sortTag));
    List<ChannelWrapper> chans = group().channel();
    for (ChannelWrapper channel : chans) {
      if (channel.store().getType().getNumber() != type.getNumber()) {
        continue;
      }
      if (!channel.isValid())
        continue;

      if (!channel.cantainTags(limitTag))
        continue;

      filtered.add(channel);
    }
    return new ArrayList<>(filtered);
  }

  @Override
  public void close() throws IOException {

  }

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
            return 1;
          }
          if (!o1.cantainTags(tag) && o2.cantainTags(tag)) {
            return -1;
          }
        }
      }
      int p = o1.priority() - o2.priority();
      if (p == 0) {
        p = (int) (o1.tps() - o2.tps());
      }
      return p == 0 ? o1.id().compareTo(o2.id()) : p;
    }
  }

}
