# Git 常用命令指南

## 📋 基础配置

```bash
# 查看 Git 配置
git config --list

# 设置用户名和邮箱
git config --global user.name "你的用户名"
git config --global user.email "你的邮箱"
```

---

## 🔄 分支操作

### 查看分支

```bash
# 查看本地分支
git branch

# 查看远程分支
git branch -r

# 查看所有分支（本地 + 远程）
git branch -a

# 查看分支关联关系
git branch -vv
```

### 创建分支

```bash
# 创建新分支
git branch 分支名

# 创建并切换到新分支
git checkout -b 分支名

# 基于远程分支创建本地分支
git checkout -b 本地分支名 origin/远程分支名
```

### 切换分支

```bash
# 切换到指定分支
git checkout 分支名

# 切换到上一个分支
git checkout -
```

### 删除分支

```bash
# 删除本地分支（已合并的）
git branch -d 分支名

# 强制删除本地分支
git branch -D 分支名

# 删除远程分支
git push origin --delete 分支名
```

---

## 📦 代码提交

### 查看状态

```bash
# 查看工作区状态
git status

# 查看修改内容
git diff

# 查看暂存区内容
git diff --staged
```

### 添加文件

```bash
# 添加指定文件
git add 文件名

# 添加所有修改
git add .

# 添加所有修改（包括删除）
git add -A
```

### 提交代码

```bash
# 提交暂存区内容
git commit -m "提交信息"

# 添加并提交所有修改
git commit -am "提交信息"

# 修改上一次提交
git commit --amend
```

---

## 🚀 远程操作

### 拉取代码

```bash
# 拉取远程代码
git pull origin 分支名

# 拉取远程代码（不合并）
git fetch origin

# 拉取并变基
git pull --rebase origin 分支名
```

### 推送代码

```bash
# 推送到远程分支
git push origin 分支名

# 推送并设置上游分支
git push -u origin 分支名

# 强制推送（慎用）
git push --force origin 分支名
```

### 关联远程仓库

```bash
# 添加远程仓库
git remote add origin 仓库地址

# 查看远程仓库
git remote -v

# 修改远程仓库地址
git remote set-url origin 新地址
```

---

## 🔀 合并与变基

### 合并分支

```bash
# 合并指定分支到当前分支
git merge 分支名

# 合并但不提交
git merge --no-commit 分支名

# 放弃合并
git merge --abort
```

### 变基操作

```bash
# 将当前分支变基到指定分支
git rebase 目标分支

# 继续变基（解决冲突后）
git rebase --continue

# 放弃变基
git rebase --abort
```

---

## 📝 提交规范

### 提交类型

```
feat:     新功能
fix:      修复 bug
docs:     文档修改
style:    代码格式修改（不影响逻辑）
refactor: 代码重构
perf:     性能优化
test:     测试相关
chore:    构建过程或辅助工具的变更
```

### 提交示例

```bash
git commit -m "feat: 添加用户登录功能"
git commit -m "fix: 修复登录验证失败问题"
git commit -m "docs: 更新 README 文档"
git commit -m "refactor: 重构用户服务代码"
```

---

## 🛠️ 撤销操作

### 撤销修改

```bash
# 撤销工作区修改（未 add）
git checkout -- 文件名

# 撤销暂存区修改（已 add，未 commit）
git reset HEAD 文件名

# 撤销提交（保留修改）
git reset --soft HEAD~1

# 撤销提交（不保留修改）
git reset --hard HEAD~1
```

### 回退版本

```bash
# 回退到指定版本
git reset --hard 提交哈希

# 回退到上一个版本
git reset --hard HEAD~1

# 回退到上上个版本
git reset --hard HEAD~2
```

---

## 📊 查看历史

### 查看提交记录

```bash
# 查看提交历史
git log

# 查看简洁的提交历史
git log --oneline

# 查看最近 n 次提交
git log -n

# 查看图形化的分支历史
git log --graph --oneline --all
```

### 查看文件历史

```bash
# 查看文件的修改历史
git log 文件名

# 查看文件的详细修改
git log -p 文件名
```

---

## 🔧 常用技巧

### 暂存工作区

```bash
# 暂存当前修改
git stash

# 查看暂存列表
git stash list

# 恢复最近一次暂存
git stash pop

# 恢复指定暂存
git stash apply stash@{0}

# 删除暂存
git stash drop stash@{0}
```

### 标签操作

```bash
# 创建标签
git tag 标签名

# 创建带注释的标签
git tag -a 标签名 -m "标签说明"

# 推送标签到远程
git push origin 标签名

# 推送所有标签
git push origin --tags

# 删除本地标签
git tag -d 标签名

# 删除远程标签
git push origin --delete 标签名
```

---

## 🎯 工作流程

### 开发新功能

```bash
# 1. 切换到 develop 分支
git checkout develop

# 2. 拉取最新代码
git pull origin develop

# 3. 创建功能分支
git checkout -b feature/功能名

# 4. 开发功能...

# 5. 提交代码
git add .
git commit -m "feat: 完成功能"

# 6. 推送到远程
git push -u origin feature/功能名

# 7. 去 GitHub 创建 PR
```

### 修复 Bug

```bash
# 1. 切换到 develop 分支
git checkout develop

# 2. 拉取最新代码
git pull origin develop

# 3. 创建修复分支
git checkout -b fix/bug描述

# 4. 修复 bug...

# 5. 提交代码
git add .
git commit -m "fix: 修复xxx问题"

# 6. 推送到远程
git push -u origin fix/bug描述

# 7. 去 GitHub 创建 PR
```

---

## ⚠️ 注意事项

1. **提交前先拉取最新代码**
2. **提交信息要清晰明了**
3. **不要提交敏感信息**（密码、密钥等）
4. **定期推送到远程仓库备份**
5. **合并前先解决冲突**

---

## 📚 参考资源

- [Git 官方文档](https://git-scm.com/doc)
- [Git 教程](https://www.liaoxuefeng.com/wiki/896043488029600)
- [Git 在线练习](https://learngitbranching.js.org/?locale=zh_CN)
