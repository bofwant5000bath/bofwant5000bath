import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// ✅ 1. Import ทั้ง axios ปกติ และ apiClient ของเรามา
import axios from 'axios'
import apiClient from './api/api.js' 

// ✅ 2. สั่งเปิด withCredentials ทั้ง 2 ตัวเลย (กันเหนียว)
// อันนี้สำหรับเวลาใช้ axios.get() ตรงๆ
axios.defaults.withCredentials = true; 

// อันนี้สำหรับหน้าที่ใช้ apiClient.get() (เช่น Dashboard ของคุณ)
if (apiClient.defaults) {
    apiClient.defaults.withCredentials = true;
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
