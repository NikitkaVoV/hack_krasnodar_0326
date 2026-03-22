import { Outlet, useLocation } from 'react-router-dom';
import { Header } from '@/widgets/Header/Header';
import { Footer } from '@/widgets/Footer/Footer';
import { appRoutes } from '@/shared/const/routes';
import { AssistantModal } from '@/widgets/AssistantModal/AssistantModal';
import styles from './layouts.module.css';

export function MainLayout() {
  const location = useLocation();
  const isHome = location.pathname === appRoutes.home;

  return (
    <div className={styles.root}>
      <Header />
      <main className={`${styles.main} ${isHome ? styles.mainHome : styles.mainDefault}`}>
        <Outlet />
      </main>
      <Footer />
      <AssistantModal />
    </div>
  );
}

export function AuthLayout() {
  return (
    <div className={styles.authRoot}>
      <main className={styles.authMain}>
        <Outlet />
      </main>
      <AssistantModal />
    </div>
  );
}
