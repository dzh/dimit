/**
 * 
 */
package dimit.core;

/**
 * @author dzh
 * @date Mar 22, 2018 7:08:12 PM
 * @version 0.0.1
 */
public interface StoreConst {

    String PATH_CONF = "conf";
    String PATH_STORE = "store";

    // env property
    // String P_STAT_DYNAMIC = "stat.dynamic"; // true
    String P_STAT_ENABLE = "stat.enable";   // true
    String P_STAT_WORKER_COUNT = "stat.worker.count"; // 1
    String P_STAT_WORKER_SNAPSHOT_INTERVAL = "stat.worker.snapshot.interval"; // 1000 ms
    String P_STAT_WORKER_SYNC_INTERVAL = "stat.worker.sync.interval"; // 5000 ms

    //
    String P_STAT_THRESHOLD_SUCC_RATE = "stat.threshold.succ.rate";
    String P_STAT_THRESHOLD_AVG_TIME = "stat.threshold.avg.time";

    int MAX_PRIORITY = 10;
    int MIN_PRIORITY = 0;

}
