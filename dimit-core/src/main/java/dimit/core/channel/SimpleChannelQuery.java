package dimit.core.channel;

import dimit.store.ChannelType;

/**
 * created by jiangt on 2018/04/25
 */
public class SimpleChannelQuery extends ChannelSelectQuery {

  private String[] tags;

  private SimpleChannelQuery(ChannelType channelType) {
    super(channelType);
  }

  public SimpleChannelQuery(String[] tags) {
    this(ChannelType.SEND);
    this.tags = tags;
  }

  @Override
  String[] buildQuery() {
    return tags;
  }
}
