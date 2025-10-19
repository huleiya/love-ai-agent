import axios from 'axios';
import { API_BASE_URL } from './env';

// 这里主要用于非 SSE 的接口调用占位；SSE 我们直接使用 EventSource
const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
});

instance.interceptors.response.use(
  (resp) => resp,
  (err) => Promise.reject(err)
);

export default instance;


