# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目概述

政务一体化平台（海口政务服务微服务项目），类似"海易办"的简化版。实现老百姓网上办事、工作人员在线审批、电子证照管理等功能。

---

## 常用命令

### 构建与运行
```bash
# 编译整个项目（跳过测试）
mvn clean install -DskipTests

# 编译单个模块
mvn clean install -DskipTests -pl gov-system

# 启动系统服务（需要先启动 MySQL、Redis、Nacos）
mvn spring-boot:run -pl gov-system
# 或
java -jar gov-system/target/gov-system-1.0.0.jar
```

### 代码生成
```bash
# 运行 MyBatis-Plus 代码生成器（在 gov-common-mybatis 模块）
# 修改 CodeGenerator.java 中的配置后运行 main 方法
```

### Docker 依赖服务
```bash
# MySQL（密码：password，数据持久化到本地）
docker start mysql

# Redis（无密码）
docker start redis

# Nacos（需要 --platform linux/amd64，M1/M2 Mac）
docker start nacos
```

---

## 模块结构

```
gov-platform (父POM，版本管理)
├── gov-common/                    # 公共模块（聚合）
│   ├── gov-common-core/           # 核心：BaseEntity、R、JwtUtils、异常处理、枚举
│   ├── gov-common-redis/          # Redis：RedisUtils 工具类
│   ├── gov-common-security/       # 安全：SecurityConfig、PasswordUtils
│   └── gov-common-mybatis/        # MyBatis：AutoFillHandler、CodeGenerator
├── gov-gateway/                   # 网关（端口 8080）
├── gov-system/                    # 系统服务（端口 8081）← 主要开发模块
├── gov-item/                      # 事项服务（待开发）
├── gov-approval/                  # 审批服务（待开发）
├── gov-license/                   # 证照服务（待开发）
├── gov-message/                   # 消息服务（待开发）
└── gov-data/                      # 数据服务（待开发）
```

### 依赖关系
```
gov-system → gov-common-security → gov-common-redis → gov-common-core
           → gov-common-mybatis → gov-common-core
```

---

## 代码架构（分层）

```
Controller → Service → Mapper → Database
    ↓           ↓         ↓
  DTO/VO    Domain    XML映射
```

| 层 | 职责 | 命名规范 |
|---|------|---------|
| Controller | 接收请求，调用Service，返回R对象 | `SysUserController` |
| DTO | 请求参数（Data Transfer Object） | `LoginDTO`、`RegisterDTO` |
| VO | 响应数据（View Object） | `LoginVO` |
| Service | 业务逻辑 | `SysUserService` / `SysUserServiceImpl` |
| Mapper | 数据访问（MyBatis-Plus BaseMapper） | `SysUserMapper` |
| Domain | 实体类（继承BaseEntity） | `SysUser` |

---

## 关键技术点

### MyBatis-Plus 配置
- **ID 策略**：`ASSIGN_ID`（雪花算法，分布式ID）
- **逻辑删除**：`delFlag` 字段（0=存在，1=删除），全局配置
- **自动填充**：`AutoFillHandler` 自动填充 createTime、updateTime、delFlag
- **字段映射**：下划线转驼峰（`user_id` → `userId`）

### 常用工具类（在 gov-common-core）
| 类 | 说明 |
|---|------|
| `R<T>` | 统一返回结果（code/msg/data） |
| `BaseEntity` | 实体基类（createTime/updateTime/delFlag等） |
| `JwtUtils` | JWT Token 生成/解析（createToken/parseToken） |
| `LoginUser` | 登录用户信息（userId/username/roles/perms） |
| `BusinessException` | 业务异常（抛出后由 GlobalExceptionHandler 捕获） |

### 安全模块（在 gov-common-security）
| 类 | 说明 |
|---|------|
| `SecurityConfig` | Spring Security 配置（permitAll 白名单） |
| `PasswordUtils` | BCrypt 密码加密/比对（encode/matches） |

### Nacos 配置
- 命名空间：`gov-platform`
- 配置文件：`gov-system.yml`（通过 `spring.config.import` 导入）
- 服务发现：`@EnableDiscoveryClient`

---

## 编码规范

### 注释要求
- 类：说明用途、作者、日期
- 方法：说明功能、参数、返回值
- 复杂逻辑：逐行注释解释业务流程

### 命名规范
- 表名：`sys_user`（下划线）
- 实体类：`SysUser`（大驼峰）
- 字段：`user_id` → `userId`（驼峰映射）
- DTO/VO：`LoginDTO`、`LoginVO`

### 统一返回格式
```java
// 成功
return R.ok(data);
return R.ok(data, "操作成功");

// 失败
return R.fail("操作失败");
throw new BusinessException("手机号已注册");
```

---

## 数据库

- 数据库名：`gov_system`
- 连接配置：`application.yml`（username: root, password: password）
- 表设计文档：`docs/政务一体化平台-数据库设计.md`
- 功能清单：`docs/政务一体化平台-功能清单.md`
- 问题记录：`docs/问题记录.md`
- 架构说明：`docs/架构设计说明.md`

---

## 注意事项

1. **Spring Boot 3.x**：使用 `jakarta.validation`，不是 `javax.validation`
2. **组件扫描**：启动类需要 `@ComponentScan` 扫描 common 模块
3. **复合主键**：`UserRole`、`RoleMenu` 使用 `@TableField` 而非 `@TableId`
4. **端口规划**：Gateway 8080，System 8081，其他服务 8082+
5. **JWT 配置**：密钥和过期时间在 `application.yml` 的 `jwt` 节点
