<template>
  <div class="error-page">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">
      返回首页
    </el-button>
  </div>
</template>

<script setup>
// 404 页面
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;

  h1 {
    font-size: 72px;
    color: #409EFF;
  }

  p {
    margin: 16px 0 24px;
    font-size: 18px;
    color: #666;
  }
}
</style>
