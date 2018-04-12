发布记录
==========================


## v0.0.2 2018-04-20 TODO
- 发布dimit-admin Web管理功能
- 编辑doc/dimit_conf.md 配置参数说明
- 自定义切换策略


## v0.0.1 2018-04-13
- 流量控制
    - ChannelConf定义max-tps, 每个Channel的tps = max-tps / Channel总数
- Tag筛选
    - ChannelConf定义通道的tag列表，ChannelGroupWrapper.select(...)时选择满足条件的Channel组
- 主备切换
    - ChannelConf定义通道ChannelStatus, select返回的可用通道列表里PRIMARY优先于STANDBY
- 质量分析
- 异常切换 