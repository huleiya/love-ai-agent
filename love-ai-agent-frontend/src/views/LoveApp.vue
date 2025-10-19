<template>
  <ChatRoom
    title="AI 恋爱大师"
    placeholder="输入你的问题，例如：如何优雅地道歉？"
    :buildSseUrl="buildUrl"
  />
  
</template>

<script setup lang="ts">
import ChatRoom from '../components/ChatRoom.vue';
import { withBase } from '../utils/env';

// LoveApp 后端为 Spring WebFlux SSE：GET /api/ai/love_app/chat/sse?message=xxx&chatId=yyy
function buildUrl(message: string, chatId: string) {
  const params = new URLSearchParams({ message, chatId });
  return withBase(`/ai/love_app/chat/sse?${params.toString()}`);
}
</script>


