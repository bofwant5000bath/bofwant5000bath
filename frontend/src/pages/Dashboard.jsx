import React, { useState, useEffect } from "react";
import apiClient from "../api/api.js"; // Axios instance ที่มี withCredentials:true
import { useNavigate, Link } from "react-router-dom";

const Dashboard = () => {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // ฟังก์ชัน normalize: map pinned → isPinned
  const normalizeDashboard = (data) => ({
    ...data,
    groups: data.groups.map((g) => ({
      ...g,
      isPinned: Boolean(g.pinned),
    })),
  });

  const formatCurrency = (amount) => {
    if (amount == null || isNaN(amount)) return "0.00";
    return amount.toLocaleString("th-TH", { minimumFractionDigits: 2 });
  };

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const userId = localStorage.getItem("user_id");
        if (!userId) throw new Error("User not logged in.");

        const response = await apiClient.get(
          `/groups/dashboard/${userId}`,
          {
            params: { _: new Date().getTime() }, // prevent cache
            withCredentials: true, // ✅ ต้องส่ง cookie session
          }
        );

        setDashboardData(normalizeDashboard(response.data));

        if (response.data.fullName) {
          localStorage.setItem("full_name", response.data.fullName);
        }
        if (response.data.profilePictureUrl) {
          localStorage.setItem(
            "profile_picture_url",
            response.data.profilePictureUrl
          );
        }
      } catch (err) {
        console.error("Error fetching dashboard data:", err);
        setError("ไม่สามารถโหลดข้อมูลได้ กรุณาลองใหม่อีกครั้ง");

        if (err.response) {
          if (err.response.status === 401) navigate("/login");
          if (err.response.status === 403)
            console.error("Cookie might be missing or session expired");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("user_id");
    localStorage.removeItem("full_name");
    localStorage.removeItem("profile_picture_url");
    navigate("/login");
  };

  const handleTogglePin = async (groupId, isPin) => {
    const userId = localStorage.getItem("user_id");
    if (!userId) {
      setError("ไม่พบ User ID กรุณาล็อกอินใหม่");
      return;
    }

    // Optimistic UI Update
    setDashboardData((prevData) => ({
      ...prevData,
      groups: prevData.groups.map((g) =>
        g.groupId === groupId ? { ...g, isPinned: isPin } : g
      ),
    }));

    try {
      await apiClient.post(
        "/groups/pin",
        {
          userId: parseInt(userId),
          groupId: groupId,
          pin: isPin,
        },
        {
          withCredentials: true,
        }
      );
    } catch (err) {
      console.error("Error toggling pin:", err);
      setError("เกิดข้อผิดพลาดในการปักหมุด");

      // Rollback
      setDashboardData((prevData) => ({
        ...prevData,
        groups: prevData.groups.map((g) =>
          g.groupId === groupId ? { ...g, isPinned: !isPin } : g
        ),
      }));
    }
  };

  const userName = localStorage.getItem("full_name") || "User";
  const userProfile =
    localStorage.getItem("profile_picture_url") ||
    "https://via.placeholder.com/150";

  if (loading)
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-100 font-sarabun">
        <p>กำลังโหลด...</p>
      </div>
    );

  if (error)
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-100 font-sarabun">
        <div className="bg-white p-6 rounded-2xl shadow-lg w-full max-w-lg text-center text-red-500">
          <p>{error}</p>
        </div>
      </div>
    );

  if (!dashboardData)
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-100 font-sarabun">
        <p>ยังไม่มีข้อมูล โปรดสร้างกลุ่มและบิล</p>
      </div>
    );

  const sortedGroups = [...dashboardData.groups].sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1;
    if (!a.isPinned && b.isPinned) return 1;
    return 0;
  });

  const pinnedGroups = sortedGroups.filter((g) => g.isPinned);
  const otherGroups = sortedGroups.filter((g) => !g.isPinned);

  const GroupCard = ({ group }) => (
    <div
      onClick={() => navigate(`/bill/${group.groupId}`)}
      className="bg-white rounded-xl shadow p-5 border cursor-pointer hover:shadow-lg transition relative"
    >
      <button
        onClick={(e) => {
          e.stopPropagation();
          handleTogglePin(group.groupId, !group.isPinned);
        }}
        className={`absolute -top-4 -right-4 p-2 rounded-full shadow-md transition-all duration-200
          ${group.isPinned
            ? "bg-white text-blue-500"
            : "bg-white text-gray-400 hover:text-blue-500"}
        `}
        title={group.isPinned ? "เลิกปักหมุด" : "ปักหมุดกลุ่มนี้"}
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="20"
          height="20"
          fill="currentColor"
        >
          <path d="M16 12V4H17C17.5523 4 18 3.55228 18 3C18 2.44772 17.5523 2 17 2H7C6.44772 2 6 2.44772 6 3C6 3.55228 6.44772 4 7 4H8V12L6 14V16H11.2599L12 22L12.7401 16H18V14L16 12Z" />
        </svg>
      </button>

      <div className="flex justify-between items-start mb-2">
        <div>
          <h2 className="font-semibold text-lg">{group.groupName}</h2>
          <p className="text-sm text-gray-500">{group.memberCount} สมาชิก</p>
        </div>

        <div className="text-right flex-shrink-0">
          <p className="font-bold text-gray-800 text-lg">
            ฿{formatCurrency(group.groupTotalAmount)}
          </p>
          <p className="text-sm text-gray-500">ยอดรวมกลุ่ม</p>
        </div>
      </div>

      <p className="text-sm text-red-500">
        คุณเป็นหนี้: ฿{formatCurrency(group.myDebt)}
      </p>
      <p className="text-sm text-green-600">
        คนอื่นเป็นหนี้คุณ: ฿{formatCurrency(group.othersDebtToMe)}
      </p>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50 font-sarabun">
      <div className="flex justify-between items-center bg-white px-6 py-4 shadow">
        <div className="flex items-center gap-3">
          <img
            src={userProfile}
            alt="Profile"
            className="w-12 h-12 rounded-full object-cover"
          />
          <div>
            <h2 className="font-bold text-lg">{userName}</h2>
            <p className="text-sm text-red-500">
              ยอดรวมที่ต้องชำระ: ฿{formatCurrency(dashboardData.totalOwed)}
            </p>
            <p className="text-sm text-green-600">
              ยอดรวมที่จะได้รับ: ฿{formatCurrency(dashboardData.totalReceivable)}
            </p>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="text-gray-600 hover:text-red-500 transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="26"
            height="26"
            stroke="currentColor"
            fill="none"
          >
            <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
            <polyline points="10 17 15 12 10 7" />
            <line x1="15" y1="12" x2="3" y2="12" />
          </svg>
        </button>
      </div>

      <div className="p-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">กลุ่มของฉัน</h1>
          <Link
            to="/create-group"
            className="flex items-center gap-2 bg-blue-500 text-white px-4 py-2 rounded-lg shadow hover:bg-blue-600"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              stroke="currentColor"
              fill="none"
            >
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            สร้างกลุ่มใหม่
          </Link>
        </div>

        {pinnedGroups.length > 0 && (
          <div className="mb-8">
            <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                fill="currentColor"
                className="text-blue-500"
              >
                <path d="M16 12V4H17C17.5523 4 18 3.55228 18 3C18 2.44772 17.5523 2 17 2H7C6.44772 2 6 2.44772 6 3C6 3.55228 6.44772 4 7 4H8V12L6 14V16H11.2599L12 22L12.7401 16H18V14L16 12Z" />
              </svg>
              ปักหมุด
            </h2>
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {pinnedGroups.map((group) => (
                <GroupCard key={group.groupId} group={group} />
              ))}
            </div>
          </div>
        )}

        <div>
          <h2 className="text-xl font-bold text-gray-800 mb-4">ทั้งหมด</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {otherGroups.map((group) => (
              <GroupCard key={group.groupId} group={group} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
