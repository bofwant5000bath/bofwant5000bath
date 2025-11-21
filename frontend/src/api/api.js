// frontend/src/api/api.js
import axios from 'axios';

// ตรวจสอบว่ารันอยู่บน Environment ไหน
// VITE_API_URL คือตัวแปรที่เราจะไปตั้งใน Railway/Zeabur
const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: baseURL
});

export default apiClient;
