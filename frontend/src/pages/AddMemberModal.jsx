import React, { useState, useEffect, useRef } from 'react';
import apiClient from '../api/api.js'; // ❗️ ตรวจสอบว่า Path นี้ถูกต้องหรือไม่ (อาจจะต้องเป็น '../api/api.js')

// Component นี้อิง Logic มาจาก CreateGroup.jsx ตามที่ร้องขอ
const AddMemberModal = ({ isOpen, onClose, groupId, currentUserId, onMembersAdded }) => {
  // --- States จาก CreateGroup.jsx ---
  const [allUsers, setAllUsers] = useState([]);
  const [allGroups, setAllGroups] = useState([]);
  const [groupMemberMap, setGroupMemberMap] = useState({});
  const [friendSearchTerm, setFriendSearchTerm] = useState('');
  const [groupSearchTerm, setGroupSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('friends'); // 'friends' หรือ 'groups'

  // --- States สำหรับ Modal นี้ ---
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // existingMemberIds: เก็บ Set ของ ID สมาชิก *ที่มีอยู่เดิม* ในกลุ่ม
  const [existingMemberIds, setExistingMemberIds] = useState(new Set());
  
  // selectedUserIds: สมาชิกทั้งหมด (ทั้งเก่า + ที่เลือกใหม่)
  const [selectedUserIds, setSelectedUserIds] = useState([]);

  const isSubmitting = useRef(false);
  const currentUserIdNum = parseInt(currentUserId);

  // Effect 1: โหลด "สมาชิกเดิม" ของกลุ่มนี้จาก localStorage (ที่ Bill.jsx บันทึกไว้)
  useEffect(() => {
    if (!isOpen) return; // ทำงานเมื่อ Modal เปิดเท่านั้น

    let initialIds = new Set([currentUserIdNum]); // อย่างน้อยต้องมีตัวเราเอง
    try {
      const storedMembers = localStorage.getItem('groupMembers');
      if (storedMembers) {
        const parsedMembers = JSON.parse(storedMembers);
        // groupMembers ที่ Bill.jsx เก็บไว้คือ Array of { userId, fullName, ... }
        const ids = new Set(parsedMembers.map(member => member.userId));
        initialIds = ids;
      }
    } catch (e) {
      console.error("Failed to parse group members from localStorage", e);
    }
    
    setExistingMemberIds(initialIds);
    setSelectedUserIds(Array.from(initialIds)); // เลือกสมาชิกเดิมไว้ล่วงหน้า
    
    // Reset state ของ Modal
    setActiveTab('friends');
    setFriendSearchTerm('');
    setGroupSearchTerm('');
    setError('');

  }, [isOpen, currentUserIdNum]);

  // Effect 2: ดึง "เพื่อนทั้งหมด" (สำหรับ Tab "เพื่อน")
  useEffect(() => {
    if (!isOpen) return;

    const fetchAllUsers = async () => {
      try {
        // ❗️ ตรวจสอบ Path นี้ด้วยครับ
        const response = await apiClient.get('/groups/users');
        setAllUsers(response.data);
      } catch (err) {
        setError('ไม่สามารถดึงรายชื่อผู้ใช้ได้');
        console.error('Error fetching users:', err);
      }
    };
    fetchAllUsers();
  }, [isOpen]);

  // Effect 3: ดึง "กลุ่มทั้งหมดและสมาชิก" (สำหรับ Tab "กลุ่ม")
  useEffect(() => {
    if (!isOpen || !currentUserId) return;

    const fetchAllGroupsAndMembers = async () => {
      try {
        // ❗️ ตรวจสอบ Path นี้ด้วยครับ
        const response = await apiClient.get(`/groups/all-with-members/${currentUserId}`);
        const groupsData = response.data;

        const groupsList = groupsData.map(group => ({
          groupId: group.groupId,
          groupName: group.groupName,
        }));
        setAllGroups(groupsList);

        const memberMap = groupsData.reduce((acc, group) => {
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
  }, [isOpen, currentUserId]);

  // --- Handlers ---

  const handleUserToggle = (userId) => {
    // ป้องกันการติ๊ก "สมาชิกเดิม" ออก
    if (existingMemberIds.has(userId)) return;
    
    setSelectedUserIds(prevSelected => {
      if (prevSelected.includes(userId)) {
        return prevSelected.filter(id => id !== userId);
      } else {
        return [...prevSelected, userId];
      }
    });
  };

  const handleRemoveSelected = (userId) => {
    // ป้องกันการลบ "สมาชิกเดิม" ออกจาก Tag
    if (existingMemberIds.has(userId)) return;
    
    setSelectedUserIds(prev => prev.filter(id => id !== userId));
  };

  // Logic: เมื่อคลิก "เพิ่มสมาชิก" จากกลุ่ม
  const handleAddGroupMembers = (groupId) => {
    const memberIds = groupMemberMap[groupId] || [];

    setSelectedUserIds(prevSelected => {
      const combinedIds = new Set([...prevSelected, ...memberIds]);
      return Array.from(combinedIds);
    });
  };

  // --- Submit Handler ---
  const handleSubmit = async () => {
    if (isSubmitting.current) return;

    // 1. คัดกรองเอา "เฉพาะ" สมาชิกใหม่
    const newMemberIds = selectedUserIds.filter(id => !existingMemberIds.has(id));

    // 2. ถ้าไม่มีสมาชิกใหม่เพิ่ม ให้ปิด Modal
    if (newMemberIds.length === 0) {
      onClose();
      return;
    }

    isSubmitting.current = true;
    setLoading(true);
    setError('');

    try {
      const payload = {
        memberIds: newMemberIds, // ส่งเฉพาะ ID ใหม่
      };
      
      // 3. ยิง API (❗️ ตรวจสอบ Path นี้ด้วยครับ)
      const response = await apiClient.post(`/groups/${groupId}/members`, payload);
      
      // 4. สำเร็จ: เรียก Callback ที่ Bill.jsx ส่งมา
      onMembersAdded(response.data); 
      onClose(); // ปิด Modal

    } catch (err) {
      setError('เกิดข้อผิดพลาดในการเพิ่มสมาชิก');
      console.error('Error adding members:', err);
    } finally {
      setLoading(false);
      isSubmitting.current = false;
    }
  };

  // --- Render Logic ---

  if (!isOpen) return null;

  const filteredUsers = allUsers.filter(user =>
    user.fullName.toLowerCase().includes(friendSearchTerm.toLowerCase())
  );

  const filteredGroups = allGroups.filter(group =>
    group.groupName.toLowerCase().includes(groupSearchTerm.toLowerCase())
  );

  const selectedUserDetails = selectedUserIds.map(id => {
    const user = allUsers.find(u => u.userId === id);
    if (user) return user;

    const storedMembers = JSON.parse(localStorage.getItem('groupMembers') || '[]');
    const existingInfo = storedMembers.find(m => m.userId === id);
    if (existingInfo) return existingInfo;
    
    return { 
      userId: id, 
      fullName: id === currentUserIdNum ? 'คุณ' : `สมาชิก #${id}`, 
      profilePictureUrl: 'https://via.placeholder.com/150' 
    };
  });


  return (
    // Modal Backdrop
    <div className="fixed inset-0 bg-black bg-opacity-50 z-40 flex justify-center items-center p-4 font-sarabun">
      
      {/* Modal Content */}
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg flex flex-col max-h-[80vh]">
        
        {/* Header */}
        <div className="flex justify-between items-center p-4 border-b">
          <h3 className="text-lg font-bold text-gray-800">เพิ่มผู้เข้าร่วม</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-800 text-3xl font-light"
          >
            &times;
          </button>
        </div>

        {/* Body (Scroll) */}
        <div className="p-4 flex-1 overflow-y-auto">
          
          {/* Error Message */}
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg relative mb-4" role="alert">
              <span className="block sm:inline">{error}</span>
            </div>
          )}

          {/* 1. ผู้เข้าร่วมที่เลือก (Tags) */}
          {selectedUserIds.length > 0 && (
            <div className="mb-4">
              <h3 className="font-semibold mb-2 text-sm text-gray-600">ผู้เข้าร่วมที่เลือก</h3>
              <div className="flex flex-wrap gap-2 p-2 rounded-lg bg-gray-50 border min-h-[40px]">
                {selectedUserDetails.map(user => (
                  <div 
                    key={user.userId} 
                    className={`flex items-center rounded-full pl-2 pr-1 py-1 text-sm font-medium
                      ${existingMemberIds.has(user.userId) 
                        ? 'bg-gray-200 text-gray-700' 
                        : 'bg-blue-100 text-blue-800'
                      }
                    `}
                  >
                    <img 
                      src={user.profilePictureUrl || 'https://via.placeholder.com/150'} 
                      alt={user.fullName} 
                      className="w-5 h-5 rounded-full object-cover mr-1.5"
                    />
                    <span>{user.fullName}</span>
                    
                    {!existingMemberIds.has(user.userId) && (
                      <button
                        type="button"
                        onClick={() => handleRemoveSelected(user.userId)}
                        className="ml-1.5 text-blue-500 hover:text-blue-700"
                      >
                        &times;
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
          
          {/* 2. ช่องค้นหา */}
          <div className="mb-4 relative">
             <span className="material-icons absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                search
             </span>
             <input
                type="text"
                placeholder={activeTab === 'friends' ? 'ค้นหาชื่อเพื่อน' : 'ค้นหาชื่อกลุ่ม'}
                value={activeTab === 'friends' ? friendSearchTerm : groupSearchTerm}
                onChange={(e) => activeTab === 'friends' ? setFriendSearchTerm(e.target.value) : setGroupSearchTerm(e.target.value)}
                className="w-full border rounded-md p-3 pl-10 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
          </div>

          {/* 3. Tabs (เพื่อน / กลุ่ม) */}
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
          
          {/* 4. Tab Content (List) */}
          <div className="max-h-64 overflow-y-auto divide-y border rounded-md">
            
            {/* Tab: เพื่อน */}
            {activeTab === 'friends' && (
              <>
                {filteredUsers.length === 0 && <p className="p-3 text-gray-500 text-center">ไม่พบเพื่อน</p>}
                {filteredUsers.map(user => {
                  const isExisting = existingMemberIds.has(user.userId);
                  return (
                    <label
                      key={user.userId}
                      className={`flex items-center justify-between p-3 
                        ${isExisting 
                          ? 'bg-blue-50 cursor-not-allowed opacity-70'
                          : 'cursor-pointer hover:bg-gray-50'
                        }`}
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
                        checked={selectedUserIds.includes(user.userId)}
                        onChange={() => handleUserToggle(user.userId)}
                        disabled={isExisting}
                        className="w-5 h-5 accent-blue-500"
                      />
                    </label>
                  );
                })}
              </>
            )}

            {/* Tab: กลุ่ม */}
            {activeTab === 'groups' && (
              <>
                {filteredGroups.length === 0 && <p className="p-3 text-gray-500 text-center">ไม่พบกลุ่ม</p>}
                {filteredGroups.map(group => (
                  <div
                    key={group.groupId}
                    className="flex items-center justify-between p-3"
                  >
                    <span>{group.groupName}</span>
                    <button
                      type="button"
                      onClick={() => handleAddGroupMembers(group.groupId)}
                      className="bg-blue-100 text-blue-700 text-sm font-semibold px-3 py-1 rounded-full hover:bg-blue-200"
                    >
                      เพิ่มสมาชิก
                    </button>
                  </div>
                ))}
              </>
            )}
          </div>
          
        </div>

        {/* Footer (ปุ่มยืนยืน) */}
        <div className="p-4 border-t bg-gray-50">
          <button
            type="button"
            onClick={handleSubmit}
            className={`w-full py-3 rounded-md text-white font-bold ${
              loading
                ? 'bg-gray-400 cursor-not-allowed'
                : 'bg-blue-500 hover:bg-blue-600'
            }`}
            disabled={loading}
          >
            {loading ? 'กำลังบันทึก...' : 'ยืนยัน'}
          </button>
        </div>

      </div>
    </div>
  );
};

export default AddMemberModal;