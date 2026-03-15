import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#5B8BD4',
          light: '#E8F0FA',
        },
        secondary: {
          DEFAULT: '#6DAE72',
          light: '#E8F5EA',
        },
        success: '#6DAE72',
        error: '#EC0000',
        warning: '#F2A800',
        text: {
          primary: '#1A1A1C',
          secondary: '#6B6B6B',
        },
        border: {
          DEFAULT: '#D4D4D4',
          active: '#5B8BD4',
        },
        background: {
          DEFAULT: '#FFFFFF',
          secondary: '#F7F7F7',
        }
      },
      fontFamily: {
        sans: ['Noto Sans JP', 'sans-serif'],
      },
      spacing: {
        '0.5': '2px',
        '1': '4px',
        '2': '8px',
        '3': '12px',
        '4': '16px',
        '5': '20px',
        '6': '24px',
        '8': '32px',
        '10': '40px',
        '12': '48px',
      },
      borderRadius: {
        'sm': '4px',
        'DEFAULT': '8px',
        'lg': '12px',
      }
    },
  },
  plugins: [],
} satisfies Config;
