import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { MainLayout, AuthLayout } from './layouts';
import { ProtectedRoute, GuestRoute } from './guards';
import { HomePage } from '@/pages/HomePage/HomePage';
import { LoginPage } from '@/pages/LoginPage/LoginPage';
import { ProfilePage } from '@/pages/ProfilePage/ProfilePage';
import { RoutesPage } from '@/pages/RoutesPage/RoutesPage';
import { RouteBuilderPage } from '@/pages/RouteBuilderPage/RouteBuilderPage';
import { RouteBuilderWorkspacePage } from '@/pages/RouteBuilderPage/RouteBuilderWorkspacePage';
import { RouteDetailsPage } from '@/pages/RouteDetailsPage/RouteDetailsPage';
import { MapDiscoveryPage } from '@/pages/MapDiscoveryPage/MapDiscoveryPage';
import { PlaceDetailsPage } from '@/pages/PlaceDetailsPage/PlaceDetailsPage';
import { NotFoundPage } from '@/pages/NotFoundPage/NotFoundPage';
import { appRoutes } from '@/shared/const/routes';
import { AuthProvider } from '@/features/auth/model/AuthProvider';

export function AppRouter() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<MainLayout />}>
            <Route path={appRoutes.home} element={<HomePage />} />
            <Route path={appRoutes.routes} element={<RoutesPage />} />
            <Route path={appRoutes.mapDiscovery} element={<MapDiscoveryPage />} />
            <Route path="/routes/:id" element={<RouteDetailsPage />} />
            <Route path="/places/:id" element={<PlaceDetailsPage />} />
            <Route element={<ProtectedRoute />}>
              <Route path={appRoutes.routeBuilder} element={<RouteBuilderPage />} />
              <Route path={appRoutes.routeBuilderCreate} element={<RouteBuilderWorkspacePage />} />
              <Route path={appRoutes.profile} element={<ProfilePage />} />
            </Route>
          </Route>

          <Route element={<GuestRoute />}>
            <Route element={<AuthLayout />}>
              <Route path={appRoutes.login} element={<LoginPage />} />
            </Route>
          </Route>

          <Route path="*" element={<NotFoundPage />} />
          <Route path="" element={<Navigate to={appRoutes.home} replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

