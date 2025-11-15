import { useState, useRef } from 'react';
import apiClient from '../api/api.js';
import { Link, useNavigate } from 'react-router-dom';

// ✅ [แก้ไข] เปลี่ยน URL รูปเริ่มต้นให้ใหม่
const DEFAULT_PROFILE_PICTURE = 'https://cdn-icons-png.flaticon.com/512/149/149071.png';

const Register = () => {
  const [name, setName] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [picture, setPicture] = useState(null);
  const [picturePreview, setPicturePreview] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const fileInputRef = useRef(null);
  const navigate = useNavigate();

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setPicture(file);
      setPicturePreview(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      setError('รหัสผ่านไม่ตรงกัน');
      return;
    }

    try {
      let pictureUrl = ''; // <-- ค่าเริ่มต้นยังเป็น string ว่าง

      // ✅ อัปโหลดรูปไป imgbb
      if (picture) {
        const data = new FormData();
        data.append('image', picture);

        const res = await fetch(
          'https://api.imgbb.com/1/upload?key=c8828a8f5e3ca1309d22a2672d99bfe2',
          { method: 'POST', body: data }
        );

        const result = await res.json();
        if (result.success && result.data?.url) {
          pictureUrl = result.data.url;
          console.log('✅ URL รูปที่อัปโหลด:', pictureUrl);
        } else {
          console.warn('⚠️ ไม่ได้ URL จาก imgbb:', result);
        }
      }

      // ✅ ส่งข้อมูลเป็น JSON ให้ backend
      const payload = {
        username: username,
        password: password,
        fullName: name,
        
        // ✅ [แก้ไข]
        // ถ้า pictureUrl (ที่อัปโหลด) เป็นค่าว่าง (falsy), ให้ใช้ DEFAULT_PROFILE_PICTURE แทน
        profilePictureUrl: pictureUrl || DEFAULT_PROFILE_PICTURE,
      };

      const response = await apiClient.post(
        '/auth/register',
        payload,
        {
          headers: { 'Content-Type': 'application/json' },
        }
      );

      setSuccess(response.data.message);
      setError('');
      console.log('🎉 การลงทะเบียนสำเร็จ:', response.data);
      
      // นำทางไปหน้า login หลังจากลงทะเบียนสำเร็จ
      setTimeout(() => {
        navigate('/login');
      }, 1000); // หน่วงเวลา 1 วินาที (ไม่บังคับ)

    } catch (err) {
      setError(err.response?.data?.message || 'เกิดข้อผิดพลาดในการลงทะเบียน');
      setSuccess('');
      console.error('เกิดข้อผิดพลาด:', err);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100 font-sarabun p-4">
      <div className="bg-white p-6 rounded-2xl shadow-lg w-full max-w-sm mx-auto">
        <h1 className="text-3xl font-bold text-center text-gray-800 mb-6">
          สร้างบัญชีใหม่
        </h1>
        
        {/* แสดงข้อความ Success หรือ Error */}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg relative mb-4" role="alert">
            <span className="block sm:inline">{error}</span>
          </div>
        )}
        {success && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded-lg relative mb-4" role="alert">
            <span className="block sm:inline">{success} (กำลังไปหน้า Login...)</span>
          </div>
        )}


        <form onSubmit={handleSubmit}>
          {/* ส่วนเลือกรูปโปรไฟล์ */}
          <div className="flex flex-col items-center mb-6">
            <div
              className="w-24 h-24 rounded-full bg-gray-200 flex items-center justify-center mb-3 overflow-hidden cursor-pointer"
              onClick={() => fileInputRef.current.click()}
            >
              {picturePreview ? (
                <img
                  src={picturePreview}
                  alt="Profile Preview"
                  className="w-full h-full object-cover"
                />
              ) : (
                <i className="material-icons text-gray-400 text-5xl">person</i>
              )}
            </div>
            <label
              className="bg-gray-200 hover:bg-gray-300 text-gray-700 font-bold py-2 px-4 rounded-lg cursor-pointer text-sm"
              htmlFor="profile_picture"
            >
              <span>เพิ่มรูปโปรไฟล์</span>
            </label>
            <input
              accept="image/*"
              className="hidden"
              id="profile_picture"
              name="profile_picture"
              onChange={handleFileChange}
              ref={fileInputRef}
              type="file"
            />
          </div>

          {/* ชื่อ-นามสกุล */}
          <div className="mb-4">
            <label
              className="block text-gray-700 text-sm font-bold mb-2"
              htmlFor="name"
            >
              ชื่อ-นามสกุล
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3">
                <i className="material-icons text-gray-400">badge</i>
              </span>
              <input
                className="shadow-sm appearance-none border rounded-lg w-full py-3 px-10 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                id="name"
                name="name"
                placeholder="กรอกชื่อและนามสกุลของคุณ"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
          </div>

          {/* ชื่อผู้ใช้ */}
          <div className="mb-4">
            <label
              className="block text-gray-70g-700 text-sm font-bold mb-2"
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
                placeholder="กรอกชื่อผู้ใช้ของคุณ"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)} // ✅ [แก้ไข] แก้บัคจาก e.g.value
                required
              />
            </div>
          </div>

          {/* รหัสผ่าน */}
          <div className="mb-4">
            <label
              className="block text-gray-700 text-sm font-bold mb-2"
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

          {/* ยืนยันรหัสผ่าน */}
          <div className="mb-6">
            <label
              className="block text-gray-700 text-sm font-bold mb-2"
              htmlFor="confirm-password"
            >
              ยืนยันรหัสผ่าน
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 flex items-center pl-3">
                <i className="material-icons text-gray-400">lock_outline</i>
              </span>
              <input
                className="shadow-sm appearance-none border rounded-lg w-full py-3 px-10 text-gray-700 leading-tight focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                id="confirm-password"
                name="confirm-password"
                placeholder="ยืนยันรหัสผ่านของคุณ"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
          </div>

          <button
            className="w-full bg-blue-500 hover:bg-blue-600 text-white font-bold py-3 px-4 rounded-lg focus:outline-none transition duration-300 ease-in-out transform hover:-translate-y-1"
            type="submit"
          >
            ลงทะเบียน
          </button>
        </form>

        <p className="text-center text-gray-500 text-sm mt-6">
          มีบัญชีอยู่แล้ว?{' '}
          <Link
            className="text-blue-500 hover:text-blue-700 font-semibold"
            to="/login"
          >
            เข้าสู่ระบบ
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;