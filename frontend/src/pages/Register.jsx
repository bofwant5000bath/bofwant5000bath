import { useState, useRef } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom'; // เพิ่ม useNavigate

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
  const navigate = useNavigate(); // ใช้ navigate

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

    const formData = new FormData();
    formData.append('full_name', name);
    formData.append('username', username);
    formData.append('password', password);
    if (picture) {
      formData.append('picture', picture);
    }

    try {
      const response = await axios.post(
        'http://localhost:8080/api/auth/register',
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
      setSuccess(response.data.message);
      setError('');
      console.log('การลงทะเบียนสำเร็จ:', response.data);

      // ✅ เปลี่ยนหน้าไป Login หลังจากสมัครสำเร็จ
      navigate('/login');

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

        <form onSubmit={handleSubmit}>
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
              />
            </div>
          </div>

          <div className="mb-4">
            <label
              className="block text-gray-700 text-sm font-bold mb-2"
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
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
          </div>

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
