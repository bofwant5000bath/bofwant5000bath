const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    // เพิ่มบรรทัดนี้เข้าไป
    baseUrl: 'http://localhost:5173', // <--- แก้เป็น URL ของคุณ
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});