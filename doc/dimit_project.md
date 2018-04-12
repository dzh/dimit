Dimit工程
=====================
源码说明

## 工程源码
```
- dimit
    - dimit-admin   管理dimit配置，实时显示运行状态
        - TODO
    - dimit-core                    流控器逻辑实现
        - ChannelStatWorker         定时对ChannelWraper.stat作快照、统计、同步
        - Dimiter                   流控器实例对应一个DimitConf实现
        - ChannelCallable           用于ChannelWrapper.call中统计等
        - ChannelGroupWrapper       通道组运行时
        - ChannelSelector           通道选择器，从一个通道组内选择可用的通道
        - ChannelStatWrapper        通道调用的统计数据
        - ChannelWrapper            通道运行时
        - DimitWrapper              流控器实例
        - RateLimiterException      通道超过tps时的异常,ChannelWrapper.call()触发
        - StatWrapper               Stat的接口定义
        - StoreWrapper              Store的接口定义
    - dimit-demo                测试用例
        - TestDimiterDemo       流控器功能单元测试
        - TestZkStoreConfDemo   初始化测试数据, StoreSystem创建Conf配置
    - dimit-redis   质量分析
        - TODO
    - dimit-store                       StoreSystem基础功能
        - src/main/proto
            - dimitStore.proto          运行时数据结构
            - dimitStoreConf.proto      流控配置数据结构 
        - DimitPath                     数据结构的路径
        - DimitStores                   StoreSystem的工具方法
        - DimitStoreSystem              StoreSystem的抽象实现类
        - DimitStoreSystemProvider      StoreSystem提供器的抽象类
        - LocalStoreSystem              StoreSystem的local实现 TODO
        - StoreAttibute                 存储属性定义
        - EventHandler                  存储系统的系统处理器接口
        - StoreEventKind                存储系统事件种类
        - StoreWatcher                  存储系统事件观察器抽象类
        - StoreWatchEvent               存储系统事件
        - StoreWatchKey                 存储系统事件的句柄
        - StoreWatchService             观察服务
    - dimit-zk                      StoreSystem的zk实现
        - NodeCacheWatcher          提供节点变更的通知
        - PathChlidrenWatcher       提供子节点变更的通知
        - ZkStoreSystem             zk实现的StoreSystem
        - ZkStoreSystemProvider     ZkStoreSystem的提供器
        - src/main/resources
            - META-INF/services/dimit.store.sys.DimitStoreSystemProvider     ZkStoreSystemProvider定义   
    - doc                               文档
        - dev/eclipse/                  eclipse ide相关开发文档
        - dimit_design.md               设计思虑
        - dimit_project.md              工程源码说明
        - dimit_runtime.md              运行时参数说明
        - dimit_store.md                存储系统说明
```


## 配置说明
- [运行时参数](dimit_runtime.md)
   
## 版本依赖
- (0.0.1, 1.0.0)
    - jdk7 or higher 
- [1.0.0, 2.0.0)
    - jdk8
- [2.0.0, +∞) TODO 
    - jdk9
    
## 贡献源码
- master发布分支,develop待发布分支。请Pull Request到develop
- eclipse配置文件
    - [代码格式化文件](dev/eclipse/formatter.xml)
    - [注释模版文件](dev/eclipse/codetemplates.xml)




