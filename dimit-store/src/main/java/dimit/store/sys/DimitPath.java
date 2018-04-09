package dimit.store.sys;

import java.io.IOException;
import java.net.URI;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dimit.store.sys.event.StoreWatchKey;
import dimit.store.sys.event.StoreWatchService;

/**
 * @author dzh
 * @date Mar 24, 2018 6:10:30 PM
 * @version 0.0.1
 */
public class DimitPath implements Watchable {

    private final DimitStoreSystem dss;

    private String path;

    /**
     * @param dfs
     * @param path
     *            absolute path(e.g. /RootPath/a/b/c) or relative path (e.g. a/b/c)
     * @throws IllegalArgumentException
     */
    public DimitPath(DimitStoreSystem dss, String path) {
        if (dss == null) throw new IllegalArgumentException("dss is null");
        this.dss = dss;

        if (path == null || path.length() == 0) { throw new IllegalArgumentException("path is null"); }
        if (path.charAt(0) == dss.provider().pathSeperator()) { // absolute
            this.path = path;
        } else { // relative
            if (path.charAt(path.length() - 1) == dss.provider().pathSeperator()) { // remove tail '/'
                this.path = path.substring(0, path.length() - 1);
            } else {
                this.path = path;
            }
        }
    }

    public DimitPath newPath(String path) {
        if (isAbsolutePath(path)) {
            return new DimitPath(dss, path);
        } else {
            return dss.getPath(getPath(), path);
        }
    }

    public DimitStoreSystem getStoreSystem() {
        return dss;
    }

    public boolean isAbsolute() {
        return isAbsolutePath(path);
    }

    private boolean isAbsolutePath(String path) {
        return path.length() > 0 && path.charAt(0) == dss.provider().pathSeperator();
    }

    public DimitPath getRoot() {
        return dss.getRoot();
    }

    private int lastSeperatorIndex() {
        int lastIndex = -1;
        char sepr = dss.provider().pathSeperator();
        for (int i = path.length() - 1; i > -1; --i) {
            if (path.charAt(i) == sepr) {
                lastIndex = i;
                break;
            }
        }
        return lastIndex;
    }

    public List<DimitPath> children() throws IOException {
        List<String> names = dss.children(this);
        if (names == null || names.isEmpty()) return Collections.emptyList();

        List<DimitPath> children = new ArrayList<>(names.size());
        for (String name : names) {
            children.add(newPath(name));
        }
        return children;
    }

    /**
     * a/b/c -> c
     * 
     * @return last segment string
     */
    public String getPathName() {
        int i = lastSeperatorIndex();
        if (i == -1) return path;

        return path.substring(i + 1);
    }

    // public byte[] getBytes() {
    // return this.path.getBytes(dss.provider().charset());
    // }

    public String getPath() {
        return this.path;
    }

    /**
     * a/b/c -> a/b
     * 
     * @return
     */
    public DimitPath getParent() {
        int i = lastSeperatorIndex();
        if (i == -1 || i == 0) return getRoot();

        return new DimitPath(dss, path.substring(0, i));
    }

    public int getNameCount() {
        char sepr = dss.provider().pathSeperator();
        int count = 0;
        for (int i = 0; i < path.length(); ++i) {
            if (sepr == path.charAt(i)) ++count;
        }
        if (isAbsolute()) {
            return path.length() == 1 ? 0 : count;
        } else {
            return count + 1;
        }
    }

    /**
     * <pre>
     * path: /a/b/c  or  a/b/c
     *        | | |      | | |
     * index: 0 1 2      0 1 2
     * </pre>
     * 
     * @param index
     * @return
     * @throws ArrayIndexOutOfBoundsException
     */
    public String getName(int index) {
        if (index < 0) {
            int count = getNameCount();
            index = count + index;
            if (index < 0) throw new ArrayIndexOutOfBoundsException(index - count);
        }

        String[] names = path.split(String.valueOf(dss.provider().pathSeperator()));
        return names[index];
    }

    public URI toUri() {
        return URI.create(dss.provider().getScheme() + "://" + dss.getDomain() + getRoot().getParent());
    }

    public <T> T toStore(Class<T> clazz) throws IOException {
        return dss.<T> io().read(this, clazz); // TODO
    }

    public boolean startsWith(DimitPath other) {
        return path.startsWith(other.getPath());
    }

    public DimitPath toAbsolutePath() {
        if (isAbsolute()) return this;
        return dss.getPath(dss.getRoot().getPath(), this.path);
    }

    public WatchKey register(WatchService watcher, Kind<?>[] events, Object attachment, Modifier... modifiers) throws IOException {
        if (watcher instanceof StoreWatchService) {
            StoreWatchKey key = new StoreWatchKey((StoreWatchService) watcher, this, attachment, events); // TODO
            return ((StoreWatchService) watcher).newStoreWatcher(key) ? key : null;
        }
        throw new IOException("Unsupported WatchService:" + watcher.getClass().getName());
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        return register(watcher, events, null, modifiers);
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return register(watcher, events);
    }

    @Override
    public boolean equals(Object path) {
        if (path == null) return false;

        if (path instanceof DimitPath) { return ((DimitPath) path).toAbsolutePath().getPath().equals(this.toAbsolutePath().getPath())
                && ((DimitPath) path).getStoreSystem().equals(getStoreSystem()); }

        return false;
    }

    @Override
    public String toString() {
        return toAbsolutePath().getPath();
    }

}
