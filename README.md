# NaiveRedis: 简单易用的 Redis Java 客户端，适用于高并发应用。

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/heimuheimu/naiveredis.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/heimuheimu/naiveredis/context:java)

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)
  * [naivemonitor 1.0+](https://github.com/heimuheimu/naivemonitor)
  * [compress-lzf 1.0.3+](https://github.com/ning/compress)

## 使用限制
* 部分方法有最低 Redis 版本要求，如果使用的 Redis 版本较老，请阅读 API 文档查找该方法对应的 Redis 命令，并前往 Redis 官网确认该命令的最低版本要求。
* [NaiveRedisStorageClient](https://heimuheimu.github.io/naiveredis/api/v1.1/com/heimuheimu/naiveredis/NaiveRedisStorageClient.html) 会将 Java 对象序列化后以字节形式进行存储，
由于序列化实现方式不同，因此无法与其它 Redis 客户端协作使用，可使用 [NaiveRedisRawStorageClient](https://heimuheimu.github.io/naiveredis/api/v1.1/com/heimuheimu/naiveredis/NaiveRedisRawStorageClient.html) 进行替换。

## NaiveRedis 特色：
* 根据 Redis 不同的数据结构定义对应的接口，方便使用。
* 自动关闭连续出现超时异常的 Redis 连接，防止应用堵塞。（超时异常大于 50 次的连接将会被自动关闭，每两次超时异常需发生在 1 秒间隔以内）
* 集群客户端将会自动恢复不可用的 Redis 连接。
* 集群客户端中的 Redis 连接出现不可用或恢复时，将通过钉钉、短信等方式实时报警，第一时间掌握线上状况。
* 为高并发应用量身定制，完善的日志信息、监控信息，第一时间定位问题。

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>naiveredis</artifactId>
        <version>1.1</version>
    </dependency>
```

## 如何使用
1. [NaiveRedis 客户端使用说明](https://github.com/heimuheimu/naiveredis/wiki/NaiveRedis-客户端使用说明)
2. [NaiveRedis Pub/Sub使用说明](https://github.com/heimuheimu/naiveredis/wiki/NaiveRedis-PubSub-使用说明)
3. [NaiveRedis 分布式锁使用说明](https://github.com/heimuheimu/naiveredis/wiki/NaiveRedis-分布式锁使用说明)

## 更多信息
* [Redis 官网](https://redis.io)
* [NaiveMonitor 项目主页](https://github.com/heimuheimu/naivemonitor)
* [NaiveRedis v1.1 API Doc](https://heimuheimu.github.io/naiveredis/api/v1.1/)
* [NaiveRPC v1.1 源码下载](https://heimuheimu.github.io/naiveredis/download/naiveredis-1.1-sources.jar)
* [NaiveRPC v1.1 Jar包下载](https://heimuheimu.github.io/naiveredis/download/naiveredis-1.1.jar)