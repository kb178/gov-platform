# JWT 认证过滤器详解

> 本文档详细讲解 JWT 认证过滤器的工作原理和执行流程

---

## 一、Session vs JWT 认证方式对比

### 1.1 传统 Session 认证方式

**原理：** 服务器保存用户会话信息，客户端只保存 SessionID。

```
┌─────────────────────────────────────────────────────────────────┐
│                     Session 认证流程                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   第 1 步：用户登录                                             │
│   ──────────────────                                            │
│   前端: POST /login {"phone":"13800138000","password":"123456"} │
│              ↓                                                  │
│   服务器: 验证用户名密码 → 创建 Session → 保存到内存            │
│           SessionID = "abc123"                                  │
│           Session 数据: {userId: 1, username: "13800138000"}    │
│              ↓                                                  │
│   响应: Set-Cookie: JSESSIONID=abc123  ← 返回给浏览器           │
│                                                                 │
│   第 2 步：后续请求                                             │
│   ──────────────────                                            │
│   前端: GET /user/info                                          │
│         Cookie: JSESSIONID=abc123  ← 浏览器自动带上            │
│              ↓                                                  │
│   服务器: 根据 SessionID 查找 Session → 获取用户信息            │
│              ↓                                                  │
│   返回用户信息                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 JWT 认证方式

**原理：** 服务器不保存状态，所有信息都在 Token 中。

```
┌─────────────────────────────────────────────────────────────────┐
│                      JWT 认证流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   第 1 步：用户登录                                             │
│   ──────────────────                                            │
│   前端: POST /login {"phone":"13800138000","password":"123456"} │
│              ↓                                                  │
│   服务器: 验证用户名密码 → 生成 Token（包含用户信息+签名）      │
│           Token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjF9.xxx" │
│              ↓                                                  │
│   响应: {"accessToken": "eyJhbGci..."}  ← 返回给前端            │
│   前端: 保存到 localStorage 或 Cookie                          │
│                                                                 │
│   第 2 步：后续请求                                             │
│   ──────────────────                                            │
│   前端: GET /user/info                                          │
│         Authorization: Bearer eyJhbGci...  ← 手动带上          │
│              ↓                                                  │
│   服务器: 验证 Token 签名 → 解析出用户信息（不查数据库）        │
│              ↓                                                  │
│   返回用户信息                                                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 核心区别对比

| 对比项 | Session 认证 | JWT 认证 |
|--------|-------------|---------|
| **存储位置** | 服务器内存/Redis | 客户端（前端） |
| **每次请求** | 带 Cookie（自动） | 带 Token（手动） |
| **验证方式** | 根据 SessionID 查找 Session | 验证 Token 签名 |
| **服务器状态** | 有状态（保存 Session） | 无状态（不保存） |
| **扩展性** | 差（需要共享 Session） | 好（任何服务器都能验证） |
| **安全性** | 中（Session 劫持） | 高（签名验证） |

### 1.4 为什么选择 JWT？

| 优势 | 说明 |
|------|------|
| **无状态** | 服务器不需要保存会话，减少内存占用 |
| **可扩展** | 微服务架构下，任何服务器都能独立验证 Token |
| **安全性高** | Token 有签名，被篡改后验证会失败 |
| **跨域支持** | 不依赖 Cookie，支持跨域请求 |
| **适合微服务** | 网关统一验证，服务间调用方便 |

### 1.5 每次请求都要重新验证吗？

**是的！** 这是 JWT 的核心设计。

```
┌─────────────────────────────────────────────────────────────────┐
│                   为什么每次都要验证？                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ❌ 错误理解：                                                 │
│   用户登录 → 验证 Token → 保存到服务器 → 后续直接使用           │
│   问题：如果 Token 被盗，攻击者可以一直使用                     │
│                                                                 │
│   ✅ 正确理解：                                                 │
│   用户登录 → 返回 Token                                         │
│   每次请求 → 验证 Token（是否过期？是否被篡改？）               │
│   好处：即使 Token 被盗，过期后就失效                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**对比 Session：**

| 项目 | Session | JWT |
|------|---------|-----|
| 验证次数 | 每次请求都要查找 Session | 每次请求都要验证 Token |
| 服务器压力 | 高（内存占用） | 低（无状态） |
| 安全性 | 中（Session 固定） | 高（每次验证签名） |

---

## 二、什么是 JWT 认证过滤器？

### 2.1 简单理解

JWT 认证过滤器就像一个**门卫**，负责检查每个请求是否携带了有效的"通行证"（Token）。

```
用户请求 → 门卫检查通行证 → 有效 → 放行 → 进入办公室
                           → 无效 → 拒绝 → 返回 401
```

### 2.2 为什么需要它？

| 没有过滤器 | 有过滤器 |
|-----------|---------|
| 任何人都能访问接口 | 只有登录用户才能访问 |
| 不安全 | 安全 |
| 无法知道是谁在访问 | 知道是哪个用户在访问 |

---

## 二、整体架构

### 2.1 请求流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│   用户（前端）                                                   │
│      │                                                          │
│      │  POST /sysUser/realNameAuth                              │
│      │  Header: Authorization: Bearer eyJhbGciOiJIUz...        │
│      ↓                                                          │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                 Gateway 网关 (端口 8080)                 │   │
│  │                                                         │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │              AuthFilter（第一道门卫）             │   │   │
│  │   │                                                 │   │   │
│  │   │   ① 检查是否白名单路径                           │   │   │
│  │   │      /sysUser/login → 不需要通行证，直接放行     │   │   │
│  │   │      /sysUser/realNameAuth → 需要通行证         │   │   │
│  │   │                                                 │   │   │
│  │   │   ② 获取通行证（Token）                         │   │   │
│  │   │      从请求头 Authorization 中提取              │   │   │
│  │   │                                                 │   │   │
│  │   │   ③ 验证通行证是否有效                          │   │   │
│  │   │      - 是否过期？                               │   │   │
│  │   │      - 签名是否正确？                           │   │   │
│  │   │                                                 │   │   │
│  │   │   ④ 从通行证中提取用户信息                      │   │   │
│  │   │      userId = 2083862615502741505               │   │   │
│  │   │      username = "13800138000"                   │   │   │
│  │   │                                                 │   │   │
│  │   │   ⑤ 将用户信息放入"信封"（请求头）              │   │   │
│  │   │      X-User-Id: 2083862615502741505            │   │   │
│  │   │      X-User-Name: 13800138000                  │   │   │
│  │   │                                                 │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  │                         │                                │   │
│  │                         ↓                                │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │              路由转发                            │   │   │
│  │   │   /system/** → http://gov-system:8081/**        │   │   │
│  │   │   去掉 /system 前缀                             │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                         │                                       │
│                         ↓                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              System 服务 (端口 8081)                     │   │
│  │                                                         │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │           JwtAuthFilter（第二道门卫）            │   │   │
│  │   │                                                 │   │   │
│  │   │   ① 从请求头获取 Token                          │   │   │
│  │   │      Authorization: Bearer eyJhbGciOiJIUz...   │   │   │
│  │   │      去掉 "Bearer " 前缀，得到纯 Token          │   │   │
│  │   │                                                 │   │   │
│  │   │   ② Token 为空？                                │   │   │
│  │   │      - 是 → 直接放行，交给后面的门卫判断         │   │   │
│  │   │      - 否 → 继续验证                            │   │   │
│  │   │                                                 │   │   │
│  │   │   ③ 验证 Token                                  │   │   │
│  │   │      - 是否过期？                               │   │   │
│  │   │      - 签名是否正确？                           │   │   │
│  │   │                                                 │   │   │
│  │   │   ④ 解析用户信息                                │   │   │
│  │   │      userId = 2083862615502741505               │   │   │
│  │   │      username = "13800138000"                   │   │   │
│  │   │                                                 │   │   │
│  │   │   ⑤ 存入"保险柜"（Security 上下文）             │   │   │
│  │   │      SecurityContextHolder.getContext()         │   │   │
│  │   │          .setAuthentication(auth)               │   │   │
│  │   │                                                 │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  │                         │                                │   │
│  │                         ↓                                │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │           Spring Security（第三个门卫）          │   │   │
│  │   │                                                 │   │   │
│  │   │   ① 检查请求路径                                │   │   │
│  │   │      /sysUser/login → 不需要登录，放行          │   │   │
│  │   │      /sysUser/realNameAuth → 需要登录          │   │   │
│  │   │                                                 │   │   │
│  │   │   ② 检查是否已登录                              │   │   │
│  │   │      SecurityContextHolder.getContext()         │   │   │
│  │   │          .getAuthentication()                   │   │   │
│  │   │      - 有认证信息 → 放行                        │   │   │
│  │   │      - 无认证信息 → 返回 401 未授权             │   │   │
│  │   │                                                 │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  │                         │                                │   │
│  │                         ↓                                │   │
│  │   ┌─────────────────────────────────────────────────┐   │   │
│  │   │              Controller（办公室）                │   │   │
│  │   │                                                 │   │   │
│  │   │   @PostMapping("/realNameAuth")                 │   │   │
│  │   │   public R<Boolean> realNameAuth(               │   │   │
│  │   │       @Valid @RequestBody RealNameDTO dto       │   │   │
│  │   │   ) {                                           │   │   │
│  │   │       // 从"保险柜"中取出用户ID                 │   │   │
│  │   │       Long userId = SecurityUtils               │   │   │
│  │   │           .getCurrentUserId();                  │   │   │
│  │   │                                                 │   │   │
│  │   │       // 调用 Service 处理业务                  │   │   │
│  │   │       boolean result = sysUserService           │   │   │
│  │   │           .realNameAuth(userId, dto);           │   │   │
│  │   │                                                 │   │   │
│  │   │       return R.ok(result);                      │   │   │
│  │   │   }                                             │   │   │
│  │   │                                                 │   │   │
│  │   └─────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、核心概念解释

### 3.1 Token（通行证）

**是什么？**
- 一串加密的字符串，由服务器生成
- 包含用户信息（userId、username）
- 有过期时间（如 2 小时）

**长什么样？**
```
eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyX2lkIjoyMDgzODYyNjE1NTAyNzQxNTA1LCJ1c2VyX2tleSI6IjU5YWY0ODViZTE4MjQ5YjRiNjAyNTE3YTNjY2ViZTdlIiwidXNlcm5hbWUiOiIxMzgwMDEzODAwMCIsImlhdCI6MTc4NTY2NzAxMiwiZXhwIjoxNzg1Njc0MjEyfQ.PL6kJOW3ACc8972tcAguc3KzPo-csscketnl8Gox4OQV5lSftUrkoNNPNRndYz6Y
```

**结构：三段用 `.` 分隔**
```
Header.Payload.Signature
│      │       │
│      │       └── 签名（验证是否被篡改）
│      └────────── 载荷（用户信息、过期时间）
└───────────────── 头部（算法类型）
```

### 3.2 Security 上下文（保险柜）

**是什么？**
- Spring Security 提供的存储空间
- 用于保存当前登录用户的信息
- 在整个请求生命周期内有效

**怎么用？**
```java
// 存入用户信息
SecurityContextHolder.getContext().setAuthentication(auth);

// 取出用户信息
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
Long userId = (Long) auth.getDetails();
```

### 3.3 过滤器链（门卫团队）

**是什么？**
- 多个过滤器组成的一个团队
- 请求会依次通过每个过滤器
- 每个过滤器负责不同的职责

**执行顺序：**
```
请求 → JwtAuthFilter → Spring Security → Controller
         │                   │
         │                   └── 检查权限
         └── 验证 Token
```

---

## 四、代码详解

### 4.1 Gateway AuthFilter（第一道门卫）

**位置：** `gov-gateway/src/main/java/.../filter/AuthFilter.java`

**职责：**
- 验证 Token 有效性
- 解析用户信息
- 将用户信息放入请求头，传递给下游服务

**核心代码：**
```java
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getURI().getPath();

    // 1. 白名单路径直接放行
    if (isWhiteList(path)) {
        return chain.filter(exchange);
    }

    // 2. 获取 Token
    String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(token)) {
        return unauthorized(exchange);
    }

    // 3. 去掉 Bearer 前缀
    if (token.startsWith("Bearer ")) {
        token = token.substring(7);
    }

    // 4. 验证 Token
    if (jwtUtils.isTokenExpired(token)) {
        return unauthorized(exchange);
    }
    jwtUtils.parseToken(token);

    // 5. 解析用户信息
    Long userId = jwtUtils.getUserIdFromToken(token);
    String username = jwtUtils.getUsernameFromToken(token);

    // 6. 将用户信息放入请求头
    ServerHttpRequest mutatedRequest = request.mutate()
            .header("X-User-Id", String.valueOf(userId))
            .header("X-User-Name", username)
            .build();

    ServerWebExchange mutatedExchange = exchange.mutate()
            .request(mutatedRequest)
            .build();

    // 7. 继续执行
    return chain.filter(mutatedExchange);
}
```

### 4.2 JwtAuthFilter（第二道门卫）

**位置：** `gov-common/gov-common-security/src/main/java/.../filter/JwtAuthFilter.java`

**职责：**
- 从请求头获取 Token
- 验证 Token 有效性
- 将用户信息存入 Security 上下文

**核心代码：**
```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    // 1. 获取 Token
    String token = getTokenFromRequest(request);

    // 2. Token 为空，直接放行
    if (StringUtils.isBlank(token)) {
        filterChain.doFilter(request, response);
        return;
    }

    // 3. 验证 Token
    try {
        // 检查是否过期
        if (jwtUtils.isTokenExpired(token)) {
            log.warn("Token 已过期");
            filterChain.doFilter(request, response);
            return;
        }

        // 验证签名
        jwtUtils.parseToken(token);

        // 4. 解析用户信息
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);

        // 5. 存入 Security 上下文
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
        authentication.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (Exception e) {
        log.error("Token 验证失败：{}", e.getMessage());
    }

    // 6. 继续执行
    filterChain.doFilter(request, response);
}

private String getTokenFromRequest(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (StringUtils.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
        return authHeader.substring(7);  // 去掉 "Bearer " 前缀
    }
    return null;
}
```

### 4.3 SecurityUtils（工具类）

**位置：** `gov-common/gov-common-security/src/main/java/.../utils/SecurityUtils.java`

**职责：**
- 封装获取用户ID的方法
- 供 Controller 调用

**核心代码：**
```java
public class SecurityUtils {

    public static Long getCurrentUserId() {
        // 1. 从 Security 上下文获取认证信息
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) 
                    SecurityContextHolder.getContext().getAuthentication();

        // 2. 获取用户ID
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }

        // 3. 未登录，抛异常
        throw new BusinessException("用户未登录");
    }
}
```

### 4.4 SecurityConfig（配置类）

**位置：** `gov-common/gov-common-security/src/main/java/.../config/SecurityConfig.java`

**职责：**
- 注册 JwtAuthFilter
- 配置白名单路径

**核心代码：**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 注册 JWT 过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/sysUser/sendCode",     // 白名单
                    "/sysUser/register",
                    "/sysUser/login",
                    "/sysUser/smsLogin",
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

---

## 五、完整请求示例

### 5.1 场景：用户实名认证

**请求：**
```bash
curl -X POST http://localhost:8081/sysUser/realNameAuth \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9..." \
  -d '{"realName":"张三","idCard":"460100199001011234"}'
```

**执行流程：**

```
步骤 1: 请求到达 Gateway (8080)
        ↓
步骤 2: AuthFilter 检查路径
        /system/sysUser/realNameAuth → 不是白名单，需要 Token
        ↓
步骤 3: AuthFilter 获取 Token
        Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
        去掉 "Bearer "，得到 Token
        ↓
步骤 4: AuthFilter 验证 Token
        jwtUtils.isTokenExpired(token) → 未过期
        jwtUtils.parseToken(token) → 签名正确
        ↓
步骤 5: AuthFilter 解析用户信息
        userId = 2083862615502741505
        username = "13800138000"
        ↓
步骤 6: AuthFilter 放入请求头
        X-User-Id: 2083862615502741505
        X-User-Name: 13800138000
        ↓
步骤 7: 路由转发到 System 服务 (8081)
        /system/sysUser/realNameAuth → /sysUser/realNameAuth
        ↓
步骤 8: JwtAuthFilter 获取 Token
        从 Authorization 请求头获取
        ↓
步骤 9: JwtAuthFilter 验证 Token
        jwtUtils.isTokenExpired(token) → 未过期
        jwtUtils.parseToken(token) → 签名正确
        ↓
步骤 10: JwtAuthFilter 解析用户信息
         userId = 2083862615502741505
         username = "13800138000"
         ↓
步骤 11: JwtAuthFilter 存入 Security 上下文
         SecurityContextHolder.getContext().setAuthentication(auth)
         ↓
步骤 12: Spring Security 检查权限
         /sysUser/realNameAuth → 需要登录
         检查 Security 上下文 → 有认证信息 → 放行
         ↓
步骤 13: Controller 处理请求
         Long userId = SecurityUtils.getCurrentUserId()
         // userId = 2083862615502741505
         ↓
步骤 14: Service 处理业务
         realNameAuth(userId, realNameDTO)
         ↓
步骤 15: 返回响应
         {"code":200,"msg":"操作成功","data":true}
```

---

## 六、常见问题

### 6.1 为什么需要两个过滤器？

| 过滤器 | 位置 | 职责 |
|--------|------|------|
| Gateway AuthFilter | 网关层 | 验证 Token，转发用户信息 |
| JwtAuthFilter | 服务层 | 验证 Token，存入 Security 上下文 |

**为什么不能只用一个？**
- Gateway 是所有服务的入口，统一验证 Token
- 每个服务需要自己的 Security 上下文，用于权限控制

### 6.2 Token 过期了怎么办？

```
Token 过期
    ↓
jwtUtils.isTokenExpired(token) 返回 true
    ↓
JwtAuthFilter 不设置 Security 上下文
    ↓
Spring Security 检查发现未认证
    ↓
返回 401 Unauthorized
    ↓
前端收到 401，跳转到登录页
```

### 6.3 如何获取当前登录用户ID？

```java
// 方式一：使用 SecurityUtils（推荐）
Long userId = SecurityUtils.getCurrentUserId();

// 方式二：直接从 Security 上下文获取
UsernamePasswordAuthenticationToken auth = 
    (UsernamePasswordAuthenticationToken) 
        SecurityContextHolder.getContext().getAuthentication();
Long userId = (Long) auth.getDetails();
```

---

## 七、总结

### 7.1 三个门卫的职责

| 门卫 | 位置 | 职责 |
|------|------|------|
| Gateway AuthFilter | 网关 | 验证 Token，转发用户信息 |
| JwtAuthFilter | 服务 | 验证 Token，存入 Security 上下文 |
| Spring Security | 服务 | 检查权限，决定是否放行 |

### 7.2 数据流转

```
Token (前端)
    ↓
Gateway AuthFilter → 解析 userId, username → 放入请求头
    ↓
JwtAuthFilter → 从请求头获取 Token → 验证 → 存入 Security 上下文
    ↓
SecurityUtils.getCurrentUserId() → 从 Security 上下文获取 userId
    ↓
Controller → 使用 userId 调用 Service
    ↓
Service → 使用 userId 查询数据库
```

### 7.3 关键代码

```java
// 1. Gateway 放入请求头
request.mutate()
    .header("X-User-Id", String.valueOf(userId))
    .header("X-User-Name", username)
    .build();

// 2. JwtAuthFilter 存入 Security 上下文
SecurityContextHolder.getContext().setAuthentication(auth);

// 3. SecurityUtils 获取用户ID
Long userId = (Long) auth.getDetails();
```

---

## 八、JwtAuthFilter 与 Security 的执行顺序

### 8.1 过滤器链执行顺序

```
请求进入
    ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security 过滤器链                      │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  ① JwtAuthFilter（我们创建的）                          │   │
│   │     - 获取 Token                                        │   │
│   │     - Token 为空 → 直接放行，不设置认证信息              │   │
│   │     - Token 有效 → 验证 → 存入 Security 上下文          │   │
│   └─────────────────────────────────────────────────────────┘   │
│                           ↓                                     │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  ② 其他 Spring Security 过滤器（框架自带）              │   │
│   │     - UsernamePasswordAuthenticationFilter               │   │
│   │     - ExceptionTranslationFilter                         │   │
│   │     - FilterSecurityInterceptor（最终权限检查）          │   │
│   └─────────────────────────────────────────────────────────┘   │
│                           ↓                                     │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │  ③ FilterSecurityInterceptor（最终权限检查）            │   │
│   │     - 检查请求路径                                       │   │
│   │     - 检查是否有认证信息                                 │   │
│   │     - 决定是否放行                                       │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 8.2 场景一：没有 Token 的情况

```
请求: GET /sysUser/realNameAuth
      （没有 Authorization 请求头）
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   ① JwtAuthFilter 执行                                          │
│                                                                 │
│   String token = getTokenFromRequest(request);                  │
│   // token = null（因为没有 Authorization 请求头）               │
│                                                                 │
│   if (StringUtils.isBlank(token)) {                             │
│       filterChain.doFilter(request, response);  // 直接放行      │
│       return;                                                   │
│   }                                                             │
│                                                                 │
│   结果：没有设置 Security 上下文（没有认证信息）                 │
└─────────────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   ② FilterSecurityInterceptor 执行（最终权限检查）              │
│                                                                 │
│   // 1. 获取请求路径                                            │
│   String path = "/sysUser/realNameAuth";                        │
│                                                                 │
│   // 2. 检查 SecurityConfig 的配置                              │
│   //    /sysUser/realNameAuth 不在白名单中                      │
│   //    所以需要认证（.authenticated()）                        │
│                                                                 │
│   // 3. 检查是否有认证信息                                      │
│   Authentication auth = SecurityContextHolder                   │
│       .getContext().getAuthentication();                        │
│   // auth = null（因为 JwtAuthFilter 没有设置）                 │
│                                                                 │
│   // 4. 没有认证信息，抛出异常                                  │
│   throw new AccessDeniedException("Access is denied");          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   ③ ExceptionTranslationFilter 执行（异常处理）                 │
│                                                                 │
│   // 捕获 AccessDeniedException                                 │
│   catch (AccessDeniedException ex) {                            │
│       // 返回 403 Forbidden                                     │
│       response.setStatus(403);                                  │
│       response.getWriter().write("Access Denied");              │
│   }                                                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   响应: 403 Forbidden                                           │
└─────────────────────────────────────────────────────────────────┘
```

### 8.3 场景二：有 Token 的情况

```
请求: GET /sysUser/realNameAuth
      Authorization: Bearer eyJhbGciOiJIUz...
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   ① JwtAuthFilter 执行                                          │
│                                                                 │
│   String token = getTokenFromRequest(request);                  │
│   // token = "eyJhbGciOiJIUz..."                                │
│                                                                 │
│   // Token 不为空，继续验证                                     │
│   jwtUtils.parseToken(token);  // 验证签名                      │
│   Long userId = jwtUtils.getUserIdFromToken(token);             │
│   String username = jwtUtils.getUsernameFromToken(token);       │
│                                                                 │
│   // 存入 Security 上下文                                       │
│   UsernamePasswordAuthenticationToken auth =                    │
│       new UsernamePasswordAuthenticationToken(                  │
│           username, null, new ArrayList<>()                     │
│       );                                                        │
│   auth.setDetails(userId);                                      │
│   SecurityContextHolder.getContext().setAuthentication(auth);   │
│                                                                 │
│   结果：设置了认证信息（userId = 2083862615502741505）           │
└─────────────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   ② FilterSecurityInterceptor 执行（最终权限检查）              │
│                                                                 │
│   // 1. 获取请求路径                                            │
│   String path = "/sysUser/realNameAuth";                        │
│                                                                 │
│   // 2. 检查 SecurityConfig 的配置                              │
│   //    /sysUser/realNameAuth 不在白名单中                      │
│   //    所以需要认证（.authenticated()）                        │
│                                                                 │
│   // 3. 检查是否有认证信息                                      │
│   Authentication auth = SecurityContextHolder                   │
│       .getContext().getAuthentication();                        │
│   // auth = UsernamePasswordAuthenticationToken                 │
│   //   principal = "13800138000"                                │
│   //   details = 2083862615502741505                            │
│                                                                 │
│   // 4. 有认证信息，放行                                        │
│   // 继续执行下一个过滤器                                       │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
            ↓
┌─────────────────────────────────────────────────────────────────┐
│   ③ Controller 执行                                             │
│                                                                 │
│   @PostMapping("/realNameAuth")                                 │
│   public R<Boolean> realNameAuth(@Valid @RequestBody ...) {     │
│       Long userId = SecurityUtils.getCurrentUserId();           │
│       // userId = 2083862615502741505                           │
│       // ...                                                    │
│   }                                                             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 8.4 SecurityConfig 配置的作用

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/sysUser/sendCode",     // 白名单：不需要认证
        "/sysUser/register",
        "/sysUser/login",
        "/sysUser/smsLogin",
        "/doc.html",
        "/webjars/**",
        "/swagger-resources/**",
        "/v3/api-docs/**"
    ).permitAll()                // 允许所有人访问
    
    .anyRequest().authenticated()  // 其他请求需要认证
)
```

**作用：** 告诉 Spring Security 哪些路径需要认证，哪些不需要。

### 8.5 执行顺序总结

| 顺序 | 过滤器 | 职责 | 没有 Token | 有 Token |
|------|--------|------|-----------|---------|
| ① | JwtAuthFilter | 验证 Token | 直接放行 | 验证并存入上下文 |
| ② | FilterSecurityInterceptor | 权限检查 | 检查路径配置 | 检查路径配置 |
| ③ | FilterSecurityInterceptor | 认证检查 | 无认证信息 → 403 | 有认证信息 → 放行 |

### 8.6 常见问题

**Q1：为什么 JwtAuthFilter 要放在 Security 过滤器之前？**

A：因为 JwtAuthFilter 负责**设置认证信息**，Security 过滤器负责**检查认证信息**。必须先设置，再检查。

**Q2：白名单路径为什么不需要 Token？**

A：SecurityConfig 配置了 `permitAll()`，FilterSecurityInterceptor 会直接放行，不检查认证信息。

**Q3：没有 Token 时，为什么返回 403 而不是 401？**

A：
- 401 Unauthorized：未认证（没有提供凭证）
- 403 Forbidden：已认证但权限不足

在 Spring Security 中，没有认证信息时返回 403。
