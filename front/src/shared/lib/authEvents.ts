const AUTH_LOGOUT_EVENT = 'app:auth-logout';

export function emitAuthLogout(): void {
  window.dispatchEvent(new CustomEvent(AUTH_LOGOUT_EVENT));
}

export function onAuthLogout(listener: () => void): () => void {
  const handler = () => listener();
  window.addEventListener(AUTH_LOGOUT_EVENT, handler);
  return () => window.removeEventListener(AUTH_LOGOUT_EVENT, handler);
}


