import { useAuth } from '@/features/auth/model/useAuth';

export function useLogout() {
  const { logout } = useAuth();
  return logout;
}


