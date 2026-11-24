# Nacos

> 记录时间：2026-11-24
> 项目里 gov-gateway、gov-system、gov-item 等服务都在用

---

## Nacos 是干嘛的

两个功能：**服务注册发现** + **配置中心**。

服务注册发现解决的问题是：微服务之间互相调用，对方在哪、还活着没有，得有个地方统一管。Nacos 就是那个地方，类似电话簿——每个服务启动时把自己的地址登记上去，要调别人时去查。

配置中心解决的问题是：一堆服务的配置文件散落在各个项目里，改一次配置要挨个改、挨个重启。配置统一放 Nacos 上，改了推下来就生效。

---

## 服务注册的流程

服务启动时做的事情：

1. 读 application.yml 里的 `spring.cloud.nacos.discovery.server-addr`，知道 Nacos 在哪
2. 发一个 HTTP 请求给 Nacos："我是 gov-system，我的地址是 192.168.1.5:8081"
3. Nacos 记到注册表里
4. 之后每隔 5 秒发一次心跳续约
5. 如果 15 秒没收到心跳，Nacos 认为这个服务挂了，从注册表里剔除

对应的配置：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: gov-platform
```

启动类上加了 `@EnableDiscoveryClient` 开启服务发现。

---

## 服务发现的流程

别的服务要调 gov-system 时：

1. 去 Nacos 查："gov-system 在哪"
2. Nacos 返回地址列表：[192.168.1.5:8081, 192.168.1.6:8081]
3. 负载均衡选一台（默认轮询）
4. 发 HTTP 请求过去

项目里有两种场景用到服务发现：

**网关路由转发：**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: gov-system
          uri: lb://gov-system    # lb:// 表示从 Nacos 查地址 + 负载均衡
          predicates:
            - Path=/system/**
```

**Feign 远程调用：**
```java
@FeignClient(value = "gov-item")  // "gov-item" 是服务名，Feign 自动去 Nacos 查地址
```

---

## 配置中心怎么用

项目里 application.yml 有这行：

```yaml
spring:
  config:
    import: optional:nacos:gov-system.yml
```

意思是启动时除了读本地 application.yml，还从 Nacos 上读 `gov-system.yml`。`optional:` 表示 Nacos 挂了也能启动，只是用不了远程配置。

在 Nacos 控制台上改配置，服务能实时收到，不用重启。

---

## 命名空间和分组

Nacos 做隔离有两个维度：**命名空间（namespace）** 和 **分组（Group）**。

命名空间是大隔离，比如开发环境一个、测试环境一个、生产环境一个，不同命名空间之间完全看不见。

分组是小隔离，在同一个命名空间内进一步分。比如同一个开发环境里，gov 系列服务一组、其他项目的服务一组。不配的话默认是 `DEFAULT_GROUP`。

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: gov-platform        # 命名空间
        group: DEFAULT_GROUP           # 分组，不配就是这个默认值
      config:
        server-addr: 127.0.0.1:8848
        namespace: gov-platform
        group: DEFAULT_GROUP
```

项目里只用了命名空间，分组用的默认值。实际项目如果有多条业务线共享一个 Nacos，可以用分组隔开。

命名空间有个坑：**配置里的 namespace 填的是 ID，不是名称**。

在控制台创建命名空间时，名称可以随便填（比如 `gov-platform`），但 ID 是自动生成的 UUID。配置里如果填名称，服务注册不上去。

```yaml
# 错误写法（填了名称）
namespace: gov-platform

# 正确写法（填 UUID）
namespace: 178acce0-ab6c-449a-9a0d-95a424ac49f7
```

或者创建命名空间时手动把 ID 填成和名称一样，这样配置里写 `gov-platform` 就能匹配上。项目里用的是后者。

---

## 临时实例和持久实例

Nacos 里的实例分两种：

**临时实例**（默认）：靠心跳保活，挂了自动剔除。适合大多数场景，服务重启、扩容缩容都能自动感知。

**持久实例**：注册后不会自动剔除，即使挂了还在注册表里（标记为不健康）。适合数据库这种不太变化的服务，避免因为网络抖动被误剔除。

微服务一般都用临时实例，项目里用的也是默认的临时实例。

---

## 负载均衡

一个服务部署多台时，Nacos 返回地址列表，调用方选一台。默认是轮询（RoundRobin），还有几种策略：

- 随机（Random）：随机选一台
- 权重（Weight）：配置的权重越大，分到的请求越多，适合机器配置不一样的场景
- 最小连接数（LeastConnection）：选当前连接数最少的那台

项目里用的是 Spring Cloud LoadBalancer，默认轮询。

---

## Nacos 认证

项目里 Nacos 是裸跑的，谁都能访问控制台、谁都能注册服务。本地开发无所谓，但放到线上就很危险——别人可以往你的 Nacos 注册个假服务，把流量截走。

开启认证在 Nacos 的 `conf/application.properties` 里配：

```properties
nacos.core.auth.enabled=true
nacos.core.auth.plugin.nacos.token.secret.key=随便写一串密钥
```

然后配置里带上用户名密码：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
      config:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

项目里目前没开认证，如果要部署到服务器上记得开。

---

## 单机模式和集群模式

项目里启动 Nacos 用的是单机模式：

```bash
startup.cmd -m standalone
```

单机模式就是一台机器跑一个 Nacos，数据只有一份。本地开发够用，但线上不行——这台机器挂了，所有服务的注册发现和配置都废了。

生产要部署集群，至少 3 台。做法是把 3 台 Nacos 的地址写到 `cluster.conf` 里，然后前面挂一个 Nginx 做负载均衡：

```
Nginx（VIP）
  ├── Nacos 节点1（192.168.1.5:8848）
  ├── Nacos 节点2（192.168.1.6:8848）
  └── Nacos 节点3（192.168.1.7:8848）
```

配置里指向 Nginx 的地址就行：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nginx的IP:80    # 指向 Nginx
```

集群模式下 Nacos 之间会同步数据。底层用的是 Raft 协议（类似 ZooKeeper 的 ZAB），保证数据一致性。3 台能容忍 1 台挂掉。

另外生产环境数据库也要换成 MySQL（Nacos 自带的嵌入式数据库只适合单机），在 `conf/application.properties` 里配 `spring.datasource.*` 指向 MySQL。

---

## 常见问题

**Q：服务注册不上去？**
A：大概率是命名空间不匹配。检查配置里的 namespace 是不是命名空间的 ID（UUID），不是名称。

**Q：服务下线了但 Nacos 里还显示在线？**
A：等 15 秒左右，心跳超时会自动剔除。如果等了很久还在，检查是不是配置了持久实例。

**Q：Nacos 挂了服务还能用吗？**
A：能用。已经注册过的服务本地有缓存，Feign 和网关会用缓存地址继续调用。但新启动的服务注册不了，新配置也读不了。配置里的 `optional:` 就是为了 Nacos 挂了不影响启动。

**Q：配置改了没生效？**
A：检查配置的 Data ID 和 Group 是否和 `spring.config.import` 里写的一致。还有就是 Nacos 的热更新只对 `@RefreshScope` 或 `@ConfigurationProperties` 注解的类生效。

**Q：服务名在哪里定义的？**
A：`spring.application.name`。Nacos 注册时用这个名字登记，调用方也用这个名字查。

**Q：命名空间和分组有什么区别？**
A：命名空间是大隔离（环境级别），不同命名空间完全看不见。分组是小隔离（业务级别），同一个命名空间内用分组再分。两个都配了才能精确找到目标服务。

**Q：Nacos 要部署几台？**
A：本地开发单机就行。生产至少 3 台集群，前面挂 Nginx。3 台能容忍 1 台挂掉，再多可以容忍更多。

**Q：Nacos 的数据存在哪？**
A：单机模式默认用内置的 Derby 数据库。集群模式必须用 MySQL，不然各节点数据不同步。
