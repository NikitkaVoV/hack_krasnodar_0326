import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { useAuth } from '@/features/auth/model/useAuth';
import { Spinner } from '@/shared/ui/Spinner/Spinner';
import { ErrorState } from '@/shared/ui/ErrorState/ErrorState';
import { getApiErrorMessage } from '@/shared/lib/errors';
import { useHomePageData } from '@/shared/hooks/useHomePageData';
import { HomeHero } from '@/widgets/HomeHero/HomeHero';
import { QuickSearchPanel } from '@/widgets/QuickSearchPanel/QuickSearchPanel';
import { CategoriesSection } from '@/widgets/CategoriesSection/CategoriesSection';
import { PopularRoutesSection } from '@/widgets/PopularRoutesSection/PopularRoutesSection';
import { EventsSection } from '@/widgets/EventsSection/EventsSection';
import { RecommendationsSection } from '@/widgets/RecommendationsSection/RecommendationsSection';
import { PromoBanners } from '@/widgets/PromoBanners/PromoBanners';
import { HowItWorks } from '@/widgets/HowItWorks/HowItWorks';
import { FinalCta } from '@/widgets/FinalCta/FinalCta';
import { appRoutes } from '@/shared/const/routes';
import styles from './HomePage.module.css';

export function HomePage() {
  const { isAuthenticated } = useAuth();
  const [selectedDate] = useState(new Date().toISOString().slice(0, 10));
  const navigate = useNavigate();
  const homeQuery = useHomePageData({ date: selectedDate, isAuthenticated, limit: 6 });

  useEffect(() => {
    void homeQuery.refetch();
    // one-time explicit fetch on mount to guarantee backend request on page reload
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className={styles.page}>
      <HomeHero
        onPrimaryClick={() => navigate(appRoutes.routeBuilder)}
        onSecondaryClick={() => navigate(appRoutes.routes)}
      />

      <div className={styles.searchWrap}>
        <QuickSearchPanel
          onSubmit={({ query, date, duration, quickTags }) => {
            const searchParams = new URLSearchParams();
            searchParams.set('date', date);
            if (query) {
              searchParams.set('q', query);
            }
            if (duration !== 'any') {
              searchParams.set('duration', duration);
            }
            if (quickTags.length) {
              searchParams.set('tags', quickTags.join(','));
            }
            navigate(`${appRoutes.routes}?${searchParams.toString()}`);
          }}
        />
      </div>

      <PageContainer className={styles.sections}>
        {homeQuery.isLoading ? <Spinner label="Формируем витрину маршрутов..." /> : null}
        {homeQuery.isError ? (
          <ErrorState
            message={getApiErrorMessage(homeQuery.error, 'Не удалось загрузить часть данных главной страницы.')}
          />
        ) : null}

        {homeQuery.data ? (
          <>
            <CategoriesSection collections={homeQuery.data.collections} />
            <PopularRoutesSection
              routes={homeQuery.data.popularRoutes.length ? homeQuery.data.popularRoutes : homeQuery.data.publicRoutes}
            />
            <EventsSection events={homeQuery.data.upcomingEvents} />
            <RecommendationsSection
              isAuthenticated={isAuthenticated}
              recommendations={homeQuery.data.recommendedRoutes}
              recommendationError={homeQuery.data.recommendationError}
            />
            <PromoBanners />
            <HowItWorks />
            <FinalCta isAuthenticated={isAuthenticated} />
          </>
        ) : null}
      </PageContainer>
    </div>
  );
}
