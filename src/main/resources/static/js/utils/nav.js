export function goTo(path = null, options = {}) {
  if (typeof path === 'object') {
    options = path;
    path = window.location.href;
  }
  let destinationURL;
  if (path.startsWith('/')) {
    destinationURL = new URL(path, window.location.origin);
  } else if (path.match(/^https?:\/\//)) {
    destinationURL = new URL(path);
  } else {
    destinationURL = new URL(path, window.location.href);
  }
  const defaults = {
    params: {},
    cache: true,
    clearParams: false,
  };
  options = {...defaults, ...options};
  if (options.clearParams) {
    destinationURL.search = '';
  }
  const searchParams = options.params;
  for (const key in searchParams) {
    // searchParams.hasOwnProperty 보다 안전한 방법
    // null 객체(prototype chain 없음), hasOwnProperty 재정의 하는 엣지 케이스 방지
    if (Object.prototype.hasOwnProperty.call(searchParams, key)) {
      const value = searchParams[key];
      destinationURL.searchParams.set(key, value);
    }
  }
  if (!options.cache) {
    destinationURL.searchParams.set('_', new Date().getTime().toString());
  }
  window.location.href = destinationURL.toString();
}