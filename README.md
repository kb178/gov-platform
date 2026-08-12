# 政务一体化平台（gov-platform）

> 基于 Spring Cloud 微服务架构的政务服务平台，主要用于学习微服务框架搭建和架构设计。

---

## 项目简介

本项目是一个类似"海易办"的政务一体化平台，实现了用户认证、权限管理、审批流程、证照管理等核心功能。

**定位：** 学习项目，重点在于微服务架构设计和实践，而非完整的业务功能。

---

## 技术栈

| 组件 | 技术选型 |
|------|---------|
| 框架 | Spring Boot 3.x + Spring Cloud 2022.x |
| 注册/配置中心 | Nacos |
| 网关 | Spring Cloud Gateway |
| 服务调用 | OpenFeign |
| 流程引擎 | Flowable |
| 缓存 | Redis |
| 消息队列 | RabbitMQ |
| 数据库 | MySQL + MyBatis-Plus |
| 搜索引擎 | Elasticsearch |
| 文件存储 | MinIO |
| 前端 | Vue3 + Element Plus |

---

## 服务架构

```
gov-platform（父工程）
├── gov-common/          公共模块（按需引入）
│   ├── gov-common-core       核心工具
│   ├── gov-common-redis      Redis
│   ├── gov-common-security   安全认证
│   └── gov-common-mybatis    持久层
├── gov-gateway          网关服务（8080）
├── gov-system           系统服务（8081）
├── gov-item             事项服务（8082）
├── gov-approval         审批服务（8083）
├── gov-license          证照服务（8084）
├── gov-message          消息服务（8085）
└── gov-data             数据服务（8086）
```

---

## 项目特点

- **公共模块分层设计** — 按需引入，避免依赖冗余
- **统一认证方案** — Spring Security + JWT + Redis
- **网关统一入口** — 路由转发、鉴权、跨域处理
- **审批工作流** — Flowable 引擎实现流程定义和流转
- **代码生成器** — MyBatis-Plus 自动生成 Entity/Mapper/Service/Controller

---

## 适用人群

- 想学习 Spring Cloud 微服务架构的开发者
- 想了解 Flowable 工作流引擎的同学
- 需要微服务项目参考的在校学生或初级开发

---

## 联系方式

如果你在学习微服务架构过程中有问题，或者需要了解项目的代码架构设计，欢迎交流。

---

## 免责声明

本项目为个人学习项目，仅供学习交流使用，请勿用于商业用途。
