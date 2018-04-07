package dimit.store.sys.event;

import java.nio.file.WatchEvent.Kind;

import dimit.store.sys.DimitPath;

/**
 * @author dzh
 * @date Apr 3, 2018 7:19:27 PM
 * @version 0.0.1
 */
public class StoreEventKind implements Kind<DimitPath> {

    private final String name;
    private final Class<DimitPath> type;

    public StoreEventKind(String name, Class<DimitPath> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<DimitPath> type() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof StoreEventKind) { return ((StoreEventKind) obj).name().equals(name()); }

        return false;
    }

    public static final StoreEventKind ADD = new StoreEventKind("ADD", DimitPath.class);
    public static final StoreEventKind DELETE = new StoreEventKind("DELETEE", DimitPath.class);
    public static final StoreEventKind UPDATE = new StoreEventKind("UPDATE", DimitPath.class);

    public static final StoreEventKind CHILDREN = new StoreEventKind("CHILDREN", DimitPath.class);
    public static final StoreEventKind CHILD_ADD = new StoreEventKind("CHILD_ADD", DimitPath.class);
    public static final StoreEventKind CHILD_DELETE = new StoreEventKind("CHILD_DELETE", DimitPath.class);
    public static final StoreEventKind CHILD_UPDATE = new StoreEventKind("CHILD_UPDATE", DimitPath.class);

}
