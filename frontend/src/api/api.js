// frontend/src/api/api.js

import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api',           // หรือ https://bofwant5000bath.zeabur.app/api
  withCredentials: true,     // ✅ ส่ง Cookie ไปกับทุก request
  headers: {
    "Content-Type": "application/json"
  }
});

export default apiClient;
