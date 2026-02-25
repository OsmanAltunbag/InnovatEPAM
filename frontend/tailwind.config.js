/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#f1f5ff",
          100: "#dbe5ff",
          200: "#b3c8ff",
          300: "#8cabff",
          400: "#658eff",
          500: "#3e71ff",
          600: "#2759e6",
          700: "#1e45b3",
          800: "#153180",
          900: "#0c1d4d"
        }
      }
    }
  },
  plugins: []
};
