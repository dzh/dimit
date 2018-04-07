package dimit.store.sys.event;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

/**
 * @author dzh
 * @date Apr 7, 2018 12:29:04 PM
 * @version 0.0.1
 */
public interface EventHandler {

    void handle(WatchKey key, WatchEvent<?> event);

}
