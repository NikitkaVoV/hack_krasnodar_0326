declare global {
  interface Window {
    ymaps?: {
      ready: (callback: () => void) => void;
      Map: new (element: HTMLElement, state: unknown, options?: unknown) => any;
      Placemark: new (geometry: number[], properties?: unknown, options?: unknown) => any;
      Circle: new (geometry: [number[], number], properties?: unknown, options?: unknown) => any;
      Clusterer: new (options?: unknown) => any;
      control: {
        ZoomControl: new (options?: unknown) => any;
        GeolocationControl: new (options?: unknown) => any;
      };
    };
  }
}

export {};
