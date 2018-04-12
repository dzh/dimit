package dimit.core.channel;

import java.util.concurrent.Callable;

/**
 * 增加更多的统计、控制功能
 * 
 * @author dzh
 * @date Apr 7, 2018 3:30:50 PM
 * @version 0.0.1
 */
public interface ChannelCallable<V> extends Callable<V> {

    int CODE_FATAL = -1; // 严重的错误,通道直接
    int CODE_SUCC = 0;

    /**
     * @param v
     *            called result
     * @return 0 if called successfully
     */
    int code(V v);

}
