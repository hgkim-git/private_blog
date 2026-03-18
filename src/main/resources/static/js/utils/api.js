export const api = {};

api.get = async function (url) {
  const options = {
    method: 'GET',
    cache: 'no-cache',
  };
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json();
    alert(`[${response.status}] ${error.message}`);
    throw new Error(error.message);
  }
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
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json();
    alert(`[${response.status}] ${error.message}`);
    throw new Error(error.message);
  }
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
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json();
    alert(`[${response.status}] ${error.message}`);
    throw new Error(error.message);
  }
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
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json();
    alert(`[${response.status}] ${error.message}`);
    throw new Error(error.message);
  }
};

api['delete'] = async function (url) {
  const options = {
    method: 'DELETE',
  };
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json();
    alert(`[${response.status}] ${error.message}`);
    throw new Error(error.message);
  }
};
