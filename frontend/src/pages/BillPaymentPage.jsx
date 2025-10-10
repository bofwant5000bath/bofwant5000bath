// src/pages/BillPaymentPage.jsx
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

const BillPaymentPage = () => {
  const { groupId, billId } = useParams();
  const navigate = useNavigate();

  const [bill, setBill] = useState(null);
  const [paymentAmount, setPaymentAmount] = useState("");
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // ⚙️ จำลอง user ปัจจุบัน (อาจเปลี่ยนเป็น token login ได้ในภายหลัง)
  const currentUserId = 1;

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("th-TH", {
      style: "currency",
      currency: "THB",
      minimumFractionDigits: 2,
    })
      .format(amount)
      .replace("฿", "฿");
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("th-TH", {
      day: "numeric",
      month: "short",
      year: "numeric",
    });
  };

  // ✅ โหลดข้อมูลจาก group endpoint แล้วหา billId ที่ตรงกัน
  useEffect(() => {
    const fetchBill = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8080/api/bills/group/${groupId}`
        );
        const data = res.data;
        const foundBill = data.bills?.find(
          (b) => b.billId === parseInt(billId)
        );

        if (!foundBill) {
          throw new Error("ไม่พบบิลในกลุ่มนี้");
        }

        setBill(foundBill);

        // หาผู้ใช้ปัจจุบัน
        const foundUser = foundBill.participants?.find(
          (p) => p.user?.userId === currentUserId
        );

        if (foundUser) {
          setCurrentUser({
            id: foundUser.user.userId,
            name: foundUser.user.fullName,
            avatarUrl: foundUser.user.profilePictureUrl,
            totalShare: foundUser.splitAmount,
            amountPaid: foundUser.paidAmount || 0,
          });

          const remaining =
            foundUser.splitAmount - (foundUser.paidAmount || 0);
          setPaymentAmount(remaining > 0 ? remaining.toFixed(2) : "0.00");
        }
      } catch (err) {
        console.error("❌ โหลดข้อมูลบิลล้มเหลว:", err);
        alert("โหลดข้อมูลบิลไม่สำเร็จ กรุณาลองใหม่");
      } finally {
        setLoading(false);
      }
    };

    fetchBill();
  }, [groupId, billId]);

  // ✅ ฟังก์ชันบันทึกการชำระเงิน (แก้ endpoint + field)
  const handleSavePayment = async () => {
    if (!paymentAmount || parseFloat(paymentAmount) <= 0) {
      alert("กรุณาระบุจำนวนเงินที่ถูกต้องก่อนบันทึก");
      return;
    }

    try {
      const payload = {
        billId: parseInt(billId),
        payerUserId: currentUserId, // ✅ เปลี่ยนชื่อให้ตรง backend
        amount: parseFloat(paymentAmount),
      };

      console.log("📤 ส่งข้อมูลไป backend:", payload);

      await axios.post("http://localhost:8080/api/payments/create", payload); // ✅ endpoint ใหม่

      alert(`✅ บันทึกการจ่ายเงินจำนวน ${formatCurrency(paymentAmount)} สำเร็จ`);
      navigate(`/bill/${groupId}`);
    } catch (err) {
      console.error("❌ บันทึกการจ่ายเงินล้มเหลว:", err);
      alert("เกิดข้อผิดพลาดในการบันทึก กรุณาลองใหม่อีกครั้ง");
    }
  };

  if (loading) return <div className="text-center mt-10">กำลังโหลด...</div>;
  if (!bill || !currentUser)
    return <div className="text-center mt-10 text-red-500">ไม่พบบิล</div>;

  const remaining = currentUser.totalShare - currentUser.amountPaid;
  let statusText = "";
  let statusColor = "";
  let detailText = "";

  if (remaining <= 0) {
    statusText = "จ่ายแล้ว";
    statusColor = "text-green-600";
    detailText = `จ่ายแล้ว ${formatCurrency(currentUser.totalShare)}`;
  } else if (currentUser.amountPaid > 0) {
    statusText = "ยังจ่ายไม่ครบ";
    statusColor = "text-yellow-600";
    detailText = `ยังจ่ายไม่ครบ ${formatCurrency(remaining)}`;
  } else {
    statusText = "ยังไม่ได้จ่าย";
    statusColor = "text-red-500";
    detailText = `เป็นหนี้ ${formatCurrency(currentUser.totalShare)}`;
  }

  return (
    <div className="bg-gray-100 min-h-screen font-sans">
      <header className="bg-white shadow-md p-4">
        <div className="container mx-auto flex items-center space-x-4">
          <button
            onClick={() => navigate(`/bill/${groupId}`)}
            className="p-2 rounded-full hover:bg-gray-200"
          >
            <i className="material-icons text-gray-600">arrow_back</i>
          </button>
          <h1 className="text-xl font-bold text-gray-800">{bill.title}</h1>
        </div>
      </header>

      <main className="container mx-auto p-4">
        <div className="bg-white rounded-xl shadow-md p-6 space-y-6">
          <div>
            <h2 className="text-lg font-semibold text-gray-800 mb-2">
              รายละเอียดบิล
            </h2>
            <p className="text-gray-600">{bill.description || "-"}</p>
          </div>

          <div className="flex items-center space-x-3 border-t border-gray-100 pt-4">
            <img
              src={
                bill.paidByUser?.profilePictureUrl ||
                "https://cdn-icons-png.flaticon.com/512/149/149071.png"
              }
              alt={bill.paidByUser?.fullName}
              className="w-10 h-10 rounded-full object-cover"
            />
            <div>
              <p className="font-medium text-gray-800">
                {bill.paidByUser?.fullName || "ไม่ระบุผู้จ่าย"}
              </p>
              <p className="text-sm text-gray-500">ผู้จ่ายเงิน</p>
            </div>
          </div>

          <div className="flex justify-between items-center border-t border-gray-100 pt-4">
            <p className="text-gray-600 font-medium">ยอดรวมทั้งหมด:</p>
            <p className="text-lg font-semibold text-green-700">
              ฿{bill.amount?.toLocaleString()}
            </p>
          </div>

          <div className="border-t border-gray-100 pt-4">
            <h3 className="text-md font-semibold text-gray-800 mb-3">
              การชำระเงินของคุณ
            </h3>
            <div className="bg-gray-50 rounded-lg p-4 space-y-2">
              <div className="flex justify-between">
                <p className="font-medium">{currentUser.name}</p>
                <p className={`${statusColor}`}>{statusText}</p>
              </div>
              <p className={`text-sm ${statusColor}`}>{detailText}</p>
              {remaining > 0 && (
                <div className="mt-4">
                  <input
                    type="number"
                    className="border rounded-md p-2 w-full text-right"
                    value={paymentAmount}
                    onChange={(e) => setPaymentAmount(e.target.value)}
                  />
                  <button
                    onClick={handleSavePayment}
                    className="mt-3 w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
                  >
                    บันทึกการชำระเงิน
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BillPaymentPage;
