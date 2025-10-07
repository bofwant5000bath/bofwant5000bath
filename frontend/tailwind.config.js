// tailwind.config.js
import forms from '@tailwindcss/forms';
import containerQueries from '@tailwindcss/container-queries';

export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sarabun: ['Sarabun', 'sans-serif'],
      },
    },
  },
  plugins: [
    forms,
    containerQueries,
  ],
};