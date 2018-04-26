package dimit.core.channel;

import dimit.store.ChannelType;

/**
 * created by jiangt on 2018/04/25
 */
public abstract class ChannelSelectQuery {

  private ChannelType channelType;


  public ChannelSelectQuery(ChannelType channelType) {
    this.channelType = channelType;
  }

  abstract String[] buildQuery();

  public ChannelType getChannelType() {
    return channelType;
  }

  public void setChannelType(ChannelType channelType) {
    this.channelType = channelType;
  }


}
