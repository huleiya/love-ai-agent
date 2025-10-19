<template>
  <div class="page">
    <div class="card chat">
      <div class="toolbar">
        <div class="title">{{ title }}</div>
        <div class="muted">会话ID：{{ chatId }}</div>
      </div>
      <div ref="messagesRef" class="messages">
        <div v-for="(m, idx) in messages" :key="idx" class="bubble" :class="m.role === 'user' ? 'user' : 'ai'">
          {{ m.content }}
        </div>
      </div>
      <div class="inputbar">
        <input
          v-model="input"
          :placeholder="placeholder"
          @keydown.enter="onSend"
        />
        <button class="btn primary" @click="onSend" :disabled="loading">发送</button>
      </div>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch, nextTick } from 'vue';
import { nanoid } from 'nanoid';

type Role = 'user' | 'ai';

interface ChatMessage {
  role: Role;
  content: string;
}

const props = defineProps<{
  title: string;
  placeholder?: string;
  // 构造 EventSource 的完整 URL 的函数：输入 message 和 chatId
  buildSseUrl: (message: string, chatId: string) => string;
  // 可选：为 true 时，每个 SSE onmessage 事件输出一个新的 AI 气泡
  perEventBubble?: boolean;
}>();

const input = ref('');
const messages = ref<ChatMessage[]>([]);
const loading = ref(false);
const chatId = ref('');
const messagesRef = ref<HTMLDivElement | null>(null);
let es: EventSource | null = null;

function ensureChatId() {
  if (!chatId.value) {
    // 生成短会话ID
    chatId.value = nanoid(10);
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = messagesRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

function closeStream() {
  if (es) {
    es.close();
    es = null;
  }
}

function onSend() {
  const text = input.value.trim();
  if (!text || loading.value) return;
  ensureChatId();
  messages.value.push({ role: 'user', content: text });
  input.value = '';
  startSse(text);
}

function startSse(message: string) {
  closeStream();
  loading.value = true;
  const usePerEventBubble = props.perEventBubble === true;
  let aiIndex = -1;
  if (!usePerEventBubble) {
    // 单气泡增量模式：预插一条空 AI 消息用于拼接
    messages.value.push({ role: 'ai', content: '' });
    aiIndex = messages.value.length - 1;
  }

  const url = props.buildSseUrl(message, chatId.value);
  // 使用原生 EventSource 以支持 SSE
  es = new EventSource(url);

  es.onmessage = (evt) => {
    // 兼容 Spring WebFlux 逐行 data: payload
    const chunk = evt.data;
    if (chunk === '[DONE]' || chunk === '__DONE__') {
      closeStream();
      loading.value = false;
      return;
    }
    if (usePerEventBubble) {
      const content = String(chunk ?? '').trim();
      if (content) messages.value.push({ role: 'ai', content });
    } else {
      messages.value[aiIndex].content += chunk;
    }
    scrollToBottom();
  };

  es.onerror = () => {
    closeStream();
    loading.value = false;
  };

  scrollToBottom();
}

onMounted(() => {
  ensureChatId();
});

onUnmounted(() => {
  closeStream();
});

watch(messages, scrollToBottom, { deep: true });
</script>

<style scoped>
</style>

