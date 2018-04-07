package dimit.core.channel;

import java.io.Closeable;

import dimit.core.Dimiter;
import dimit.store.sys.event.EventHandler;

/**
 * @author dzh
 * @date Apr 4, 2018 12:17:07 PM
 * @version 0.0.1
 */
public interface StoreWrapper<S, C> extends Closeable, EventHandler {

    /**
     * store
     * 
     * @return
     */
    S store();

    /**
     * store conf
     * 
     * @return
     */
    C conf();

    /**
     * conf name
     * 
     * @return
     */
    String name();

    /**
     * store id
     * 
     * @return
     */
    String id();

    Dimiter dimiter();

}
