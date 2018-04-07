Dimit存储
==================
存储系统说明


## DimitStore 
### 功能
- 表示单机的一个存储实例

## ChannelGroupStore
### 功能
- 通道组包含多个ChannelStore，定义一组Channel的公共配置
- 通道组其下可以包含子通道、通道组


## ChannelStore
### 功能
- 通道的配置
- 通道必然属于一个组，默认组的名字是通道的id


## DimitStoreEngine
### 功能
- Dimit服务的存储引擎，负责数据存储、变更通知等
### ZkDimitStore
- 基于zookeeper实现的DimitStore


## ZkDimitStore存储层次
- Dimit  # dimit分布式的根目录
    - DimitStore A # 应用服务的存储实例A，相当于一个应用进程
        - ChannelGroupStore CG1 # 通道组配置,其下包含多个通道(组)
            - ChannelStore A_CG1_S1 # 通道A_CG1_S1配置
            - ChannelStore A_CG1_S2 # 通道A_CG1_S2配置
            - ChannelGroupStore CG1_CG1 # 通道组
                - ChannelStore A_CG1_CG1_S1 # 通道配置
        - ChannelGroupStore CG2 # 通道组配置
    - DimitStore B # 应用服务的存储实例B
        - ChannelGroupStore CG1 # 和实例A有相同的通道组
            - ChannelStore B_CG1_S1 # 说明B_CG1_S1和A_CG1_S1是相同CG1组下的通道
            - ChannelStore B_CG1_S2 # 说明B_CG1_S2和A_CG1_S2是相同CG2组下的通道
        - ChannelGroupStore CG3

`注:` 这里的命名目的是为了说明包含层次关系，与实际的通道(组)命名无关

## engine的本地索引机制

## engine的本地缓存机制

