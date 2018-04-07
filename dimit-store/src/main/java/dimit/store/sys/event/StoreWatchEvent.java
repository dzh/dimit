package dimit.store.sys.event;

import java.nio.file.WatchEvent;

import dimit.store.sys.DimitPath;

/**
 * @author dzh
 * @date Apr 3, 2018 6:44:10 PM
 * @version 0.0.1
 */
public class StoreWatchEvent implements WatchEvent<DimitPath> {

    private StoreEventKind kind;
    private int count = 1;
    private DimitPath context;

    public StoreWatchEvent(StoreEventKind kind, DimitPath context, int count) {
        this.kind = kind;
        this.count = count;
        this.context = context;
    }

    public StoreWatchEvent(StoreEventKind kind, DimitPath context) {
        this.kind = kind;
        this.context = context;
    }

    @Override
    public Kind<DimitPath> kind() {
        return kind;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public DimitPath context() {
        return context;
    }

    @Override
    public String toString() {
        return kind + "_" + context.getPath() + "_" + count;
    }

}
