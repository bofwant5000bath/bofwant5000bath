// frontend/src/api/api.js

import axios from "axios";

const apiClient = axios.create({
  baseURL: "https://backend-bofwant5000bath.zeabur.app/api", // backend domain
  withCredentials: true, // ✅ สำคัญมาก
  headers: {
    "Content-Type": "application/json"
  }
});

export default apiClient;
