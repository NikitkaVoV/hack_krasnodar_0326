export const queryKeys = {
  authMe: ['auth', 'me'] as const,
  userById: (id: string) => ['users', 'by-id', id] as const,
  userPreferences: (id: string) => ['users', 'preferences', id] as const,
  userTags: (id: string) => ['users', 'tags', id] as const,
  userConstraints: (id: string) => ['users', 'constraints', id] as const,
  routesByUserDate: (userId: string, date: string) =>
    ['routes', 'list', userId, date] as const,
  routeById: (routeId: string) => ['routes', 'by-id', routeId] as const,
  routeSteps: (routeId: string) => ['routes', 'steps', routeId] as const,
  placeById: (id: string) => ['places', id] as const,
  eventById: (id: string) => ['events', id] as const,
  home: (date: string, limit: number, isAuthenticated: boolean) =>
    ['home', date, limit, isAuthenticated ? 'auth' : 'guest'] as const,
} as const;

