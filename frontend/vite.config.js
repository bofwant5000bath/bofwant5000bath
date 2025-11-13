// For Kubernetes
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import istanbul from 'vite-plugin-istanbul'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // Plugin สำหรับทำ Code Coverage ร่วมกับ Cypress
    istanbul({
      cypress: true,
      requireEnv: false, // บังคับให้ plugin ทำงานเสมอสำหรับ dev server
    }),
  ],
  build: {
    // เปลี่ยน output directory จาก 'dist' เป็น 'build' เพื่อให้เข้ากับ Dockerfile
    outDir: 'dist', 
    // (ทางเลือก) สร้าง source map เพื่อให้ coverage report อ่านง่ายขึ้น
    sourcemap: true,
  },
  server: {
    // (สำคัญสำหรับ Docker) ตั้งค่าให้ Vite รับการเชื่อมต่อจากทุก network interface
    // ไม่ใช่แค่จาก localhost
    host: true, 
    port: 5173, // Port ที่ Vite จะรันใน container

    // จำเป็นเพื่อให้ Hot Module Replacement (HMR) ทำงานได้ถูกต้อง
    // เมื่อรันอยู่หลัง reverse proxy เช่น Nginx ใน production
    hmr: {
      clientPort: 80, // Port ที่ Nginx เปิดให้เข้าถึงจากภายนอก
    },
    
    // (สำคัญสำหรับ Docker) บังคับให้ Vite ใช้วิธี polling ในการตรวจจับการเปลี่ยนแปลงของไฟล์
    // เนื่องจาก event การเปลี่ยนแปลงไฟล์แบบปกติอาจทำงานไม่ถูกต้องในบางสภาพแวดล้อมของ Docker
    watch: {
      usePolling: true
    }
  }
})


// For frontend : npm run dev
// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'
// import istanbul from 'vite-plugin-istanbul'

// // https://vitejs.dev/config/
// export default defineConfig({
//   plugins: [
//     react(),
//     istanbul({
//       cypress: true,
//       requireEnv: false,
//     }),
//   ],
//   build: {
//     outDir: 'dist', 
//     sourcemap: true,
//   },
//   server: {
//     // host: true, // ยังคงเปิดไว้ได้ ถ้าคุณต้องการเข้าจากมือถือในวง LAN เดียวกัน
//     port: 5173, 

//     // ❗️ 1. ลบ HMR BLOCK ออก
//     // hmr: {
//     //   clientPort: 80, 
//     // },
    
//     // ❗️ 2. เพิ่ม PROXY BLOCK นี้เข้าไป
//     // เพื่อบอก Vite ว่าถ้ามีการเรียก /api/ ให้ส่งต่อไปที่ backend ที่รันบน port 8080
//     proxy: {
//       '/api': {
//         target: 'http://localhost:8080', // ที่อยู่ของ Backend Server ของคุณ
//         changeOrigin: true, // จำเป็นสำหรับการทำ proxy
//       }
//     },

//     // ❗️ 3. (แนะนำ) ลบ WATCH BLOCK ออก
//     // 'usePolling: true' จำเป็นสำหรับ Docker แต่จะทำงานช้ากว่าบน Local ปกติ
//     // watch: {
//     //   usePolling: true
//     // }
//   }
// })