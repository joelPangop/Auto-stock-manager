// Jest pour Angular 10 / TypeScript 3.9.
// Les versions sont contraintes par cette stack : jest 26 + jest-preset-angular 8.
// Une montee en Jest 29/30 impose d'abord une montee d'Angular et de TypeScript.
module.exports = {
  preset: 'jest-preset-angular',
  roots: ['<rootDir>/src'],
  setupFilesAfterEnv: ['<rootDir>/src/setup-jest.ts'],
  // src/test.ts est l'amorce de Karma : son nom correspond au motif par defaut
  // de Jest, qui le prend alors pour une suite vide.
  testPathIgnorePatterns: [
    '<rootDir>/node_modules/',
    '<rootDir>/e2e/',
    '<rootDir>/dist/',
    '<rootDir>/src/test.ts',
  ],
  moduleFileExtensions: ['ts', 'html', 'js', 'json'],
  collectCoverageFrom: [
    'src/app/**/*.ts',
    '!src/app/**/*.module.ts',
    '!src/app/**/*.spec.ts',
  ],
  coverageDirectory: '<rootDir>/coverage',
  globals: {
    'ts-jest': {
      tsconfig: '<rootDir>/tsconfig.spec.json',
      stringifyContentPathRegex: '\.html$',
      // Angular 10 compile encore en ES2015 avec des decorateurs : ts-jest doit
      // utiliser le meme tsconfig que le build, sinon les metadonnees
      // d'injection disparaissent et TestBed ne resout plus les dependances.
      diagnostics: {
        // TS2531 / TS2532 : le code applicatif n'est pas en mode strict, on
        // n'impose pas aux tests une rigueur que le code de prod n'a pas.
        ignoreCodes: [2531, 2532, 151001],
      },
    },
  },
};
