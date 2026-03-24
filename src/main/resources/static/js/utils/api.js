export const api = {};

async function request(url, options) {
  const response = await fetch(url, options);
  if (!response.ok) {
    if (response.status === 401) {
      alert('세션이 만료되었습니다. 로그인 페이지로 이동합니다.');
      window.location.href = '/admin/login';
      throw new Error('Unauthorized');
    }
    const error = await response.json();
    alert(`[${response.status}] ${error.message}`);
    throw new Error(error.message);
  }
  return response;
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
  return await response.json();
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
  return await response.json();
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
