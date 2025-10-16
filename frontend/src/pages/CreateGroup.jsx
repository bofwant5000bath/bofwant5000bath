import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/api.js';

const CreateGroup = () => {
  const [groupName, setGroupName] = useState('');
  const [allUsers, setAllUsers] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();
  const currentUserId = localStorage.getItem('user_id');

  // ✅ 1. เพิ่ม State สำหรับเก็บค่าในช่องค้นหา
  const [searchTerm, setSearchTerm] = useState('');

  const isSubmitting = useRef(false);

  useEffect(() => {
    const fetchAllUsers = async () => {
      try {
        const response = await apiClient.get('/groups/users');
        const users = response.data;
        setAllUsers(users);
        const currentUser = users.find(user => user.userId === parseInt(currentUserId));
        if (currentUser) {
          setSelectedUsers([currentUser.userId]);
        }
      } catch (err) {
        setError('ไม่สามารถดึงรายชื่อผู้ใช้ได้ โปรดลองอีกครั้ง');
        console.error('Error fetching users:', err);
      }
    };
    fetchAllUsers();
  }, [currentUserId]);

  const handleUserToggle = (userId) => {
    setSelectedUsers(prevSelected => {
      if (prevSelected.includes(userId)) {
        return prevSelected.filter(id => id !== userId);
      } else {
        return [...prevSelected, userId];
      }
    });
  };

  const handleRemoveSelected = (userId) => {
    setSelectedUsers(prev => prev.filter(id => id !== userId));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isSubmitting.current) return;
    isSubmitting.current = true;

    setLoading(true);
    setError('');
    setSuccess('');

    // ✅ 2. เพิ่มการตรวจสอบว่าใส่ชื่อกลุ่มแล้วหรือยัง
    if (!groupName.trim()) {
      setError('กรุณาตั้งชื่อกลุ่ม');
      setLoading(false);
      isSubmitting.current = false;
      return;
    }

    if (selectedUsers.length < 2) {
      setError('กลุ่มต้องมีสมาชิกอย่างน้อย 2 คน');
      setLoading(false);
      isSubmitting.current = false;
      return;
    }

    try {
      const payload = {
        groupName: groupName,
        createdByUserId: parseInt(currentUserId),
        memberIds: selectedUsers,
      };

      await apiClient.post('/groups/create', payload);
      setSuccess('สร้างกลุ่มสำเร็จ!');
      
      setTimeout(() => {
        isSubmitting.current = false;
        navigate('/dashboard');
      }, 1200);
    } catch (err) {
      setError('เกิดข้อผิดพลาดในการสร้างกลุ่ม โปรดลองอีกครั้ง');
      console.error('Error creating group:', err);
      isSubmitting.current = false;
    } finally {
      setLoading(false);
    }
  };

  // ✅ 3. สร้าง list ของ user ที่ผ่านการกรองจากช่องค้นหา
  const filteredUsers = allUsers.filter(user =>
    user.fullName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen flex flex-col items-center bg-gray-50 font-sarabun">
      {/* ... (ส่วน Header เหมือนเดิม) ... */}
      <div className="w-full max-w-2xl flex items-center p-4">
        <Link to="/dashboard" className="mr-4 text-gray-600 hover:underline">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
        </Link>
        <h2 className="text-xl font-bold">สร้างกลุ่มใหม่</h2>
      </div>


      <form onSubmit={handleSubmit} className="w-full max-w-2xl bg-white shadow-md rounded-lg p-6 flex flex-col flex-1">
        
        {/* แสดงข้อความ Error */}
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg relative mb-4" role="alert">
            <span className="block sm:inline">{error}</span>
          </div>
        )}

        {/* Group Name */}
        <div className="mb-4">
          <input
            type="text"
            placeholder="เช่น ทริปเที่ยวทะเล, ค่าห้องเดือนนี้"
            value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            className="w-full border rounded-md p-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
            required // เพิ่ม required เพื่อการ validation พื้นฐานของ browser
          />
        </div>

        {/* Search Bar */}
        <div className="mb-4">
          {/* ✅ 4. เชื่อม State เข้ากับช่องค้นหา */}
          <input
            type="text"
            placeholder="ค้นหาชื่อเพื่อน"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full border rounded-md p-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* ... (ส่วน Selected Friends เหมือนเดิม) ... */}
        {selectedUsers.length > 0 && (
          <div className="mb-6">
            <h3 className="font-semibold mb-2">เพื่อนที่เลือก</h3>
            <div className="flex gap-4 flex-wrap">
              {allUsers
                .filter(user => selectedUsers.includes(user.userId))
                .map(user => (
                  <div key={user.userId} className="relative text-center">
                    <img
                      src={user.profilePictureUrl || 'https://via.placeholder.com/150'}
                      alt={user.fullName}
                      className="w-16 h-16 rounded-full object-cover mx-auto"
                    />
                    <span className="text-sm block mt-1">{user.fullName}</span>
                    <button
                      type="button"
                      onClick={() => handleRemoveSelected(user.userId)}
                      className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs"
                    >
                      ✕
                    </button>
                  </div>
                ))}
            </div>
          </div>
        )}

        {/* All Friends List */}
        <div className="flex-1 overflow-y-auto mb-6">
          <h3 className="font-semibold mb-2">เพื่อนทั้งหมด</h3>
          <div className="divide-y border rounded-md">
            {/* ✅ 5. ใช้ list ที่ผ่านการกรองแล้วมาแสดงผล */}
            {filteredUsers.map(user => (
              <label
                key={user.userId}
                className="flex items-center justify-between p-3 cursor-pointer hover:bg-gray-100"
              >
                <div className="flex items-center">
                  <img
                    src={user.profilePictureUrl || 'https://via.placeholder.com/150'}
                    alt={user.fullName}
                    className="w-10 h-10 rounded-full object-cover mr-3"
                  />
                  <span>{user.fullName}</span>
                </div>
                <input
                  type="checkbox"
                  checked={selectedUsers.includes(user.userId)}
                  onChange={() => handleUserToggle(user.userId)}
                  className="w-5 h-5"
                />
              </label>
            ))}
          </div>
        </div>

        {/* Submit Button */}
        <div className="mt-auto">
          <button
            type="submit"
            className={`w-full py-3 rounded-md text-white font-bold ${
              loading || !groupName.trim() || selectedUsers.length < 2
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-500 hover:bg-blue-600'
            }`}
            disabled={loading || !groupName.trim() || selectedUsers.length < 2}
          >
            {loading ? 'กำลังสร้าง...' : 'เสร็จสิ้น'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateGroup;