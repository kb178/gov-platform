# 开发指南

## 🛠️ 工程化工具说明

### 1. ESLint - 代码检查

**作用**：自动检查代码中的错误和坏习惯

**使用**：
```bash
# 检查代码
npm run lint

# 自动修复
npm run lint -- --fix
```

**常见错误**：
- `no-unused-vars`: 未使用的变量
- `no-undef`: 未定义的变量
- `vue/attributes-order`: Vue 属性顺序

---

### 2. Prettier - 代码格式化

**作用**：自动统一代码格式

**使用**：
```bash
# 格式化所有代码
npm run format

# 格式化单个文件
npx prettier --write src/views/home/index.vue
```

**配置说明**（`.prettierrc.json`）：
```json
{
  "semi": false,           // 不加分号
  "singleQuote": true,     // 使用单引号
  "tabWidth": 2,           // 缩进 2 空格
  "printWidth": 100        // 每行最大 100 字符
}
```

---

### 3. Git 提交规范

**提交格式**：
```
<类型>: <描述>

[可选正文]

[可选脚注]
```

**类型说明**：
| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 添加用户登录功能` |
| `fix` | 修复 bug | `fix: 修复手机号验证失败` |
| `docs` | 文档 | `docs: 更新 README` |
| `style` | 样式 | `style: 优化首页样式` |
| `refactor` | 重构 | `refactor: 重构登录逻辑` |
| `perf` | 性能 | `perf: 优化列表渲染性能` |
| `test` | 测试 | `test: 添加登录功能测试` |
| `build` | 构建 | `build: 升级 Vite 版本` |
| `ci` | CI | `ci: 添加 GitHub Actions` |
| `chore` | 杂项 | `chore: 清理无用文件` |

**示例**：
```bash
# ✅ 正确
git commit -m "feat: 添加用户注册功能"
git commit -m "fix: 修复密码强度校验问题"

# ❌ 错误
git commit -m "update"
git commit -m "fix bug"
git commit -m "修改"
```

---

### 4. Vitest - 单元测试

**作用**：验证代码功能是否正常

**使用**：
```bash
# 运行测试
npm test

# 运行测试并生成覆盖率报告
npm run test:coverage

# 监听模式（开发时使用）
npm test -- --watch
```

**编写测试**：
```javascript
// src/utils/index.test.js
import { describe, it, expect } from 'vitest'
import { formatDate } from './index'

describe('formatDate', () => {
  it('应该正确格式化日期', () => {
    const result = formatDate('2024-01-15', 'YYYY-MM-DD')
    expect(result).toBe('2024-01-15')
  })
})
```

---

### 5. Docker - 容器化

**作用**：打包项目，确保环境一致

**使用**：
```bash
# 构建镜像
docker build -t gov-citizen .

# 运行容器
docker run -p 3000:80 gov-citizen

# 使用 docker-compose
docker-compose up -d

# 停止容器
docker-compose down
```

---

## 📁 项目结构

```
gov-citizen/
├── src/
│   ├── api/          # API 接口
│   ├── assets/       # 静态资源
│   ├── components/   # 公共组件
│   ├── layouts/      # 布局组件
│   ├── router/       # 路由配置
│   ├── stores/       # 状态管理
│   ├── utils/        # 工具函数
│   └── views/        # 页面组件
├── public/           # 公共静态文件
├── .eslintrc.js      # ESLint 配置
├── .prettierrc.json  # Prettier 配置
├── Dockerfile        # Docker 配置
├── docker-compose.yml
└── package.json
```

---

## 🚀 开发流程

1. **拉取代码**
   ```bash
   git pull origin develop
   ```

2. **创建分支**
   ```bash
   git checkout -b feature/功能名称
   ```

3. **开发代码**
   - 编写功能代码
   - 运行 `npm run lint` 检查
   - 运行 `npm run format` 格式化

4. **编写测试**
   ```bash
   # 创建测试文件
   touch src/utils/index.test.js
   
   # 运行测试
   npm test
   ```

5. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 添加xxx功能"
   ```
   
   > 提交时会自动运行 ESLint 检查，如果有错误会阻止提交

6. **推送代码**
   ```bash
   git push origin feature/功能名称
   ```

---

## ❓ 常见问题

### Q: ESLint 报错怎么办？
A: 运行 `npm run lint -- --fix` 自动修复，或手动修改错误

### Q: 提交被拒绝怎么办？
A: 检查提交信息格式是否正确，必须以 `feat:`, `fix:` 等开头

### Q: 如何跳过 ESLint 检查？
A: 不建议跳过，但紧急情况可以使用 `git commit --no-verify`

### Q: 测试怎么写？
A: 参考 `src/utils/index.test.js` 示例
