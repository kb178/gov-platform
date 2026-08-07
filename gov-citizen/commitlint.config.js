export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    // 类型枚举
    'type-enum': [
      2,
      'always',
      [
        'feat', // 新功能
        'fix', // 修复 bug
        'docs', // 文档变更
        'style', // 代码格式（不影响功能）
        'refactor', // 重构（既不是新功能也不是修复）
        'perf', // 性能优化
        'test', // 添加测试
        'build', // 构建工具或外部依赖变更
        'ci', // CI 配置变更
        'chore', // 其他杂项
        'revert' // 回滚
      ]
    ],
    // 主题最大长度
    'subject-max-length': [2, 'always', 100],
    // 类型必须小写
    'type-case': [2, 'always', 'lower-case'],
    // 主题不能为空
    'subject-empty': [2, 'never']
  }
}
