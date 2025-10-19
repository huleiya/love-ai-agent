<template>
  <ChatRoom
    title="AI 超级智能体"
    placeholder="输入你的任务，例如：帮我制定三天的杭州约会计划"
    :buildSseUrl="buildUrl"
    :perEventBubble="true"
  />
  
</template>

<script setup lang="ts">
import ChatRoom from '../components/ChatRoom.vue';
import { withBase } from '../utils/env';

// Manus 接口为 SseEmitter，同样可用 EventSource 直连
// GET /api/ai/manus/chat?message=xxx
function buildUrl(message: string) {
  const params = new URLSearchParams({ message });
  return withBase(`/ai/manus/chat?${params.toString()}`);
}
</script>


