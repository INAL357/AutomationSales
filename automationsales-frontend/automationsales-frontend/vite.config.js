// import { defineConfig } from 'vite';
// import react from '@vitejs/plugin-react';

// // https://vite.dev/config/
// export default defineConfig({
//   plugins: [react()],
//   server: {
//     port: 5173,
//     // During development, any request to /api is forwarded to Spring Boot.
//     // So in the code we just call "/api/leads" and never write "http://localhost:8080".
//     proxy: {
//       '/api': 'http://localhost:8080',
//     },
//   },
// });

{
  "rewrites": [
    { "source": "/api/:path*", "destination": "https://automationsales-backend.onrender.com/api/:path*" },
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
