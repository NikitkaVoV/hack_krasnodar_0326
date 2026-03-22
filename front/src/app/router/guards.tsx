import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { appRoutes } from '@/shared/const/routes';
import { useAuth } from '@/features/auth/model/useAuth';
import { Spinner } from '@/shared/ui/Spinner/Spinner';

export function ProtectedRoute() {
  const { isAuthenticated, isInitializing } = useAuth();
  const location = useLocation();

  if (isInitializing) {
    return <Spinner fullScreen label="Проверяем сессию..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to={appRoutes.login} replace state={{ from: location }} />;
  }

  return <Outlet />;
}

export function GuestRoute() {
  const { isAuthenticated, isInitializing } = useAuth();

  if (isInitializing) {
    return <Spinner fullScreen label="Загружаем..." />;
  }

  if (isAuthenticated) {
    return <Navigate to={appRoutes.home} replace />;
  }

  return <Outlet />;
}
