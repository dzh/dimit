package dimit.core.channel;

import java.io.Closeable;

/**
 * @author dzh
 * @date Apr 10, 2018 5:25:00 PM
 * @version 0.0.1
 */
public abstract class StatWrapper<T> implements Closeable {

    private String id;

    private T stat;

    protected StatWrapper(String id, T stat) {
        this.id = id;
        this.stat = stat;
    }

    public String id() {
        return this.id;
    }

    public T stat() {
        return this.stat;
    }

}
