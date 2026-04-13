import {isRefreshUrl, redirectToLogin, refreshSession} from '/js/utils/auth.js';

export const api = {};

async function request(url, options, config = {}) {
  const {retryOnUnauthorized = true} = config;
  const response = await fetch(url, options);

  if (response.status === 401 && retryOnUnauthorized && !isRefreshUrl(url)) {
    const refreshed = await refreshSession();
    if (refreshed) {
      return request(url, options, {retryOnUnauthorized: false});
    }
    alert('세션이 만료되었습니다. 로그인 페이지로 이동합니다.');
    redirectToLogin();
    throw new Error('Unauthorized');
  }

  if (!response.ok) {
    if (response.status === 401) {
      alert('세션이 만료되었습니다. 로그인 페이지로 이동합니다.');
      redirectToLogin();
      throw new Error('Unauthorized');
    }
    const message = await extractErrorMessage(response);
    alert(`[${response.status}] ${message}`);
    throw new Error(message);
  }
  return response;
}

async function extractErrorMessage(response) {
  const contentType = response.headers.get('Content-Type') || '';
  if (contentType.includes('application/json')) {
    try {
      const error = await response.json();
      return error.message || response.statusText;
    } catch (e) {
      console.error('API error response parsing failed:', e);
    }
  }
  try {
    const message = await response.text();
    return message || response.statusText;
  } catch (e) {
    console.error('API error response reading failed:', e);
    return response.statusText;
  }
}

async function parseJsonOrNull(response) {
  const contentType = response.headers.get('Content-Type') || '';
  if (!contentType.includes('application/json')) {
    return null;
  }
  return await response.json();
}

api.get = async function (url) {
  const options = {
    method: 'GET',
    cache: 'no-cache',
  };
  const response = await request(url, options);
  return await response.json();
};

api.post = async function (url, data) {
  const options = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  };
  const response = await request(url, options);
  return await parseJsonOrNull(response);
};

api.patch = async function (url, data) {
  const options = {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  };
  const response = await request(url, options);
  return await parseJsonOrNull(response);
};

api.put = async function (url, data) {
  const options = {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  };
  await request(url, options);
};

api['delete'] = async function (url) {
  const options = {
    method: 'DELETE',
  };
  await request(url, options);
};
