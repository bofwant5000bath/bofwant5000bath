import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/api.js';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate(); // Hook สำหรับการเปลี่ยนหน้า

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // เรียกใช้งาน API สำหรับการเข้าสู่ระบบ
      const response = await apiClient.post('/auth/login', {
        username,
        password,
      });

      // จัดการเมื่อเข้าสู่ระบบสำเร็จ
      console.log('เข้าสู่ระบบสำเร็จ:', response.data);

      // จัดเก็บข้อมูลผู้ใช้ที่จำเป็นลงใน localStorage
      localStorage.setItem('user_id', response.data.user_id);
      localStorage.setItem('full_name', response.data.full_name);
      // หาก backend ส่ง profile_picture_url มาด้วย ให้บันทึกไว้
      if (response.data.profile_picture_url) {
        localStorage.setItem('profile_picture_url', response.data.profile_picture_url);
      }

      setError(''); // เคลียร์ข้อความผิดพลาด

      // เปลี่ยนเส้นทางผู้ใช้ไปยังหน้า Dashboard
      navigate('/dashboard'); 
      
    } catch (err) {
      // จัดการเมื่อเกิดข้อผิดพลาด
      // แสดงข้อความผิดพลาดที่กำหนดไว้ใน Backend หรือข้อความเริ่มต้น
      setError(err.response?.data?.message || 'ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง'); 
      console.error('Error logging in:', err);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 font-sarabun p-4">
      <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md">
        <h2 className="text-3xl font-bold text-center text-gray-800 mb-6">เข้าสู่ระบบ</h2>
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg relative mb-4" role="alert">
            <span className="block sm:inline">{error}</span>
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label
              className="block text-gray-700 text-sm font-semibold mb-2"
              htmlFor="username"
            >
              ชื่อผู้ใช้
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3">
                <i className="material-icons text-gray-400">person</i>
              </span>
              <input
                className="shadow-sm appearance-none border rounded-lg w-full py-3 px-10 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                id="username"
                name="username"
                type="text"
                placeholder="กรอกชื่อผู้ใช้ของคุณ"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>
          
          <div>
            <label
              className="block text-gray-700 text-sm font-semibold mb-2"
              htmlFor="password"
            >
              รหัสผ่าน
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3">
                <i className="material-icons text-gray-400">lock</i>
              </span>
              <input
                className="shadow-sm appearance-none border rounded-lg w-full py-3 px-10 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                id="password"
                name="password"
                placeholder="กรอกรหัสผ่านของคุณ"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
          </div>
          
          <button
            className="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-4 rounded-lg focus:outline-none transition duration-300 ease-in-out transform hover:-translate-y-1"
            type="submit"
          >
            เข้าสู่ระบบ
          </button>
        </form>

        <p className="text-center text-gray-500 text-sm mt-6">
          ยังไม่มีบัญชี?{' '}
          <Link
            className="text-blue-500 hover:text-blue-700 font-semibold"
            to="/register"
          >
            ลงทะเบียนที่นี่
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
