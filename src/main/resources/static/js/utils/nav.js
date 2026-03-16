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
  options = Object.assign({
    params: {},
    cache: true,
    clearParams: false,
  }, options);
  if (options.clearParams) {
    destinationURL.search = '';
  }
  Object.entries(options.params).forEach(
      ([key, value]) => destinationURL.searchParams.set(key, value));
  if (!options.cache) {
    destinationURL.searchParams.set('_', new Date().getTime().toString());
  }
  window.location.href = destinationURL.toString();
}