export const appRoutes = {
  home: '/',
  login: '/login',
  profile: '/profile',
  routes: '/routes',
  routeBuilder: '/routes/builder',
  routeBuilderCreate: '/routes/builder/create',
  mapDiscovery: '/discover',
  placeDetails: (id: string) => `/places/${id}`,
  routeDetails: (id: string) => `/routes/${id}`,
} as const;

