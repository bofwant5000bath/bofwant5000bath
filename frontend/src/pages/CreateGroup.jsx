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
  const currentUserId = localStorage.getItem('user_id'); // นี่คือ String

  // 1. State สำหรับเก็บค่าในช่องค้นหา "เพื่อน"
  const [friendSearchTerm, setFriendSearchTerm] = useState('');

  // 2. State สำหรับ Tab, รายชื่อกลุ่ม, และการค้นหากลุ่ม
  const [activeTab, setActiveTab] = useState('friends'); // 'friends' หรือ 'groups'
  const [allGroups, setAllGroups] = useState([]);
  const [groupSearchTerm, setGroupSearchTerm] = useState('');

  // 3. State สำหรับเก็บ "แผนที่" สมาชิกกลุ่ม (โครงสร้าง: { groupId: [userId1, userId2], ... })
  const [groupMemberMap, setGroupMemberMap] = useState({});

  const isSubmitting = useRef(false);

  // Effect: ดึงรายชื่อ User ทั้งหมด (สำหรับ Tab "เพื่อน")
  useEffect(() => {
    const fetchAllUsers = async () => {
      try {
        const response = await apiClient.get('/groups/users');
        const users = response.data;
        setAllUsers(users);
        
        const currentUserIdNum = parseInt(currentUserId);
        const currentUser = users.find(user => user.userId === currentUserIdNum);
        
        if (currentUser) {
          setSelectedUsers([currentUser.userId]);
        } else if (currentUserIdNum) {
          // ถ้าไม่พบใน list แต่มี ID ใน localStorage ก็ยังเพิ่มเข้าไป
          setSelectedUsers([currentUserIdNum]);
        }
      } catch (err) {
        setError('ไม่สามารถดึงรายชื่อผู้ใช้ได้ โปรดลองอีกครั้ง');
        console.error('Error fetching users:', err);
      }
    };
    fetchAllUsers();
  }, [currentUserId]);

  // Effect: ดึง "กลุ่มและสมาชิก" (สำหรับ Tab "กลุ่ม")
  useEffect(() => {
    const fetchAllGroupsAndMembers = async () => {
      if (!currentUserId) return; // รอให้ currentUserId พร้อมใช้งาน

      try {
        // --- 1. เรียก API ใหม่ตัวเดียว ---
        // ✅ เพิ่ม '/groups' เข้าไปข้างหน้า
        const response = await apiClient.get(`/groups/all-with-members/${currentUserId}`); // ✅ บรรทัดที่ 82 (แก้ไข)
        const groupsData = response.data;          

        // --- 2. สร้าง List ของกลุ่ม (สำหรับแสดงใน Tab "กลุ่ม") ---
        const groupsList = groupsData.map(group => ({
          groupId: group.groupId,
          groupName: group.groupName,
        }));
        setAllGroups(groupsList);

        // --- 3. สร้าง "แผนที่" ของสมาชิก (สำหรับปุ่ม "เพิ่มสมาชิก") ---
        const memberMap = groupsData.reduce((acc, group) => {
          // group.members คือ List<MemberDto>
          acc[group.groupId] = group.members.map(member => member.userId);
          return acc;
        }, {});
        
        setGroupMemberMap(memberMap);

      } catch (err) {
        setError('ไม่สามารถดึงรายชื่อกลุ่มได้');
        console.error('Error fetching groups and members:', err);
      }
    };
    
    fetchAllGroupsAndMembers();
  }, [currentUserId]); // ทำงานเมื่อ currentUserId พร้อมใช้งาน


  const handleUserToggle = (userId) => {
    // ป้องกันไม่ให้ติ๊ก "ตัวเอง" ออก
    if (userId === parseInt(currentUserId)) return;
    
    setSelectedUsers(prevSelected => {
      if (prevSelected.includes(userId)) {
        return prevSelected.filter(id => id !== userId);
      } else {
        return [...prevSelected, userId];
      }
    });
  };

  const handleRemoveSelected = (userId) => {
    // ป้องกันไม่ให้ลบ "ตัวเอง" ออกจากกลุ่ม
    if (userId === parseInt(currentUserId)) return;
    
    setSelectedUsers(prev => prev.filter(id => id !== userId));
  };

  // Logic: เมื่อคลิก "เพิ่มสมาชิก" จากกลุ่ม
  const handleAddGroupMembers = (groupId) => {
    
    // 1. ดึง ID สมาชิกจาก State (groupMemberMap) ที่โหลดไว้แล้ว
    const memberIds = groupMemberMap[groupId] || [];

    // 2. เพิ่มสมาชิกทั้งหมดจากกลุ่มนั้น vào selectedUsers (โดยไม่ซ้ำ)
    setSelectedUsers(prevSelected => {
      const combinedIds = new Set([...prevSelected, ...memberIds]);
      return Array.from(combinedIds);
    });
  };


  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isSubmitting.current) return;
    isSubmitting.current = true;
    setLoading(true);
    setError('');
    setSuccess('');

    if (!groupName.trim()) {
      setError('กรุณาตั้งชื่อกลุ่ม');
      setLoading(false);
      isSubmitting.current = false;
      return;
    }

    if (selectedUsers.length < 2) {
      setError('กลุ่มต้องมีสมาชิกอย่างน้อย 2 คน (รวมตัวคุณ)');
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

  // สร้าง list ของ user ที่ผ่านการกรอง (สำหรับ Tab "เพื่อน")
  const filteredUsers = allUsers.filter(user =>
    user.fullName.toLowerCase().includes(friendSearchTerm.toLowerCase())
  );

  // สร้าง list ของ group ที่ผ่านการกรอง (สำหรับ Tab "กลุ่ม")
  const filteredGroups = allGroups.filter(group =>
    group.groupName.toLowerCase().includes(groupSearchTerm.toLowerCase())
  );

  // ดึงข้อมูล User (จาก allUsers) เพื่อมาแสดงใน "เพื่อนที่เลือก"
  const selectedUserDetails = allUsers.filter(user => 
    selectedUsers.includes(user.userId)
  );

  // จัดการกรณีที่คนสร้างกลุ่ม (currentUserId) ไม่ได้อยู่ใน allUsers ที่ fetch มา
  const currentUserIdNum = parseInt(currentUserId);
  if (currentUserIdNum && !selectedUserDetails.find(u => u.userId === currentUserIdNum) && selectedUsers.includes(currentUserIdNum)) {
    // ถ้ายังไม่มี ให้เพิ่ม object สำหรับคนสร้างเข้าไป
    selectedUserDetails.unshift({ 
      userId: currentUserIdNum, 
      fullName: 'คุณ (ผู้สร้าง)', 
      profilePictureUrl: 'https://via.placeholder.com/150' // หรือรูป default
    });
  }


  return (
    <div className="min-h-screen flex flex-col items-center bg-gray-50 font-sarabun">
      {/* Header */}
      <div className="w-full max-w-2xl flex items-center p-4">
        <Link to="/dashboard" className="mr-4 text-gray-600 hover:underline">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
        </Link>
        <h2 className="text-xl font-bold">สร้างกลุ่มใหม่</h2>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="w-full max-w-2xl bg-white shadow-md rounded-lg p-6 flex flex-col flex-1">
        
        {/* Error Message */}
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
            required
          />
        </div>

        {/* Tabs (เพื่อน / กลุ่ม) */}
        <div className="flex border-b mb-4">
          <button
            type="button"
            className={`py-2 px-6 ${activeTab === 'friends' ? 'border-b-2 border-blue-500 text-blue-500 font-semibold' : 'text-gray-500'}`}
            onClick={() => setActiveTab('friends')}
          >
            เพื่อน
          </button>
          <button
            type="button"
            className={`py-2 px-6 ${activeTab === 'groups' ? 'border-b-2 border-blue-500 text-blue-500 font-semibold' : 'text-gray-500'}`}
            onClick={() => setActiveTab('groups')}
          >
            กลุ่ม
          </button>
        </div>


        {/* Selected Members Display */}
        {selectedUsers.length > 0 && (
          <div className="mb-6">
            <h3 className="font-semibold mb-2">สมาชิกที่เลือก ({selectedUsers.length} คน)</h3>
            <div className="flex gap-4 flex-wrap">
              {selectedUserDetails.map(user => (
                  <div key={user.userId} className="relative text-center w-16">
                    <img
                      src={user.profilePictureUrl || 'https://via.placeholder.com/150'}
                      alt={user.fullName}
                      className="w-16 h-16 rounded-full object-cover mx-auto"
                    />
                    <span className="text-sm block mt-1 truncate">{user.fullName}</span>
                    
                    {/* ปุ่ม X (ป้องกันการลบตัวเอง) */}
                    {user.userId !== parseInt(currentUserId) && (
                      <button
                        type="button"
                        onClick={() => handleRemoveSelected(user.userId)}
                        className="absolute -top-1 -right-1 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs"
                      >
                        ✕
                      </button>
                    )}
                  </div>
                ))}
            </div>
          </div>
        )}

        
        {/* Tab Content: Friends */}
        {activeTab === 'friends' && (
          <div className="flex flex-col flex-1">
            {/* Search Bar (Friends) */}
            <div className="mb-4">
              <input
                type="text"
                placeholder="ค้นหาชื่อเพื่อน"
                value={friendSearchTerm} 
                onChange={(e) => setFriendSearchTerm(e.target.value)} 
                className="w-full border rounded-md p-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* All Friends List */}
            <div className="flex-1 overflow-y-auto mb-6">
              <h3 className="font-semibold mb-2">เพื่อนทั้งหมด</h3>
              <div className="divide-y border rounded-md max-h-64 overflow-y-auto">
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
                      disabled={user.userId === parseInt(currentUserId)} // ป้องกันการติ๊กตัวเองออก
                      className="w-5 h-5"
                    />
                  </label>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Tab Content: Groups */}
        {activeTab === 'groups' && (
          <div className="flex flex-col flex-1">
            {/* Search Bar (Groups) */}
            <div className="mb-4">
              <input
                type="text"
                placeholder="ค้นหาชื่อกลุ่ม"
                value={groupSearchTerm}
                onChange={(e) => setGroupSearchTerm(e.target.value)}
                className="w-full border rounded-md p-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* All Groups List */}
            <div className="flex-1 overflow-y-auto mb-6">
              <h3 className="font-semibold mb-2">กลุ่มทั้งหมด</h3>
              <div className="divide-y border rounded-md max-h-64 overflow-y-auto">
                {filteredGroups.map(group => (
                  <div
                    key={group.groupId}
                    className="flex items-center justify-between p-3"
                  >
                    <div className="flex items-center">
                      <span>{group.groupName}</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleAddGroupMembers(group.groupId)}
                      className="bg-blue-100 text-blue-700 text-sm font-semibold px-3 py-1 rounded-full hover:bg-blue-200"
                    >
                      เพิ่มสมาชิก
                    </button>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}


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