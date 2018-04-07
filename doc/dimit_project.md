Dimit工程
=====================
源码说明

## 工程源码
```
- dimit
    - dimit-admin   管理dimit配置，实时显示运行状态
        - 
    - dimit-core    核心功能实现
    - dimit-demo    示例
    - dimit-redis   
    - dimit-store                                           核心存储的实现
        - dimit.store.sys.DimitStoreSystem                  存储系统的基础实现
    - dimit-zk                                              zookeeper相关实现
        - dimit.zk.store.ZkStoreSystem                      DimitStoreSystem的zookeeper实现
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
- [2.0.0, +∞) 待定
    - jdk9
    
## 贡献源码
- master发布分支,develop待发布分支。请Pull Request到develop
- eclipse配置文件
    - [代码格式化文件](doc/dev/formatter.xml)
    - [注释模版文件](doc/dev/codetemplates.xml)




