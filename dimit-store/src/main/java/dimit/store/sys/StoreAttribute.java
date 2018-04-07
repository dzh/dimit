package dimit.store.sys;

import java.nio.file.attribute.FileAttribute;

/**
 * @author dzh
 * @date Apr 4, 2018 10:55:32 AM
 * @version 0.0.1
 */
public class StoreAttribute<T> implements FileAttribute<T> {

    private String name;
    private T value;

    public StoreAttribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public StoreAttribute(String name) {
        this(name, null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof StoreAttribute<?>) { return ((StoreAttribute<?>) obj).name().equals(name()); }

        return false;
    }

    @Override
    public String toString() {
        return name + "_" + value;
    }

    public static final StoreAttribute<String> PERSISTENT = new StoreAttribute<>("PERSISTENT");
    public static final StoreAttribute<String> PERSISTENT_SEQUENTIAL = new StoreAttribute<>("PERSISTENT_SEQUENTIAL");

    public static final StoreAttribute<String> EPHEMERAL = new StoreAttribute<>("EPHEMERAL");
    public static final StoreAttribute<String> EPHEMERAL_SEQUENTIAL = new StoreAttribute<>("EPHEMERAL_SEQUENTIAL");

}
