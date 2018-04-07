package dimit.store.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dzh
 * @date Apr 2, 2018 7:51:08 PM
 * @version 0.0.1
 */
public class URIUtil {

    public static final Map<String, String> getQuery(URI uri) throws IOException {
        String query = uri.getQuery();
        if (query.length() == 0) return Collections.emptyMap();

        Map<String, String> r = new HashMap<>();
        int i = 0;
        int p = i;
        String key = null;
        while (i < query.length()) {
            char c = query.charAt(i);
            switch (c) {
            case '=':
                key = query.substring(p, i).trim();
                p = i + 1;
                break;
            case '&':
                r.put(key, URLDecoder.decode(query.substring(p, i).trim(), "utf-8"));
                p = i + 1;
                break;
            default:
                if (i == query.length() - 1) {
                    r.put(key, URLDecoder.decode(query.substring(p, query.length()).trim(), "utf-8"));
                    break;
                }
            }
            i++;
        }
        return r;
    }

}
