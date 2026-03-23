const logoutMessage = document.getElementById('logout-message');

if (logoutMessage) {
  setTimeout(() => {
    logoutMessage.style.display = 'none';
  }, 3000);
}

document.getElementById('toggle-password').addEventListener('click',
    togglePassword);

function togglePassword() {
  const passwordInput = document.getElementById('password');
  const toggleBtn = document.querySelector('.password-toggle');

  if (passwordInput.type === 'password') {
    passwordInput.type = 'text';
    toggleBtn.textContent = '🙈';
  } else {
    passwordInput.type = 'password';
    toggleBtn.textContent = '👁️';
  }
}

document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();

  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({username, password}),
  });

  if (response.ok) {
    window.location.href = '/admin';
  } else {
    showError('아이디 또는 비밀번호가 올바르지 않습니다.');
  }
});

function showError(message) {
  let errorEl = document.createElement('div');
  errorEl.className = 'error-message';
  document.querySelector('.login-card').insertBefore(
      errorEl,
      document.getElementById('login-form')
  );
  errorEl.textContent = message;
  errorEl.style.display = '';
  setTimeout(() => {
    errorEl.style.display = 'none';
  }, 3000);
}
