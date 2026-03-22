import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import placeholderImage from '@/assets/placeholder.png';
import { useAuth } from '@/features/auth/model/useAuth';
import { useLogout } from '@/features/logout/useLogout';
import { useUserProfileQuery } from '@/shared/hooks/useUserProfileQuery';
import { useMyRoutesHistoryQuery } from '@/shared/hooks/useMyRoutesHistory';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { Card } from '@/shared/ui/Card/Card';
import { Button } from '@/shared/ui/Button/Button';
import { EmptyState } from '@/shared/ui/EmptyState/EmptyState';
import { ErrorState } from '@/shared/ui/ErrorState/ErrorState';
import { Spinner } from '@/shared/ui/Spinner/Spinner';
import { getApiErrorMessage } from '@/shared/lib/errors';
import { formatBudget, formatMinutes } from '@/shared/lib/format';
import { appRoutes } from '@/shared/const/routes';
import type { PublicRouteCard } from '@/entities/publicRoute/model';
import styles from './ProfilePage.module.css';

type RouteHistoryStatus = 'Пройден' | 'Сохранён' | 'В процессе';

interface RouteHistoryItem {
  id: string;
  title: string;
  date: string;
  description: string;
  durationMinutes: number;
  status: RouteHistoryStatus;
  imageUrl?: string;
}

const STATUS_CYCLE: RouteHistoryStatus[] = ['Пройден', 'Сохранён', 'В процессе'];

const MOCK_HISTORY: RouteHistoryItem[] = [
  {
    id: 'TR-9812',
    title: 'Вечерний Краснодар: гастро и набережная',
    date: '2026-03-19',
    description: 'Короткий городской маршрут с акцентом на локальные кухни и прогулочные зоны.',
    durationMinutes: 260,
    status: 'Пройден',
    imageUrl: placeholderImage,
  },
  {
    id: 'TR-9813',
    title: 'Исторический центр и дворики',
    date: '2026-03-17',
    description: 'Спокойный маршрут по архитектуре и культурным точкам города.',
    durationMinutes: 320,
    status: 'Сохранён',
    imageUrl: placeholderImage,
  },
  {
    id: 'TR-9814',
    title: 'Семейный уикенд в зелёных локациях',
    date: '2026-03-12',
    description: 'Парки, обзорные точки и места для отдыха с детьми.',
    durationMinutes: 410,
    status: 'В процессе',
    imageUrl: placeholderImage,
  },
];

const PASSPORT_MOCK = {
  passportNumber: 'TR-2026-00124',
  birthDate: '14.01.2001',
  issuedAt: '21.03.2026',
  travelStatus: 'Исследователь маршрутов',
  travelRegion: 'Краснодарский край',
};

function formatDate(date: string): string {
  const parsed = Date.parse(date);
  if (!Number.isFinite(parsed)) {
    return date;
  }
  return new Date(parsed).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

function formatCoordinates(lat?: number, lng?: number): string {
  if (typeof lat !== 'number' || typeof lng !== 'number') {
    return 'Не указана';
  }
  return `${lat.toFixed(4)}, ${lng.toFixed(4)}`;
}

function formatAdditionalInfo(value: Record<string, unknown> | undefined): string {
  if (!value || !Object.keys(value).length) {
    return 'Нет дополнительной информации';
  }

  const text = Object.entries(value)
    .slice(0, 3)
    .map(([key, item]) => `${key}: ${String(item)}`)
    .join(' · ');

  return text || 'Нет дополнительной информации';
}

function mapRealRoutesToHistory(routes: PublicRouteCard[]): RouteHistoryItem[] {
  const today = new Date();

  return routes.map((route, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() - index * 3);

    return {
      id: route.id,
      title: route.title,
      description: route.description,
      date: date.toISOString().slice(0, 10),
      durationMinutes: route.durationMinutes,
      status: STATUS_CYCLE[index % STATUS_CYCLE.length],
      imageUrl: route.imageUrl || placeholderImage,
    };
  });
}

function travelerLevelByRoutes(count: number): string {
  if (count >= 20) return 'Эксперт путешествий';
  if (count >= 10) return 'Продвинутый исследователь';
  if (count >= 4) return 'Активный путешественник';
  return 'Начинающий исследователь';
}

export function ProfilePage() {
  const { user } = useAuth();
  const logout = useLogout();
  const navigate = useNavigate();

  const profileQuery = useUserProfileQuery(user?.id);
  const historyQuery = useMyRoutesHistoryQuery();

  const profile = profileQuery.data;
  const displayName = profile?.name || user?.name || 'Путешественник';
  const displayLogin = profile?.login || user?.login || '—';
  const displayUserType = profile?.userType || user?.userType || 'Пользователь';

  const historyItems = useMemo<RouteHistoryItem[]>(() => {
    if (historyQuery.data?.length) {
      return mapRealRoutesToHistory(historyQuery.data).slice(0, 6);
    }
    return MOCK_HISTORY;
  }, [historyQuery.data]);

  const stats = useMemo(() => {
    const created = historyQuery.data?.length ?? MOCK_HISTORY.length;

    return {
      routesCreated: created,
      placesVisited: Math.max(8, created * 5 + 4),
      favoriteFormat: displayUserType === 'family' ? 'Семейный' : 'Смешанный городской',
      travelerLevel: travelerLevelByRoutes(created),
    };
  }, [displayUserType, historyQuery.data?.length]);

  const initialLetter = (displayName.trim().charAt(0) || 'U').toUpperCase();

  return (
    <PageContainer className={styles.page}>
      <section className={styles.profileHero}>
        <div className={styles.identity}>
          <div className={styles.avatar}>{initialLetter}</div>
          <div>
            <h1>{displayName}</h1>
            <p className={styles.login}>@{displayLogin}</p>
            <p className={styles.status}>Планирует насыщенные маршруты и открывает новые точки каждую неделю</p>
            <span className={styles.userType}>{displayUserType}</span>
          </div>
        </div>

        <div className={styles.heroActions}>
          <Button>Редактировать профиль</Button>
          <Button variant="ghost" onClick={() => navigate(appRoutes.routes)}>
            Смотреть маршруты
          </Button>
          <Button variant="danger" onClick={logout}>
            Выйти
          </Button>
        </div>
      </section>

      {profileQuery.isLoading ? <Spinner label="Загружаем профиль..." /> : null}
      {profileQuery.isError ? (
        <ErrorState
          title="Профиль загружен частично"
          message={getApiErrorMessage(profileQuery.error, 'Часть данных недоступна. Показываем сохранённый профиль.')}
        />
      ) : null}

      <section className={styles.mainGrid}>
        <Card className={styles.aboutCard}>
          <h2>О пользователе</h2>
          <div className={styles.aboutGrid}>
            <div>
              <span>Имя</span>
              <strong>{displayName}</strong>
            </div>
            <div>
              <span>Логин</span>
              <strong>{displayLogin}</strong>
            </div>
            <div>
              <span>Тип пользователя</span>
              <strong>{displayUserType}</strong>
            </div>
            <div>
              <span>Возраст</span>
              <strong>{typeof profile?.age === 'number' ? profile.age : 'Не указан'}</strong>
            </div>
            <div>
              <span>Бюджет путешествий</span>
              <strong>{formatBudget(profile?.budgetMin, profile?.budgetMax)}</strong>
            </div>
            <div>
              <span>Последняя геопозиция</span>
              <strong>{formatCoordinates(profile?.lastLocationLat, profile?.lastLocationLng)}</strong>
            </div>
            <div className={styles.wideField}>
              <span>Дополнительная информация</span>
              <strong>{formatAdditionalInfo(profile?.additionalInfo)}</strong>
            </div>
            <div className={styles.wideField}>
              <span>ID пользователя</span>
              <strong>{profile?.id || user?.id || 'Не определён'}</strong>
            </div>
          </div>
        </Card>

        <section className={styles.passportCard}>
          <header>
            <h2>Паспорт путешественника</h2>
            <p>Traveler Passport</p>
          </header>

          <div className={styles.passportBody}>
            <div>
              <span>Номер</span>
              <strong>{PASSPORT_MOCK.passportNumber}</strong>
            </div>
            <div>
              <span>ФИО</span>
              <strong>{displayName}</strong>
            </div>
            <div>
              <span>Дата рождения</span>
              <strong>{PASSPORT_MOCK.birthDate}</strong>
            </div>
            <div>
              <span>Дата выдачи</span>
              <strong>{PASSPORT_MOCK.issuedAt}</strong>
            </div>
            <div>
              <span>Статус</span>
              <strong>{PASSPORT_MOCK.travelStatus}</strong>
            </div>
            <div>
              <span>Регион</span>
              <strong>{PASSPORT_MOCK.travelRegion}</strong>
            </div>
          </div>
        </section>
      </section>

      <Card className={styles.statsCard}>
        <h2>Краткая статистика</h2>
        <div className={styles.statsGrid}>
          <div>
            <span>Маршрутов создано</span>
            <strong>{stats.routesCreated}</strong>
          </div>
          <div>
            <span>Мест посещено</span>
            <strong>{stats.placesVisited}</strong>
          </div>
          <div>
            <span>Любимый формат</span>
            <strong>{stats.favoriteFormat}</strong>
          </div>
          <div>
            <span>Уровень путешественника</span>
            <strong>{stats.travelerLevel}</strong>
          </div>
        </div>
      </Card>

      <section className={styles.historySection}>
        <div className={styles.sectionHead}>
          <h2>История маршрутов</h2>
          <p>Ваши сохранённые и пройденные маршруты. Раздел готов к подключению полноценного API истории.</p>
        </div>

        {historyQuery.isLoading ? <Spinner label="Загружаем историю маршрутов..." /> : null}

        {!historyItems.length ? (
          <EmptyState
            title="История маршрутов пока пуста"
            description="Создайте первый маршрут в конструкторе, чтобы он появился в истории."
          />
        ) : (
          <div className={styles.historyGrid}>
            {historyItems.map((item) => (
              <article className={styles.routeCard} key={item.id}>
                <img src={item.imageUrl || placeholderImage} alt={item.title} loading="lazy" />
                <div className={styles.routeBody}>
                  <div>
                    <p className={styles.routeId}>#{item.id}</p>
                    <h3>{item.title}</h3>
                    <p className={styles.routeDescription}>{item.description}</p>
                  </div>

                  <div className={styles.routeMeta}>
                    <span>{formatDate(item.date)}</span>
                    <span>{formatMinutes(item.durationMinutes)}</span>
                    <span className={styles.statusChip}>{item.status}</span>
                  </div>

                  <Button variant="ghost" onClick={() => navigate(appRoutes.routeDetails(item.id))}>
                    Открыть маршрут
                  </Button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </PageContainer>
  );
}


