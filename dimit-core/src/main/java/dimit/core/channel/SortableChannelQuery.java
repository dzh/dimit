package dimit.core.channel;

import dimit.store.ChannelType;

/**
 * created by jiangt on 2018/04/25
 */
public class SortableChannelQuery extends ChannelSelectQuery {

  private String[] hitTags;

  private String[] sortTags;

  public SortableChannelQuery(ChannelType channelType) {
    super(channelType);
  }

  public SortableChannelQuery() {
    this(ChannelType.SEND);
  }

  @Override
  String[] buildQuery() {
    return new String[]{join(hitTags),join(sortTags)};
  }

  public static SortableChannelQuery newQuery() {
    return  new SortableChannelQuery();
  }


  public SortableChannelQuery hits(String... hitTags) {
    this.hitTags = hitTags;
    return this;
  }

  public SortableChannelQuery sort(String... sortTags) {
    this.sortTags = sortTags;
    return this;
  }

  private String join(String... args) {
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      sb.append(arg);
      sb.append(",");
    }
    return sb.substring(0, sb.length() - 1);
  }
}
