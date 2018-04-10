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
String uri = "dimit-zk://yp/dimit?host=127.0.0.1:2181&sleep=1000&retry=3";
DimitStoreSystem dss = DimitStores.newStoreSystem(URI.create(uri), null);
```
- 依次创建流控Conf,存储结构简介:

```
- DimitConf             定义一个流控器
    - ChannelGroupConf  通道组配置
        - ChannelConf   通道配置
```
`注:` 参考单位测试 dimit-demo/../TestZkStoreConfDemo

- 初始化Dimiter，依据流控Conf创建对应的运行时数据

```java
// init dimiter and runtime channels
String uri = "dimit-zk://yp/dimit?host=127.0.0.1:2181&sleep=1000&retry=3";
Dimiter dimiter = Dimiter.newDimiter(URI.create(uri), null, dimitConfId);
ChannelGroupWrapper group = demo.initChannelGroup(channelGroupConfId);
group.newChannel(ChannelConfId_1, ChannelType.SEND);
group.newChannel(ChannelConfId_2, ChannelType.SEND);

// select
List<ChannelWrapper> selected = group.select(TagA,TagB);
V result = null;
for(ChannelWrapper channel : selected) {
    try{
        result = channel.call(new Callable<V>{
            // TODO request
            ...
        });
        //TODO parse result
        ... 
        break;
    }catch(RateLimiterException e){
        LOG.info(e.getMessage(), e);
        continue;  // next channel
    }catch(Exception e){
        LOG.error(e.getMessage(), e);
        continue; // or break
    }
}

// 程序结束时
dimiter.close();
```
`注:` 参考单位测试 dimit-demo/../TestDimiterDemo

## 特性列表
- 流量控制

- Tag筛选
    
- 质量分析

- 主备切换

- 异常切换

- Web管理

## 环境依赖
- maven3.0 or higher
- JDK1.7 or higher
- servlet-api v3.1.0 TODO 使用wep4j

## 文档链接
- [dimit工程说明](doc/dimit_project.md)
- [dimit数据模型](doc/dimit_store.md)
- [dimit设计思考](doc/dimit_design.md)
- [通道质量分析](doc/channel_stat.md)

## TODO
- 质量分析
- 异常切换
- Web管理
- 异常通道自动重试恢复
