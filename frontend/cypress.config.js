import { defineConfig } from "cypress";
import codeCoverageTask from '@cypress/code-coverage/task';

export default defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
      config.baseUrl = 'http://localhost:5173/'; // <--- แก้เป็น URL ของคุณ
      codeCoverageTask(on, config);
      return config;
    },
    // Explicitly define the spec pattern
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
    // Video and screenshot settings for CI
    video: false,
    screenshotOnRunFailure: true,
    // CI-friendly settings
    defaultCommandTimeout: 10000,
    pageLoadTimeout: 60000,
    requestTimeout: 10000,
    responseTimeout: 30000,
    // Retry failed tests in CI
    retries: {
      runMode: 2,
      openMode: 0
    },
    // Don't wait for animations
    waitForAnimations: false,
    animationDistanceThreshold: 2,
  },
});
