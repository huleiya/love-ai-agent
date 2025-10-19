import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { setSeoTags } from '../utils/seo';

const Home = () => import('../views/Home.vue');
const LoveApp = () => import('../views/LoveApp.vue');
const ManusApp = () => import('../views/ManusApp.vue');

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: Home, meta: { title: '灵犀恋爱助手 - 首页', description: '选择 AI 恋爱大师或 AI 超级智能体，开始对话' } },
  { path: '/love', name: 'love', component: LoveApp, meta: { title: 'AI 恋爱大师 - 实时聊天', description: '智能情感分析与恋爱建议，SSE 实时对话' } },
  { path: '/manus', name: 'manus', component: ManusApp, meta: { title: 'AI 超级智能体 - 多步推理', description: '多步骤推理、工具调用与问题求解，SSE 实时对话' } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.afterEach((to) => {
  const meta = to.meta || {};
  setSeoTags({
    title: (meta as any).title,
    description: (meta as any).description,
    url: to.fullPath,
  });
});

export default router;


