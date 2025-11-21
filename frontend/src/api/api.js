// frontend/src/api/api.js
import axios from 'axios';

const apiClient = axios.create({
  // ถ้ามี Env VITE_API_URL ให้ใช้ (บน Cloud) ถ้าไม่มีให้ใช้ localhost (เครื่องเรา)
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api' 
});

export default apiClient;
