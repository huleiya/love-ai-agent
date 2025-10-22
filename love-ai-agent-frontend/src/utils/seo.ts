export interface SeoInfo {
  title?: string;
  description?: string;
  url?: string;
}

function setMeta(name: string, content: string) {
  let el = document.querySelector(`meta[name="${name}"]`) as HTMLMetaElement | null;
  if (!el) {
    el = document.createElement('meta');
    el.setAttribute('name', name);
    document.head.appendChild(el);
  }
  el.setAttribute('content', content);
}

function setOg(property: string, content: string) {
  let el = document.querySelector(`meta[property="${property}"]`) as HTMLMetaElement | null;
  if (!el) {
    el = document.createElement('meta');
    el.setAttribute('property', property);
    document.head.appendChild(el);
  }
  el.setAttribute('content', content);
}

export function setSeoTags(info: SeoInfo) {
  const title = info.title ?? document.title ?? '灵犀恋爱助手';
  const desc = info.description ?? '灵犀恋爱助手：AI 恋爱大师与 AI 超级智能体，支持 SSE 实时对话';
  const url = info.url ?? (typeof location !== 'undefined' ? location.pathname : '/');

  document.title = title;
  setMeta('description', desc);
  setOg('og:title', title);
  setOg('og:description', desc);
  setOg('og:url', url);
}


