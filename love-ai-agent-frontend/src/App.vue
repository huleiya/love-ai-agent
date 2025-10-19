<template>
  <div class="layout-root">
    <main class="content">
      <router-view />
    </main>
    <AppFooter />
  </div>
  
</template>

<script setup lang="ts">
import AppFooter from './components/AppFooter.vue';
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.content {
  flex: 1 0 auto;
}
</style>

