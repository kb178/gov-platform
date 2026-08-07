import { fileURLToPath } from 'node:url'
import { dirname } from 'node:path'
import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import eslintConfigPrettier from '@vue/eslint-config-prettier'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

export default [
  {
    ignores: ['dist/**', 'node_modules/**', '*.md', '*.json', '*.yaml', '*.yml']
  },
  js.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    languageOptions: {
      globals: {
        // 浏览器全局变量
        window: 'readonly',
        document: 'readonly',
        localStorage: 'readonly',
        sessionStorage: 'readonly',
        console: 'readonly',
        setTimeout: 'readonly',
        clearTimeout: 'readonly',
        setInterval: 'readonly',
        clearInterval: 'readonly',
        alert: 'readonly',
        confirm: 'readonly',
        URL: 'readonly',
        Blob: 'readonly',
        FormData: 'readonly',
        XMLHttpRequest: 'readonly',
        fetch: 'readonly',
        navigator: 'readonly',
        location: 'readonly',
        requestAnimationFrame: 'readonly',
        cancelAnimationFrame: 'readonly',
        performance: 'readonly'
      }
    },
    rules: {
      // 关闭多单词组件名限制
      'vue/multi-word-component-names': 'off',
      // 允许 console
      'no-console': 'off',
      // 警告 debugger
      'no-debugger': 'warn',
      // 未使用变量警告
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      // Vue 自闭合标签
      'vue/html-self-closing': [
        'error',
        {
          html: { void: 'always', normal: 'never', component: 'always' }
        }
      ]
    }
  },
  eslintConfigPrettier
]
