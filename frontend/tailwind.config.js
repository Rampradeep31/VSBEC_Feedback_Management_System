/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#eef2f9', 100: '#d8e2f0', 500: '#1e3a6d', 600: '#16305e',
          700: '#102448', 800: '#0b1a34', 900: '#071122'
        },
        teal: {
          50: '#eafaf8', 100: '#c9f1eb', 500: '#0f9d8f', 600: '#0c7f74', 700: '#096560'
        }
      }
    },
  },
  plugins: [],
}
