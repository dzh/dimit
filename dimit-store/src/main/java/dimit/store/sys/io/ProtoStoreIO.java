package dimit.store.sys.io;

import java.io.IOException;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;

import dimit.store.Channel;
import dimit.store.ChannelGroup;
import dimit.store.ChannelRecvStat;
import dimit.store.ChannelSendStat;
import dimit.store.ChannelTotalStat;
import dimit.store.Dimit;
import dimit.store.conf.ChannelConf;
import dimit.store.conf.ChannelGroupConf;
import dimit.store.conf.DimitConf;
import dimit.store.conf.MagicFlag;
import dimit.store.util.IDUtil;

/**
 * @author dzh
 * @date Apr 3, 2018 1:26:45 AM
 * @version 0.0.1
 */
public abstract class ProtoStoreIO implements StoreIO<Message> {

    /**
     * 
     * @param data
     *            store bytes
     * @return
     * @throws IOException
     */
    public static final <T> Message read(byte[] data, Class<T> clazz) throws IOException {
        CodedInputStream input = CodedInputStream.newInstance(data);
        if (clazz == null) {  // readby
            // v
            input.readTag();
            input.readUInt32(); // TODO
            // id
            input.readTag();
            String id = input.readString();

            MagicFlag mf = IDUtil.toMagicFlag(id);
            switch (mf.getNumber()) {
            // conf
            case MagicFlag.DIMIT_CONF_VALUE:
                return DimitConf.parseFrom(data);
            case MagicFlag.CHANNEL_GROUP_CONF_VALUE:
                return ChannelGroupConf.parseFrom(data);
            case MagicFlag.CHANNEL_CONF_VALUE:
                return ChannelConf.parseFrom(data);
            // store
            case MagicFlag.DIMIT_VALUE:
                return Dimit.parseFrom(data);
            case MagicFlag.CHANNEL_GROUP_VALUE:
                return ChannelGroup.parseFrom(data);
            case MagicFlag.CHANNEL_VALUE:
                return Channel.parseFrom(data);
            case MagicFlag.CHANNEL_SEND_STAT_VALUE:
                return ChannelSendStat.parseFrom(data);
            case MagicFlag.CHANNEL_RECV_STAT_VALUE:
                return ChannelRecvStat.parseFrom(data);
            case MagicFlag.CHANNEL_TOTAL_STAT_VALUE:
                return ChannelTotalStat.parseFrom(data);
            default:
                throw new IOException("Not found MagicFlag from id:" + id);
            }
        } else {
            if (clazz.equals(DimitConf.class)) {
                return DimitConf.parseFrom(data);
            } else if (clazz.equals(ChannelGroupConf.class)) {
                return ChannelGroupConf.parseFrom(data);
            } else if (clazz.equals(ChannelConf.class)) {
                return ChannelConf.parseFrom(data);
            } else if (clazz.equals(Dimit.class)) {
                return Dimit.parseFrom(data);
            } else if (clazz.equals(ChannelGroup.class)) {
                return ChannelGroup.parseFrom(data);
            } else if (clazz.equals(Channel.class)) {
                return Channel.parseFrom(data);
            } else if (clazz.equals(ChannelSendStat.class)) {
                return ChannelSendStat.parseFrom(data);
            } else if (clazz.equals(ChannelRecvStat.class)) {
                return ChannelRecvStat.parseFrom(data);
            } else if (clazz.equals(ChannelTotalStat.class)) {
                return ChannelTotalStat.parseFrom(data);
            } else {
                throw new IOException("Not found clazz:" + clazz.getName());
            }
        }

    }

}
