# gov-admin

政务一体化平台 - 管理员端前端项目

## 技术栈

- **框架**：Vue 3.3+ (Composition API)
- **构建**：Vite 5.0+
- **UI库**：Element Plus 2.4+
- **路由**：Vue Router 4.2+
- **状态**：Pinia 2.1+
- **HTTP**：Axios 1.6+
- **图表**：ECharts 5.4+
- **样式**：Sass

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器（端口 3001）
npm run dev

# 构建生产版本
npm run build
```

## 项目结构

```
src/
├── api/          # API 接口
├── assets/       # 静态资源
├── components/   # 公共组件
├── layouts/      # 布局组件
├── router/       # 路由配置
├── stores/       # 状态管理
├── utils/        # 工具函数
└── views/        # 页面组件
```

## 功能模块

- 工作台（数据概览、待办提醒）
- 审批管理（待我审批、已办事项、流程监控）
- 事项管理（分类、列表、指南、模板）
- 证照管理（模板、列表）
- 数据统计（办件统计、数据大屏）
- 系统管理（用户、角色、部门、菜单、字典、日志、参数）
- 消息中心（公告、站内消息）

## 开发规范

- 组件命名：PascalCase
- 文件命名：kebab-case
- CSS方案：Scoped SCSS
- 提交规范：Conventional Commits

## 相关文档

- [框架搭建说明](docs/框架搭建说明.md)
