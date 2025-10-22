<template>
  <footer class="site-footer">
    <div class="container">
      <div class="left">
        <span class="brand">灵犀恋爱助手</span>
        <span class="sep">·</span>
        <span>© 2025</span>
      </div>
      <div class="right">
        <a href="https://www.hulei.tech" target="_blank" rel="noopener">作者：hulei</a>
        <span class="sep">·</span>
        <a href="/" @click.prevent>隐私政策</a>
        <span class="sep">·</span>
        <a href="/" @click.prevent>使用条款</a>
      </div>
    </div>
  </footer>
  
</template>

<script setup lang="ts">
// Simple footer component
</script>

<style scoped>
.site-footer {
  margin-top: 24px;
  border-top: 1px solid #1f2937;
  color: #94a3b8;
}
.container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.brand { font-weight: 600; color: #e2e8f0; }
.sep { margin: 0 8px; opacity: .6; }
.right a { color: #9aa8be; text-decoration: none; }
.right a:hover { color: #e2e8f0; }
@media (max-width: 640px) {
  .container { flex-direction: column; gap: 6px; }
}
</style>


