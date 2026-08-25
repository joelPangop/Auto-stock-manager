// Initialise l'environnement de test Angular (zone.js, TestBed) pour Jest.
// Remplace src/test.ts, qui etait le point d'entree de Karma.
import 'jest-preset-angular/setup-jest';

// Angular Material s'appuie sur des API que jsdom n'implemente pas.
// Sans ces bouchons, tout composant utilisant un mat-select, un dialog ou une
// media query echoue au rendu avec une erreur peu parlante.
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

Object.defineProperty(window, 'CSS', {value: null});

Object.defineProperty(document.body.style, 'transform', {
  value: () => ({enumerable: true, configurable: true}),
});
