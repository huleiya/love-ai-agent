<template>
  <div class="page">
    <div class="card">
      <div class="toolbar">
        <div class="title">灵犀恋爱助手</div>
      </div>
      <div style="padding: 20px;">
        <section class="hero">
          <h1 class="gradient-text">做点酷的：和 AI 来一场真正有趣的对话</h1>
          <div class="hero-cta">
            <router-link class="cta primary" to="/love">立即进入恋爱大师</router-link>
            <router-link class="cta ghost" to="/manus">体验超级智能体</router-link>
          </div>
        </section>
        <div class="muted" style="margin: 8px 0 16px;">也可以直接选择下面的应用卡片</div>
        <div class="grid">
          <div class="card app-card neon">
            <div class="app-icon">❤️</div>
            <div class="title" style="margin: 4px 0;">AI 恋爱大师</div>
            <div class="app-desc">与 AI 恋爱大师进行甜蜜对话，获取恋爱建议</div>
            <ul class="bullets">
              <li>智能情感分析</li>
              <li>个性化恋爱建议</li>
              <li>实时对话响应</li>
            </ul>
            <router-link class="cta" to="/love">开始聊天</router-link>
          </div>
          <div class="card app-card neon">
            <div class="app-icon">🧠</div>
            <div class="title" style="margin: 4px 0;">AI 超级智能体</div>
            <div class="app-desc">与超级智能体进行对话，解决各种问题</div>
            <ul class="bullets">
              <li>多步推理能力</li>
              <li>工具调用与分析</li>
              <li>高级问题解决</li>
            </ul>
            <router-link class="cta" to="/manus">开始聊天</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
  
</template>

<script setup lang="ts">
// simple home navigation
</script>


