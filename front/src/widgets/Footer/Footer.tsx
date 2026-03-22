import { Link } from 'react-router-dom';
import { appRoutes } from '@/shared/const/routes';
import styles from './Footer.module.css';

const navigationLinks = [
  { to: appRoutes.home, label: 'Главная' },
  { to: appRoutes.routes, label: 'Маршруты' },
  { to: `${appRoutes.routes}?type=events`, label: 'События' },
  { to: `${appRoutes.routes}?type=places`, label: 'Места' },
  { to: appRoutes.mapDiscovery, label: 'Карта' },
];

const userLinks = [
  { to: appRoutes.login, label: 'Персональный подбор' },
  { to: appRoutes.profile, label: 'Избранное' },
  { to: appRoutes.profile, label: 'История' },
  { to: appRoutes.login, label: 'Помощь' },
];

const projectLinks = [
  { to: appRoutes.home, label: 'О проекте' },
  { to: appRoutes.routeBuilder, label: 'Добавить место' },
  { to: appRoutes.home, label: 'Партнерство' },
  { to: appRoutes.home, label: 'Контакты' },
];

const contactLinks = [
  { href: 'mailto:hello@tourfront.ru', label: 'hello@tourfront.ru' },
  { href: 'https://t.me/tourfront', label: 'Telegram' },
  { href: 'https://vk.com/tourfront', label: 'VK' },
];

export function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.inner}>
        <section className={styles.brand} aria-label="Бренд">
          <p className={styles.logo}>TourFront</p>
          <p className={styles.description}>
            Платформа для осознанных путешествий: открывайте места, события и маршруты, которые подходят
            вашему ритму и интересам.
          </p>
        </section>

        <nav className={styles.column} aria-label="Навигация">
          <h3>Навигация</h3>
          {navigationLinks.map((link) => (
            <Link key={link.label} to={link.to}>
              {link.label}
            </Link>
          ))}
        </nav>

        <nav className={styles.column} aria-label="Пользователь">
          <h3>Пользователь</h3>
          {userLinks.map((link) => (
            <Link key={link.label} to={link.to}>
              {link.label}
            </Link>
          ))}
        </nav>

        <nav className={styles.column} aria-label="Проект">
          <h3>Проект</h3>
          {projectLinks.map((link) => (
            <Link key={link.label} to={link.to}>
              {link.label}
            </Link>
          ))}
        </nav>

        <address className={styles.column} aria-label="Контакты">
          <h3>Контакты</h3>
          {contactLinks.map((contact) => (
            <a key={contact.label} href={contact.href} target="_blank" rel="noreferrer">
              {contact.label}
            </a>
          ))}
        </address>
      </div>

      <div className={styles.bottomRow}>
        <p>© 2026 TourFront</p>
        <div className={styles.legalLinks}>
          <Link to={appRoutes.home}>Политика конфиденциальности</Link>
          <Link to={appRoutes.home}>Пользовательское соглашение</Link>
        </div>
      </div>
    </footer>
  );
}
