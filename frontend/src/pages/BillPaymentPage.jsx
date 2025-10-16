// src/pages/BillPaymentPage.jsx
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import apiClient from '../api/api.js';

const BillPaymentPage = () => {
  const { groupId, billId } = useParams();
  const navigate = useNavigate();

  const [bill, setBill] = useState(null);
  const [paymentAmount, setPaymentAmount] = useState("");
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // ✅ แก้ไขตรงนี้: ดึง user id จาก localStorage
  // ใช้ parseInt เพื่อแปลง string ที่ได้จาก localStorage ให้เป็น number สำหรับการเปรียบเทียบ
  const currentUserId = parseInt(localStorage.getItem("user_id"));

  const formatCurrency = (amount) => {
    // ... (ฟังก์ชันเดิม) ...
    return new Intl.NumberFormat("th-TH", {
      style: "currency",
      currency: "THB",
      minimumFractionDigits: 2,
    })
      .format(amount)
      .replace("฿", "฿");
  };

  useEffect(() => {
    // ✅ เพิ่มการตรวจสอบว่ามี user_id หรือไม่
    if (!currentUserId) {
      alert("ไม่พบข้อมูลผู้ใช้, กรุณาเข้าสู่ระบบใหม่");
      navigate('/login');
      return;
    }

    const fetchBillAndPayments = async () => {
      try {
        const groupRes = await apiClient.get(
          `/bills/group/${groupId}`
        );
        // ... (โค้ดส่วนที่เหลือใน useEffect เหมือนเดิมทั้งหมด) ...
        const foundBill = groupRes.data.bills?.find(
          (b) => b.billId === parseInt(billId)
        );

        if (!foundBill) {
          throw new Error("ไม่พบบิลในกลุ่มนี้");
        }
        setBill(foundBill);

        const paymentsRes = await apiClient.get(
          `/payments/bill/${billId}`
        );
        const payments = paymentsRes.data;

        const userParticipantInfo = foundBill.participants?.find(
          (p) => p.user?.userId === currentUserId
        );

        if (userParticipantInfo) {
          const totalPaidByCurrentUser = payments
            .filter((p) => p.payerUser.userId === currentUserId)
            .reduce((sum, p) => sum + p.amount, 0);

          setCurrentUser({
            id: userParticipantInfo.user.userId,
            name: userParticipantInfo.user.fullName,
            avatarUrl: userParticipantInfo.user.profilePictureUrl,
            totalShare: userParticipantInfo.splitAmount,
            amountPaid: totalPaidByCurrentUser,
          });

          const remaining =
            userParticipantInfo.splitAmount - totalPaidByCurrentUser;
          setPaymentAmount(remaining > 0 ? remaining.toFixed(2) : "0.00");
        }
      } catch (err) {
        console.error("❌ โหลดข้อมูลบิลหรือการจ่ายเงินล้มเหลว:", err);
        alert("โหลดข้อมูลไม่สำเร็จ กรุณาลองใหม่");
      } finally {
        setLoading(false);
      }
    };

    fetchBillAndPayments();
  }, [groupId, billId, currentUserId, navigate]); // เพิ่ม dependency เพื่อให้สอดคล้อง


  // ... (โค้ดส่วนที่เหลือทั้งหมดเหมือนเดิม) ...
  const handleSavePayment = async () => {
    const enteredAmount = parseFloat(paymentAmount);
    if (!enteredAmount || enteredAmount <= 0) {
      alert("กรุณาระบุจำนวนเงินที่ถูกต้องก่อนบันทึก");
      return;
    }

    const remainingAmount = currentUser.totalShare - currentUser.amountPaid;

    if (enteredAmount > remainingAmount + 0.009) {
      const overpaymentAmount = enteredAmount - remainingAmount;
      const confirmationMessage = `คุณกำลังชำระเงินเกินจำนวนที่ต้องจ่ายเป็นเงิน ${formatCurrency(
        overpaymentAmount
      )}\n\nคุณต้องการยืนยันการทำรายการต่อหรือไม่?`;
      
      const isConfirmed = window.confirm(confirmationMessage);

      if (!isConfirmed) {
        return;
      }
    }

    try {
      const payload = {
        billId: parseInt(billId),
        payerUserId: currentUserId,
        amount: enteredAmount, 
      };

      await apiClient.post("/payments/create", payload);

      alert(`✅ บันทึกการจ่ายเงินจำนวน ${formatCurrency(enteredAmount)} สำเร็จ`);
      navigate(`/bill/${groupId}`);
    } catch (err) {
      console.error("❌ บันทึกการจ่ายเงินล้มเหลว:", err);
      alert("เกิดข้อผิดพลาดในการบันทึก กรุณาลองใหม่อีกครั้ง");
    }
  };

  if (loading) return <div className="text-center mt-10">กำลังโหลด...</div>;
  if (!bill || !currentUser)
    return <div className="text-center mt-10 text-red-500">ไม่พบบิล หรือคุณไม่ได้อยู่ในบิลนี้</div>;

  const remaining = currentUser.totalShare - currentUser.amountPaid;
  let statusText = "";
  let statusColor = "";
  let detailText = "";

  if (
    currentUser.id === bill.paidByUser?.userId ||
    remaining <= 0.009
  ) {
    statusText = "ชำระครบแล้ว";
    statusColor = "text-green-600";
    detailText = `จ่ายแล้ว ${formatCurrency(currentUser.totalShare)}`;
  } else if (currentUser.amountPaid > 0) {
    statusText = "ยังจ่ายไม่ครบ";
    statusColor = "text-yellow-600";
    detailText = `ยังค้างชำระ ${formatCurrency(remaining)}`;
  } else {
    statusText = "ยังไม่ได้จ่าย";
    statusColor = "text-red-500";
    detailText = `ยอดที่ต้องชำระ ${formatCurrency(currentUser.totalShare)}`;
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
                <p className={`${statusColor} font-semibold`}>{statusText}</p>
              </div>
              <p className={`text-sm ${statusColor}`}>{detailText}</p>
              
              {currentUser.id !== bill.paidByUser?.userId &&
                remaining > 0.009 && (
                  <div className="mt-4">
                    <label className="text-sm text-gray-600">
                      ระบุจำนวนเงินที่ต้องการจ่าย:
                    </label>
                    <input
                      type="number"
                      className="border rounded-md p-2 w-full text-right mt-1"
                      value={paymentAmount}
                      onChange={(e) => setPaymentAmount(e.target.value)}
                    />
                    <button
                      onClick={handleSavePayment}
                      className="mt-3 w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition"
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