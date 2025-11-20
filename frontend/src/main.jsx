import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

// ✅ 1. Import มาทั้ง 2 ตัวเลย (axios ปกติ และ apiClient ที่คุณใช้ใน Dashboard)
import axios from 'axios'
import apiClient from './api/api.js' 

// ✅ 2. สั่งเปิด withCredentials ให้ axios ตัวปกติ (เผื่อหน้าไหนใช้)
axios.defaults.withCredentials = true; 

// ✅ 3. สั่งเปิด withCredentials ให้ apiClient (ตัวนี้แหละที่ Dashboard.jsx ใช้อยู่)
// ต้องเช็คก่อนว่ามีค่าไหม เพื่อความปลอดภัย
if (apiClient && apiClient.defaults) {
    apiClient.defaults.withCredentials = true;
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
