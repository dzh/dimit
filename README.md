dimit
====================
分布式流控系统

## 快速开始
- pom依赖

```xml
    <dependencies>
        <dependency>
            <groupId>io.github.dzh</groupId>
            <artifactId>dimit-core</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.dzh</groupId>
            <artifactId>dimit-zk</artifactId>
        </dependency>
    </dependencies>
```

- 初始化StoreSystem

```java
String uri = "dimit-zk://dzh/dimit?host=127.0.0.1:2181&sleep=1000&retry=3";
DimitStoreSystem dss = DimitStores.newStoreSystem(URI.create(uri), null);
```

- 依次创建流控Conf,配置结构层次:

```
- DimitConf             定义一个流控器
    - ChannelGroupConf  通道组配置
        - ChannelConf   通道配置
```

`注:` 参考单位测试 dimit-demo/../TestZkStoreConfDemo

- 初始化Dimiter，依据流控Conf创建对应的运行时数据

```java
// init dimiter and runtime channels
String uri = "dimit-zk://dzh/dimit?host=127.0.0.1:2181&sleep=1000&retry=3";
Dimiter dimiter = Dimiter.newDimiter(URI.create(uri), null, dimitConfId);
ChannelGroupWrapper group = demo.initChannelGroup(channelGroupConfId);
group.newChannel(ChannelConfId_1, ChannelType.SEND);
group.newChannel(ChannelConfId_2, ChannelType.SEND);

// select
List<ChannelWrapper> selected = group.select(TagA,TagB);
V result = null;
for(ChannelWrapper channel : selected) {
    try{
        result = channel.call(new ChannelCallable<V>{
            // TODO request
            ...
        });
        // TODO parse result
        ... 
        break;
    }catch(RateLimiterException e){
        LOG.info(e.getMessage(), e);
    }catch(InvalidChannelException e){
        LOG.error(e.getMessage(), e);
    }catch(Exception e){  // or break
        LOG.error(e.getMessage(), e);
    }
}

// program exit
dimiter.close();
```

`注:` 参考单位测试 dimit-demo/../TestDimiterDemo

## 特性列表
- 流量控制
    - ChannelConf定义max-tps, Channel实例的tps = max-tps / Channel总数
- Tag筛选
    - ChannelConf定义通道的tag列表，ChannelGroupWrapper.select(...)时选择满足条件的Channel组
- 主备切换
    - ChannelConf定义通道ChannelStatus, select返回的可用通道列表里PRIMARY优先于STANDBY
- 质量分析
    - 启用Channel的质量分析功能(`stat.enable=true` 默认开启)，在ChannelWrapper.call()时采集stat信息
- 异常切换
    - 通过stat信息动态计算ChannelWrapper.priority(), 影响select结果
- Web管理
    - TODO

## 环境依赖
- JDK1.7 or higher
- maven 3
- protobuf 3
- servlet-api v3.1.0 TODO 使用wep4j


## 文档链接
- [工程源码](doc/dimit_project.md)
- [数据模型](doc/dimit_store.md)
- [配置说明](doc/dimit_conf.md)
- [版本记录](doc/release_note.md)

## TODO
- Web管理
- 通道自动恢复
