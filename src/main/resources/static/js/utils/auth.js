const REFRESH_URL = '/api/auth/refresh';
const LOGIN_URL = '/admin/login';

let refreshPromise = null;

export async function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = fetch(REFRESH_URL, {
      method: 'POST',
      cache: 'no-cache',
    })
    .then(response => response.ok)
    .catch(() => false)
    .finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

export function isRefreshUrl(url) {
  const targetUrl = new URL(url, window.location.origin);
  return targetUrl.origin === window.location.origin
      && targetUrl.pathname === REFRESH_URL;
}

export function redirectToLogin(params = {}) {
  const loginURL = new URL(LOGIN_URL, window.location.origin);
  for (const key in params) {
    if (Object.prototype.hasOwnProperty.call(params, key)) {
      loginURL.searchParams.set(key, params[key]);
    }
  }
  window.location.href = loginURL.toString();
}
