// Centralized environment helpers for API base URL
// Usage: import { API_BASE_URL, withBase } from './env'

const MODE = import.meta.env.MODE;
const PROD = import.meta.env.PROD;

const DEFAULT_DEV = 'http://localhost:8123/api';
const DEFAULT_PROD = '/api';

export const API_BASE_URL: string = (import.meta.env.VITE_API_BASE_URL as string) || (PROD ? DEFAULT_PROD : DEFAULT_DEV);

export function withBase(path: string): string {
  const base = API_BASE_URL.replace(/\/$/, '');
  const p = path.startsWith('/') ? path : `/${path}`;
  return `${base}${p}`;
}

export const isProd = PROD;
export const mode = MODE;


