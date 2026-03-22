import { useMemo } from 'react';
import { PageContainer } from '@/shared/ui/PageContainer/PageContainer';
import { useAuth } from '@/features/auth/model/useAuth';
import { getApiErrorMessage } from '@/shared/lib/errors';
import { useRouteBuilder } from './useRouteBuilder';
import { BuilderSettingsPanel } from './components/BuilderSettingsPanel';
import { RouteStepsPanel } from './components/RouteStepsPanel';
import { RecommendationSection } from './components/RecommendationSection';
import { RouteWarnings } from './components/RouteWarnings';
import { RouteSummary } from './components/RouteSummary';
import { BuilderMapPanel } from './components/BuilderMapPanel';
import styles from './RouteBuilderWorkspacePage.module.css';

export function RouteBuilderWorkspacePage() {
  const { user } = useAuth();
  const builder = useRouteBuilder({ userId: user?.id });

  const allRecommendations = useMemo(
    () => builder.recommendationGroups.flatMap((group) => group.items),
    [builder.recommendationGroups],
  );

  return (
    <PageContainer className={styles.page}>
      <section className={styles.hero}>
        <div>
          <p className={styles.kicker}>Route Builder Workspace</p>
          <h1>Конструктор маршрутов</h1>
          <p>
            Соберите персональный маршрут вручную или с подсказками системы. Порядок, тайминг, переезды и
            конфликты проверяются в реальном времени.
          </p>
        </div>
        <div className={styles.userMeta}>
          <span>Пользователь: {user?.name ?? 'Гость'}</span>
          <span>Тип: {builder.userContext?.userType ?? 'Стандарт'}</span>
          <span>Интересов: {builder.userContext?.interestTags.length ?? 0}</span>
        </div>
      </section>

      <BuilderSettingsPanel
        value={builder.settings}
        onChange={(updater) => builder.setSettings((prev) => updater(prev))}
        onBuild={() => void builder.candidatesQuery.refetch()}
        isBuilding={builder.candidatesQuery.isFetching}
      />

      <section className={styles.workspace}>
        <div className={styles.leftColumn}>
          <RouteStepsPanel
            steps={builder.timeline.steps}
            summary={builder.timeline.summary}
            selectedStepId={builder.selectedStepId}
            replacingStepSourceId={builder.replacingStepSourceId}
            onSelect={builder.setSelectedStepId}
            onRemove={builder.removeStep}
            onMove={builder.moveStep}
            onReplace={(sourceId) =>
              builder.setReplacingStepSourceId((prev) => (prev === sourceId ? null : sourceId))
            }
            onTogglePin={builder.togglePin}
            onClearAll={builder.clearAll}
          />

          <RouteWarnings items={builder.warnings} />

          <RouteSummary
            summary={builder.timeline.summary}
            onMakeShorter={() => {
              const last = builder.timeline.steps[builder.timeline.steps.length - 1];
              if (last) {
                builder.removeStep(last.sourceId);
              }
            }}
            onAddFood={() =>
              builder.setSettings((prev) => ({
                ...prev,
                pacePreset: 'gastro',
                manualSearch: 'гастро',
              }))
            }
            onAddEvent={() =>
              builder.setSettings((prev) => ({
                ...prev,
                includeEvents: true,
                manualSearch: prev.manualSearch,
              }))
            }
          />
        </div>

        <div className={styles.rightColumn}>
          <BuilderMapPanel
            center={builder.timeline.steps.length ? builder.timeline.steps[builder.timeline.steps.length - 1] : builder.anchorPoint}
            radiusKm={builder.settings.radiusKm}
            steps={builder.timeline.steps}
            recommendations={allRecommendations}
            selectedStepId={builder.selectedStepId}
            onPickStep={builder.setSelectedStepId}
            onPickRecommendation={(sourceId) => {
              const recommendation = allRecommendations.find((item) => item.sourceId === sourceId);
              if (recommendation) {
                builder.addStepFromRecommendation(recommendation);
              }
            }}
            onMapClick={(lat, lng) => builder.setAnchorPoint({ lat, lng })}
          />

          <RecommendationSection
            groups={builder.recommendationGroups}
            isLoading={builder.candidatesQuery.isLoading}
            isError={builder.candidatesQuery.isError}
            errorMessage={getApiErrorMessage(builder.candidatesQuery.error, 'Не удалось загрузить рекомендации.')}
            replacingStepSourceId={builder.replacingStepSourceId}
            onRetry={() => void builder.candidatesQuery.refetch()}
            onAdd={builder.addStepFromRecommendation}
            onSelect={(item) => {
              if (!builder.selectedStep || builder.selectedStep.sourceId !== item.sourceId) {
                builder.setSelectedStepId(item.sourceId);
              }
            }}
          />
        </div>
      </section>
    </PageContainer>
  );
}


