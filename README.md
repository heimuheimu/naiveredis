# NaiveRedis: 简单易用的 Redis Java 客户端，适用于高并发应用。

## 使用要求
* JDK 版本：1.8+ 
* 依赖类库：
  * [slf4j-log4j12 1.7.5+](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)
  * [naivemonitor 1.0+](https://github.com/heimuheimu/naivemonitor)
  * [compress-lzf 1.0.3+](https://github.com/ning/compress)

## 使用限制
* 不支持需要阻塞的 Redis 操作。
* 不支持 Redis 原生 Cluster 模式。
* 部分方法有最低 Redis 版本要求，如果使用的 Redis 版本较老，请阅读 API 文档查找该方法对应的 Redis 命令，并前往 Redis 官网确认该命令的最低版本要求。 

## NaiveRedis 特色：
* 根据 Redis 不同的数据结构定义对应的接口，方便使用。
* 自动关闭连续出现超时异常的 Redis 连接，防止应用堵塞。（超时异常大于 50 次的连接将会被自动关闭，每两次超时异常需发生在 1 秒间隔以内）
* 集群客户端将会自动恢复不可用的 Redis 连接。
* 集群客户端中的 Redis 连接出现不可用或恢复时，将通过钉钉、短信等方式实时报警，第一时间掌握线上状况。
* 完善的日志信息、监控信息，方便问题定位。

## Maven 配置
```xml
    <dependency>
        <groupId>com.heimuheimu</groupId>
        <artifactId>naiveredis</artifactId>
        <version>1.0</version>
    </dependency>
```

## Log4J 配置
```
# Redis 根日志
log4j.logger.com.heimuheimu.naiveredis=WARN, NAIVEREDIS
log4j.additivity.com.heimuheimu.naiveredis=false
log4j.appender.NAIVEREDIS=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVEREDIS.file=${log.output.directory}/naiveredis/naiveredis.log
log4j.appender.NAIVEREDIS.encoding=UTF-8
log4j.appender.NAIVEREDIS.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVEREDIS.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVEREDIS.layout.ConversionPattern=%d{ISO8601} %-5p [%F:%L] : %m%n

# Redis 连接信息日志
log4j.logger.NAIVEREDIS_CONNECTION_LOG=INFO, NAIVEREDIS_CONNECTION_LOG
log4j.additivity.NAIVEREDIS_CONNECTION_LOG=false
log4j.appender.NAIVEREDIS_CONNECTION_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVEREDIS_CONNECTION_LOG.file=${log.output.directory}/naiveredis/connection.log
log4j.appender.NAIVEREDIS_CONNECTION_LOG.encoding=UTF-8
log4j.appender.NAIVEREDIS_CONNECTION_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVEREDIS_CONNECTION_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVEREDIS_CONNECTION_LOG.layout.ConversionPattern=%d{ISO8601} %-5p : %m%n

# Redis 错误信息日志，不打印错误堆栈
log4j.logger.NAIVEREDIS_ERROR_LOG=ERROR, NAIVEREDIS_ERROR_LOG
log4j.additivity.NAIVEREDIS_ERROR_LOG=false
log4j.appender.NAIVEREDIS_ERROR_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVEREDIS_ERROR_LOG.file=${log.output.directory}/naiveredis/error.log
log4j.appender.NAIVEREDIS_ERROR_LOG.encoding=UTF-8
log4j.appender.NAIVEREDIS_ERROR_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVEREDIS_ERROR_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVEREDIS_ERROR_LOG.layout.ConversionPattern=%d{ISO8601} : %m%n

# Redis 慢查日志，打印执行时间过慢的操作
log4j.logger.NAIVEREDIS_SLOW_EXECUTION_LOG=INFO, NAIVEREDIS_SLOW_EXECUTION_LOG
log4j.additivity.NAIVEREDIS_SLOW_EXECUTION_LOG=false
log4j.appender.NAIVEREDIS_SLOW_EXECUTION_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.NAIVEREDIS_SLOW_EXECUTION_LOG.file=${log.output.directory}/naiveredis/slow_execution.log
log4j.appender.NAIVEREDIS_SLOW_EXECUTION_LOG.encoding=UTF-8
log4j.appender.NAIVEREDIS_SLOW_EXECUTION_LOG.DatePattern=_yyyy-MM-dd
log4j.appender.NAIVEREDIS_SLOW_EXECUTION_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.NAIVEREDIS_SLOW_EXECUTION_LOG.layout.ConversionPattern=%d{ISO8601} : %m%n
```

## Spring 配置
```xml
    <!-- Redis 一致性 Hash 分发集群客户端 -->
    <bean id="redisClient" class="com.heimuheimu.naiveredis.spring.SimpleRedisClusterClientFactory" destroy-method="close">
        <constructor-arg index="0" value="127.0.0.1:6379,127.0.0.1:6380" />
        <constructor-arg index="1">
            <!-- 在 Redis 服务不可用时进行实时通知 -->
            <bean class="com.heimuheimu.naiveredis.facility.clients.NoticeableDirectRedisClientListListener">
                <constructor-arg index="0" value="your-project-name" /> <!-- 当前项目名称 -->
                <constructor-arg index="1" ref="notifierList" /> <!-- 报警器列表，报警器的信息可查看 naivemonitor 项目 -->
            </bean>
        </constructor-arg>
    </bean>
```

如果 Redis 使用 Master-Slave 模式，可使用 [SimpleRedisReplicationClient]()。 

## Falcon 监控数据上报 Spring 配置
```xml
    <!-- 监控数据采集器列表 -->
    <util:list id="falconDataCollectorList">
        <!-- Redis 客户端监控数据采集器 -->
        <bean class="com.heimuheimu.naiveredis.monitor.falcon.ClusterDataCollector" />
        <bean class="com.heimuheimu.naiveredis.monitor.falcon.CompressionDataCollector" />
        <bean class="com.heimuheimu.naiveredis.monitor.falcon.ExecutionDataCollector" />
        <bean class="com.heimuheimu.naiveredis.monitor.falcon.SocketDataCollector" />
        <bean class="com.heimuheimu.naiveredis.monitor.falcon.ThreadPoolDataCollector" />
    </util:list>
    
    <!-- Falcon 监控数据上报器 -->
    <bean id="falconReporter" class="com.heimuheimu.naivemonitor.falcon.FalconReporter" init-method="init" destroy-method="close">
        <constructor-arg index="0" value="http://127.0.0.1:1988/v1/push" /> <!-- Falcon 监控数据推送地址 -->
        <constructor-arg index="1" ref="falconDataCollectorList" />
    </bean>
```

## Falcon 上报数据项说明（上报周期：30秒）
### Redis 集群客户端数据项：
 * naiveredis_cluster_unavailable_client_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 集群客户端获取到不可用 Redis 客户端的次数
 * naiveredis_cluster_multi_get_error_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 集群客户端调用 #multiGet(Set<String> keySet) 方法出现的错误次数
 
### Redis 操作执行错误数据项：
 * naiveredis_illegal_argument/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 操作发生的参数不正确错误次数
 * naiveredis_illegal_state/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 操作发生的管道或命令已关闭错误次数
 * naiveredis_timeout/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 操作发生的超时错误次数
 * naiveredis_redis_error/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 操作发生的 Redis 错误次数
 * naiveredis_key_not_found/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis Get 操作发生的 Key 未找到错误次数
 * naiveredis_unexpected_error/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 操作发生的预期外错误次数
 * naiveredis_slow_execution/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Redis 操作发生的慢执行次数

### Redis 操作执行数据项：
 * naiveredis_tps/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒平均执行次数
 * naiveredis_peak_tps/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内每秒最大执行次数
 * naiveredis_avg_exec_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次 Redis 操作平均执行时间
 * naiveredis_max_exec_time/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内单次 Redis 操作最大执行时间
 
### Redis 操作 Socket 数据项：
 * naiveredis_socket_read_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 读取的总字节数
 * naiveredis_socket_avg_read_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 每次读取的平均字节数
 * naiveredis_socket_written_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 写入的总字节数
 * naiveredis_socket_avg_written_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 每次写入的平均字节数
 
### Redis 操作压缩数据项： 
 * naiveredis_compression_reduce_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内压缩操作已节省的字节数
 * naiveredis_compression_avg_reduce_bytes/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内平均每次压缩操作节省的字节数
 
### Redis 客户端线程池数据项：  
 * naiveredis_threadPool_rejected_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内所有线程池拒绝执行的任务总数
 * naiveredis_threadPool_active_count/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 采集时刻所有线程池活跃线程数近似值总和
 * naiveredis_threadPool_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 采集时刻所有线程池线程数总和
 * naiveredis_threadPool_peak_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 所有线程池出现过的最大线程数总和
 * naiveredis_threadPool_core_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 所有线程池配置的核心线程数总和
 * naiveredis_threadPool_maximum_pool_size/module=naiveredis &nbsp;&nbsp;&nbsp;&nbsp; 所有线程池配置的最大线程数总和

## 已支持的 Redis 客户端
 * [NaiveRedisClient]() 提供所有已支持的 Redis 操作，建议仅在 Service 中需要同时使用多种 Redis 数据结构时才使用此接口。
 * [NaiveRedisStorageClient]() 提供 Redis 存储相关的操作，将 Redis 作为缓存或 NoSQL 数据库时进行使用。
 * [NaiveRedisCountClient]() 提供 Redis 计数器相关的操作，使用 Redis 进行数字的原子加、减。
 * [NaiveRedisGeoClient]() 提供 Redis GEO 数据结构的相关的操作。
 * [NaiveRedisHashesClient]() 提供 Redis Hashes 数据结构的相关的操作。
 * [NaiveRedisListClient]() 提供 Redis List 数据结构的相关的操作。
 * [NaiveRedisSetClient]() 提供 Redis Set 数据结构的相关的操作。
 * [NaiveRedisSortedSetClient]() 提供 Redis Sorted Set 数据结构的相关的操作。
 

## Redis 部分客户端示例代码
### 存储客户端
```java
    public class RedisStorageDemoService {
        
        @Autowired
        private NaiveRedisStorageClient redisStorageClient;
        
        public void test() {
            User alice = new User(); //需要存入 Redis 中的 User 实例，必须是可序列化的（实现 Serializable 接口）
            
            redisStorageClient.set("demo_user_alice", alice, 30); //将 alice 实例存入 Redis 中，并设置过期时间为 30 秒
            
            User aliceFromRedis = redisStorageClient.get("demo_user_alice"); //从 Redis 中将 alice 实例取回
            
            redisStorageClient.delete("demo_user_alice"); //在 Redis 中删除 alice 实例
        }
    }
```

### 计数器客户端
```java
    public class RedisCountDemoService {
        
        @Autowired
        private NaiveRedisCountClient redisCountClient;
        
        public void test() {
            redisCountClient.addAndGet("demo_counter", 1, 60); //对 "demo_counter" 这个 Key 执行原子 +1 操作，并设置过期时间为 60 秒
            
            redisCountClient.addAndGet("demo_counter", 1, 60); //对 "demo_counter" 这个 Key 再次执行原子 +1 操作，不会刷新过期时间
            
            Long count = redisCountClient.getCount("demo_counter"); //获得 "demo_counter" 这个 Key 对应的计数值，如果 Key 不存在，将返回 null
            
            redisCountClient.delete("demo_counter"); //在 Redis 中删除 Key 对应的计数值
        }
    }
```

### Set 客户端
```java
    public class RedisSetDemoService {
        
        @Autowired
        private NaiveRedisSetClient redisSetClient;
        
        public void test() {
            redisSetClient.addToSet("demo_set", "one"); // 将 "one" 存入名称为 "demo_set" 的 Set 集合中
            
            List<String> members = new ArrayList<>();
            members.add("two");
            members.add("three");
            members.add("four");
            members.add("five");
            members.add("six");
            
            redisSetClient.addToSet("demo_set", members); // 将 members 中的所有元素存入名称为 "demo_set" 的 Set 集合中
            
            // 判断 "one" 是否在名称为 "demo_set" 的 Set 集合中存在
            boolean isExist = redisSetClient.isMemberInSet("demo_set", "one"); 
            
            int size = redisSetClient.getSizeOfSet("demo_set"); // 获得名称为 "demo_set" 的 Set 集合大小
            
            // 从名称为 "demo_set" 的 Set 集合中随机返回 3 个值
            List<String> randMembers = redisSetClient.getMembersFromSet("demo_set", 3); 
            
            // 从名称为 "demo_set" 的 Set 集合中随机返回 3 个值，并将这 3 个值从 Set 集合中删除
            List<String> popMembers = redisSetClient.popMembersFromSet("demo_set", 3); 
            
            // 返回名称为 "demo_set" 的 Set 集合中的所有值
            List<String> allMembers = redisSetClient.getAllMembersFromSet("demo_set");
            
            // 从名称为 "demo_set" 的 Set 集合中删除值 "three"
            redisSetClient.removeFromSet("demo_set", "three");
            
            redisSetClient.delete("demo_set"); //删除名称为 "demo_set" 的 Set 集合
        }
    }
``` 

## 更多信息
* [Redis 官网](https://redis.io)
* [NaiveMonitor 项目主页](https://github.com/heimuheimu/naivemonitor)
* [NaiveRedis v1.0 API Doc](https://heimuheimu.github.io/naiveredis/api/v1.0/)
* [NaiveRPC v1.0 源码下载](https://heimuheimu.github.io/naiveredis/download/naiveredis-1.0-sources.jar)
* [NaiveRPC v1.0 Jar包下载](https://heimuheimu.github.io/naiveredis/download/naiveredis-1.0.jar)

