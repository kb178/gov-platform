# Sentinel 熔断

> 记录时间：2026-11-24
> 基于 gov-approval 调 gov-item 的实际踩坑经验整理

---

## 为什么需要熔断

gov-approval 调 gov-item 查材料清单，如果 gov-item 挂了或者很慢，每个请求都要等超时才能继续。请求一多，gov-approval 的线程全被占满，自己也瘫了。熔断就是在 gov-item 不正常时自动断开这个调用，快速失败，保护自己。

---

## 代码怎么写

用编程式埋点（`SphU.entry`），不用 `@SentinelResource` 注解，因为同类内部方法调用走不了 AOP 代理，注解不生效。

```java
Entry entry = null;
try {
    entry = SphU.entry("checkMaterial");       // 进入资源，Sentinel 开始计数
    R<List<Map<String, Object>>> result = remoteItemService.getMaterialListByItemId(itemId);
    if (result == null || !result.isSuccess()) {
        Tracer.trace(new RuntimeException("查询失败"));  // 业务失败也要报告
        return;
    }
    // 正常处理...
} catch (BlockException e) {
    // 熔断了，SphU.entry 直接抛到这里，远程调用根本没执行
    log.warn("checkMaterial 被熔断，跳过校验");
} catch (Exception e) {
    Tracer.trace(e);    // 系统异常（超时等）报告给 Sentinel
    log.warn("调用失败，跳过校验");
} finally {
    if (entry != null) entry.exit();    // 离开资源
}
```

三个关键 API：

- `SphU.entry("资源名")`：进入资源。如果处于熔断状态，直接抛 BlockException
- `Tracer.trace(e)`：报告一次失败。**不调这个 Sentinel 不知道失败了**，这是踩过的坑
- `entry.exit()`：离开资源，结束本次统计

---

## 踩过的坑

### 1. Tracer.trace 必须手动调

SphU.entry / entry.exit 只负责计数，不会自动识别异常。catch 住了异常但不调 Tracer.trace，Sentinel 会认为全是成功，永远不熔断。

### 2. 业务失败也要报告

远程服务抛异常时，Feign 可能把它转成 R(code=500) 返回，而不是抛 Java 异常。这时候代码走的是 `!result.isSuccess()` 分支，直接 return 了，根本没进 catch。所以 result 不是 success 时也要调 Tracer.trace 告诉 Sentinel 这次失败了。


---

## 控制台配置

Sentinel 控制台地址：http://localhost:8858（账号密码都是 sentinel）

项目 application.yml 配置：

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8858
        port: 8719
```

启动服务后，在控制台的"簇点链路"里能看到资源（要先调一次接口才会出现）。

### 熔断规则参数

在"熔断规则"里新增，以 checkMaterial 为例：

| 参数 | 值             | 含义 |
|------|---------------|------|
| 资源名 | checkMaterial | SphU.entry 里写的资源名 |
| 熔断策略 | 异常比例          | 按失败率判断（另有慢调用比例、异常数两种） |
| 阈值 | 0.5           | 失败率超 50% 触发熔断 |
| 熔断时长 | 10 秒          | 熔断期间所有请求直接拒绝 |
| 最小请求数 | 5             | 窗口内至少 2 个请求才开始判断，防止样本太少误判 |
| 统计时长 | 1000ms        | 统计窗口大小 |

### 各参数怎么调

- **最小请求数**：要在统计窗口内能凑够。请求慢就放大窗口，请求少就减小最小请求数
- **统计时长**：越大越平滑但反应越慢，越小越灵敏但容易误判
- **阈值**：多大比例的失败算不可接受。核心接口可以设高一些（0.7），非核心设低一些（0.3）
- **熔断时长**：给对方多久恢复的机会。一般 10~60 秒

---

## 熔断的三个状态

```
正常状态 →（异常率超阈值）→ 熔断状态 →（过了熔断时长）→ 半开状态
                                                              │
                                          试探成功 ←──────────┤
                                              ↓              │
                                          恢复正常    试探失败 → 回到熔断
```

- **正常**：请求正常放行，持续统计
- **熔断**：SphU.entry 直接抛 BlockException，不调远程
- **半开**：熔断时长到了，Sentinel 自动放一个请求试探。成功就恢复正常，失败就继续熔断

半开状态是 Sentinel 自动做的，代码不用管。

---

## 常见问题

**Q：为什么改了熔断规则没生效？**
A：规则是在控制台实时推送的，不用重启。但资源名要和代码里 SphU.entry 的名字完全一致。

**Q：簇点链路里看不到资源？**
A：Sentinel 是懒加载，接口被调用过一次才会出现。先调一次接口再看。

**Q：熔断触发了但服务恢复了，什么时候恢复？**
A：等熔断时长结束后自动进入半开状态，放一个请求试探。成功就恢复，不用手动操作。

**Q：@SentinelResource 和 SphU.entry 选哪个？**
A：公开方法用注解，同类内部调用用 SphU.entry。因为 Spring AOP 代理拦截不了 this.xxx() 的调用。
