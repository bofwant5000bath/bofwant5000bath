// frontend/src/api/api.js

import axios from 'axios';

// 1. สร้าง "ตู้สาขา" กลางขึ้นมา
const apiClient = axios.create({
  // 2. ตั้งค่าเบอร์หลักของบริษัทไว้ที่นี่ที่เดียว
  baseURL: '/api'
});

// 3. ส่งออก "ตู้สาขา" นี้เพื่อให้ไฟล์อื่นนำไปใช้
export default apiClient;