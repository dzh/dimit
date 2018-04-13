DimitStoreSystem
==================
存储系统说明


## 核心类
### URI

### DimitStoreSystemProvider

### DimitStoreSystem


### DimitPath

### StoreIO

### StoreWatchKey


## 存储结构
- 示例说明 URI: `dimit-zk://domain/path`

```
/path
    domain
        conf        // 配置信息
            DimitConf_N             // 分流器定义
                ChannelGroupConf_N  // 通道组定义
                    ChannelConf_N   // 通道配置
        store       // 运行时信息
            DimitConf_N_ID          // DimitConf_N的ID 读取Dimiter所在机器的基本服务器信息
                Dimit_ID            // Dimit实例，临时节点
            ChannelConf_N_ID        // ChannelConf_N的ID
                0           // ChannelType.SEND 监听这里可以计算总和tps 
                    Channel_ID_N        // 通道实例ID，临时节点 Channel
                1           // ChannelType.RECV
                    Channel_ID_N        // 通道实例ID，临时节点 Channel
                stat0       // 发送统计信息 TODO 由master角色的Dimiter定期汇总children得出
                    Channel_ID_N        // 通道实例ID的发送统计，临时节点 ChannelStat
                stat1       // 接收统计信息 TODO 由master角色的Dimiter定期汇总children得出
                    Channel_ID_N        // 通道实例ID的接收统计，临时节点 ChannelStat                  
```

- 运行时更新
    - 不可用状态的通道不会更新统计信息 stat_0 stat_1









