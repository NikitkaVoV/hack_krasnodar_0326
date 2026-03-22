import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { appRoutes } from '@/shared/const/routes';
import { useAuth } from '@/features/auth/model/useAuth';
import { Button } from '@/shared/ui/Button/Button';
import styles from './Header.module.css';

export function Header() {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setIsScrolled(window.scrollY > 24);
    onScroll();
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const isHome = location.pathname === appRoutes.home;
  const isHeroState = isHome && !isScrolled;
  const variantClass = isHeroState ? styles.hero : styles.default;

  return (
    <header className={`${styles.header} ${variantClass}`}>
      <div className={styles.inner}>
        <button className={styles.logo} onClick={() => navigate(appRoutes.home)}>
          TourFront
        </button>

        <nav className={styles.nav}>
          <NavLink to={appRoutes.home}>Главная</NavLink>
          <NavLink to={appRoutes.routes}>Маршруты</NavLink>
          <NavLink to={appRoutes.routeBuilder}>Конструктор</NavLink>
          <NavLink to={appRoutes.mapDiscovery}>Карта</NavLink>
        </nav>

        <div className={styles.actions}>
          {isAuthenticated ? (
            <>
              <Button
                variant={isHeroState ? 'secondary' : 'ghost'}
                onClick={() => navigate(appRoutes.profile)}
                className={styles.profileButton}
              >
                {user?.name ? `Профиль: ${user.name}` : 'Профиль'}
              </Button>
              <Button variant="danger" onClick={logout} className={styles.logoutButton}>
                Выйти
              </Button>
            </>
          ) : (
            <Button
              onClick={() => navigate(appRoutes.login, { state: { from: location } })}
              variant={isHeroState ? 'secondary' : 'primary'}
              className={styles.loginButton}
            >
              Войти
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
