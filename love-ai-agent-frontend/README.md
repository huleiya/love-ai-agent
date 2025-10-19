# AI 应用合集（前端）

基于 Vite + Vue3，包含两个 SSE 聊天页面：AI 恋爱大师与 AI 超级智能体。

## 快速开始

npm i
npm run dev

打开 http://localhost:5173

## 路由
- 主页：/
- 恋爱大师：/love（GET /api/ai/love_app/chat/sse?message=...&chatId=...）
- 超级智能体：/manus（GET /api/ai/manus/chat?message=...）

## 代理
- Vite 将前端 /api 代理到 http://localhost:8123
- 如后端端口不同，请修改 vite.config.ts 中 server.proxy['/api'].target
