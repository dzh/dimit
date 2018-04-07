package dimit.zk;

import dimit.store.sys.Const;

/**
 * @author dzh
 * @date Apr 2, 2018 7:54:32 PM
 * @version 0.0.1
 */
public interface ZkConst extends Const {

    String URI_QUERY_HOST = "host";

    String URI_QUERY_RETRY = "retry"; // max retries

    String URI_QUERY_SLEEP = "sleep"; // base sleep ms

    String URI_QUERY_SESSION_TIMEOUT = "sessionTimeout";// TODO
    String URI_QUERY_CONNECT_TIMEOUT = "connectTimeout";// TODO
    String URI_QUERY_NAMESPACE = "ns";

}
