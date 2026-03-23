import {goTo} from '/js/utils/nav.js';
import {getCsrfToken} from '/js/utils/api.js';

// 로그아웃
document.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
      const csrfToken = getCsrfToken();
      if (!csrfToken) {
        // await fetch('/api/auth/csrf', { method: 'GET' });
      }
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: {'X-XSRF-TOKEN': getCsrfToken()},
      });
      goTo('/admin/login', {params: {logout: true}});
    });
  }
});

// 다크모드 토글 기능
document.addEventListener('DOMContentLoaded', () => {
  const themeToggle = document.getElementById('themeToggle');
  const themeIcon = document.getElementById('themeIcon');
  const html = document.documentElement;

  // 저장된 테마 불러오기
  const savedTheme = localStorage.getItem('theme') || 'light';
  html.setAttribute('data-theme', savedTheme);
  themeIcon.textContent = savedTheme === 'dark' ? '☀️' : '🌙';

  // 테마 토글 이벤트
  themeToggle.addEventListener('click', () => {
    const currentTheme = html.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

    html.setAttribute('data-theme', newTheme);
    themeIcon.textContent = newTheme === 'dark' ? '☀️' : '🌙';
    localStorage.setItem('theme', newTheme);
  });
});
