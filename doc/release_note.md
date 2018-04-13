发布记录
==========================


## v0.0.2 2018-xx-xx TODO
- Web管理功能 dimit-admin
- 编辑doc/dimit_conf.md配置参数说明
- 自定义切换策略
- 增加Channel从Invalid状态恢复的自动机制
- Dimiter的Master选举机制, 清理store、ChannelTotalStat统计等


## v0.0.1 2018-04-13
- 流量控制
    - ChannelConf定义max-tps, 每个Channel的tps = max-tps / Channel总数
- Tag筛选
    - ChannelConf定义通道的tag列表，ChannelGroupWrapper.select(...)时选择满足条件的Channel组
- 主备切换
    - ChannelConf定义通道ChannelStatus, select返回的可用通道列表里PRIMARY优先于STANDBY
- 质量分析
    - 启用Channel的质量分析功能(stat.enable=true 默认开启)，在ChannelWrapper.call()时采集stat信息
- 异常切换 
    - 通过stat信息动态计算ChannelWrapper.priority(), 影响select结果
