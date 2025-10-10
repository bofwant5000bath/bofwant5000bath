import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

const Bill = () => {
  const { groupId } = useParams();
  const navigate = useNavigate();
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [summary, setSummary] = useState({ totalAmount: 0 });

  const currentUserId = parseInt(localStorage.getItem("user_id") || "1");

  const formatCurrency = (amount) => {
    if (amount == null || isNaN(amount)) return "฿0.00";
    return new Intl.NumberFormat("th-TH", {
      style: "currency",
      currency: "THB",
      minimumFractionDigits: 2,
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("th-TH", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  // ✅ โหลดข้อมูลบิลจาก backend
  useEffect(() => {
    const fetchBills = async () => {
      try {
        setLoading(true);
        setError(null);

        const billRes = await axios.get(
          `http://localhost:8080/api/bills/group/${groupId}`
        );

        console.log("✅ ข้อมูลจาก backend:", billRes.data);

        const { groupMembers, bills } = billRes.data;

        if (!Array.isArray(bills)) {
          throw new Error("ข้อมูล bills ไม่ถูกต้องจาก backend");
        }

        setBills(bills);

        if (groupMembers) {
          localStorage.setItem("groupMembers", JSON.stringify(groupMembers));
        }

        const totalAmount = bills.reduce(
          (sum, bill) => sum + (bill.amount || 0),
          0
        );
        setSummary({ totalAmount });
      } catch (err) {
        console.error("❌ โหลดข้อมูลบิลล้มเหลว:", err);
        setError("ไม่สามารถโหลดข้อมูลบิลได้");
      } finally {
        setLoading(false);
      }
    };

    fetchBills();
  }, [groupId]);

  // ✅ UI เดิมทั้งหมด (ไม่แตะ layout)
  if (loading)
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-100 font-sarabun">
        <p>กำลังโหลดข้อมูล...</p>
      </div>
    );

  if (error)
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-100 font-sarabun text-red-500">
        <p>{error}</p>
      </div>
    );

  const groupName = bills[0]?.group?.groupName || "กลุ่มของฉัน";
  const memberCount = new Set(
    bills.flatMap(
      (b) => [
        b.paidByUser?.userId,
        ...(b.participants || []).map((p) => p.user.userId),
      ]
    )
  ).size;

  return (
    <div className="bg-gray-100 min-h-screen font-sans">
      {/* Header */}
      <header className="bg-white shadow-sm p-4 sticky top-0 z-10">
        <div className="container mx-auto flex items-center">
          <button
            onClick={() => navigate("/dashboard")}
            className="p-2 rounded-full hover:bg-gray-100 mr-2"
          >
            <span className="material-icons text-gray-600">arrow_back</span>
          </button>
          <div>
            <h1 className="text-lg sm:text-xl font-bold text-gray-800">
              {groupName}
            </h1>
            <p className="text-sm text-gray-500">{memberCount} สมาชิก</p>
          </div>
        </div>
      </header>

      {/* Content */}
      <main className="container mx-auto p-4 pb-10">
        {/* Summary Box */}
        <div className="bg-white shadow-sm rounded-xl p-6 flex justify-between items-center mb-6">
          <div>
            <p className="text-sm text-gray-500">ยอดรวมของกลุ่ม</p>
            <p className="text-3xl font-bold text-gray-900 mt-1">
              {formatCurrency(summary.totalAmount)}
            </p>
          </div>
        </div>

        {/* Add Expense Button */}
        <div className="flex justify-end px-6 mt-4">
          <button
            onClick={() => navigate(`/addexpense/${groupId}`)} // ✅ ไปหน้า AddExpenseUnified
            className="flex items-center gap-2 bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg shadow"
          >
            + เพิ่มค่าใช้จ่าย
          </button>
        </div>

        {/* Bill Cards */}
        <div className="space-y-4">
          {bills.map((bill) => (
            <div
              key={bill.billId}
              onClick={() => navigate(`/bill/${groupId}/payment/${bill.billId}`)}
              className="bg-white rounded-xl shadow-sm p-5 border border-gray-100 hover:bg-gray-50 cursor-pointer transition"
            >
              <div className="flex justify-between items-center mb-1">
                <div>
                  <h3 className="font-bold text-gray-800 text-base sm:text-lg">
                    {bill.title}
                  </h3>
                  <p className="text-sm text-gray-500">
                    จ่ายโดย:{" "}
                    <span className="font-medium text-gray-700">
                      {bill.paidByUser?.fullName}
                    </span>
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-bold text-lg sm:text-xl text-gray-900">
                    {formatCurrency(bill.amount)}
                  </p>
                  <p className="text-xs text-gray-400">
                    {formatDate(bill.billDate)}
                  </p>
                </div>
              </div>

              {/* ✅ Participant Section */}
              <div className="mt-4 pt-3 border-t border-gray-200 space-y-2 text-sm">
                {bill.participants?.map((p, index) => (
                  <div key={index} className="flex justify-between items-center">
                    <div className="flex items-center gap-2">
                      <img
                        src={
                          p.user?.profilePictureUrl ||
                          "https://via.placeholder.com/150"
                        }
                        alt={p.user?.fullName || "unknown"}
                        className="w-7 h-7 rounded-full object-cover"
                      />
                      <p className="text-gray-600">
                        {p.user?.fullName} เป็นหนี้{" "}
                        {bill.paidByUser?.userId === currentUserId
                          ? "คุณ"
                          : bill.paidByUser?.fullName}
                      </p>
                    </div>

                    {/* ✅ สีตามสถานะ */}
                    <p
                      className={`font-semibold ${p.status === "paid"
                          ? "text-green-500"
                          : p.status === "partial"
                            ? "text-yellow-500"
                            : "text-red-500"
                        }`}
                    >
                      {formatCurrency(p.splitAmount)}{" "}
                      {p.status === "paid"
                        ? "(ชำระแล้ว)"
                        : p.status === "partial"
                          ? "(ค้างบางส่วน)"
                          : "(ยังไม่ชำระ)"}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
};

export default Bill;
