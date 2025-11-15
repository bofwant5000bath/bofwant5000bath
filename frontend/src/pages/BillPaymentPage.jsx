// src/pages/BillPaymentPage.jsx
import React, { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import apiClient from '../api/api.js';

// ✅ NEW: imports สำหรับ QR Code
import QRCode from "react-qr-code"; 
import generatePayload from "promptpay-qr";

// ✅ NEW: ใส่ API Key ของคุณ (เหมือนไฟล์ที่แล้ว)
const IMGBB_API_KEY = "fd22cd3f2efd8a2f7a98628df12a7889";

const BillPaymentPage = () => {
  const { groupId, billId } = useParams();
  const navigate = useNavigate();

  const [bill, setBill] = useState(null);
  const [paymentAmount, setPaymentAmount] = useState("");
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // ✅ NEW: State สำหรับ QR Code
  const [promptPayQRData, setPromptPayQRData] = useState("");

  // ✅ NEW: State สำหรับการอัปโหลดสลิป
  const [slipFile, setSlipFile] = useState(null);
  const [slipPreviewUrl, setSlipPreviewUrl] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef(null);
  
  const currentUserId = parseInt(localStorage.getItem("user_id"));

  const formatCurrency = (amount) => {
    // ... (ฟังก์ชันเดิม) ...
    if (amount == null || isNaN(amount)) return "฿0.00";
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

        // ✅ [แก้ไขแล้ว] สร้าง QR Code เสมอ ถ้าบิลมีเบอร์ PromptPay
        if (foundBill.promptpayNumber) {
          const qrData = generatePayload(foundBill.promptpayNumber, {}); 
          setPromptPayQRData(qrData);
        }

        const paymentsRes = await apiClient.get(
          `/payments/bill/${billId}`
        );
        
        const payments = paymentsRes.data || []; 

        const userParticipantInfo = foundBill.participants?.find(
          (p) => p.user?.userId === currentUserId
        );

        if (userParticipantInfo) {
          
          const totalPaidByCurrentUser = payments 
            .filter((p) => p && p.payerUser && p.payerUser.userId === currentUserId) 
            .reduce((sum, p) => sum + (p.amount || 0), 0); 


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
        } else if (foundBill.paidByUser?.userId === currentUserId) {
          // ✅ NEW: กรณีเราเป็นผู้จ่าย (ไม่มีใน participants list)
          // ให้ตั้งค่า currentUser เพื่อให้ UI ส่วนอื่นไม่พัง
          setCurrentUser({
             id: foundBill.paidByUser.userId,
             name: foundBill.paidByUser.fullName,
             avatarUrl: foundBill.paidByUser.profilePictureUrl,
             totalShare: 0, // ผู้จ่ายไม่มียอดค้าง
             amountPaid: 0,
          });
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


  // ✅ NEW: ฟังก์ชันจัดการสลิป (เหมือน AddExpenseUnified)
  const handleFileChange = (e) => {
    const file = e.target.files && e.target.files[0];
    if (file) {
      setSlipFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setSlipPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRemoveFile = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setSlipFile(null);
    setSlipPreviewUrl(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = null;
    }
  };


  // ✅ MODIFIED: แก้ไข handleSavePayment ให้รองรับการอัปโหลดสลิป
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
      if (!isConfirmed) return;
    }

    // --- 1. เริ่ม Loading ---
    setLoading(true);
    let finalSlipUrl = null;

    // --- 2. ตรวจสอบ ImgBB Key ---
    if (IMGBB_API_KEY === "YOUR_IMGBB_API_KEY_HERE" && slipFile) {
        alert("กรุณาใส่ IMGBB_API_KEY ในโค้ดก่อนครับ (BillPaymentPage.jsx)");
        setLoading(false);
        return;
    }

    // --- 3. อัปโหลดสลิป (ถ้ามี) ---
    if (slipFile) {
      setIsUploading(true);
      const formData = new FormData();
      formData.append('image', slipFile);

      try {
        console.log("... กำลังอัปโหลดสลิปไป ImgBB...");
        const res = await fetch(
          `https://api.imgbb.com/1/upload?key=${IMGBB_API_KEY}`,
          { method: 'POST', body: formData }
        );
        const data = await res.json();
        if (data.success) {
          finalSlipUrl = data.data.url;
          console.log("... อัปโหลดสลิปสำเร็จ:", finalSlipUrl);
        } else {
          throw new Error(data.error?.message || 'อัปโหลดสลิปล้มเหลว');
        }
      } catch (uploadErr) {
        console.error("❌ การอัปโหลดสลิปล้มเหลว:", uploadErr);
        alert(`อัปโหลดสลิปล้มเหลว: ${uploadErr.message}`);
        setLoading(false);
        setIsUploading(false);
        return;
      }
      setIsUploading(false);
    }

    // --- 4. บันทึกการจ่ายเงิน ---
    try {
      const payload = {
        billId: parseInt(billId),
        payerUserId: currentUserId,
        amount: enteredAmount,
        slipImageUrl: finalSlipUrl, // ✅ ส่ง URL ของสลิปไปด้วย
      };

      await apiClient.post("/payments/create", payload);

      alert(`✅ บันทึกการจ่ายเงินจำนวน ${formatCurrency(enteredAmount)} สำเร็จ`);
      navigate(`/bill/${groupId}`);
    } catch (err) {
      console.error("❌ บันทึกการจ่ายเงินล้มเหลว:", err);
      alert("เกิดข้อผิดพลาดในการบันทึก กรุณาลองใหม่อีกครั้ง");
    } finally {
      setLoading(false);
      setIsUploading(false);
    }
  };

  if (loading && !isUploading) return <div className="text-center mt-10">กำลังโหลด...</div>;
  
  // ✅ แก้ไข: เพิ่มการตรวจสอบ `bill` ก่อนใช้งาน
  if (!bill || !currentUser)
    return <div className="text-center mt-10 text-red-500">ไม่พบบิล หรือคุณไม่ได้อยู่ในบิลนี้</div>;

  const remaining = currentUser.totalShare - currentUser.amountPaid;
  let statusText = "";
  let statusColor = "";
  let detailText = "";

  // Logic การแสดงสถานะ (เหมือนเดิม)
  if (
    currentUser.id === bill.paidByUser?.userId ||
    remaining <= 0.009
  ) {
    statusText = "ชำระครบแล้ว";
    statusColor = "text-green-600";
    // ปรับข้อความเล็กน้อยสำหรับผู้จ่าย
    detailText = currentUser.id === bill.paidByUser?.userId 
      ? "คุณเป็นผู้จ่ายบิลนี้" 
      : `จ่ายแล้ว ${formatCurrency(currentUser.totalShare)}`;
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

      {/* ✅✅✅ [แก้ไขแล้ว] ✅✅✅ */}
      {/* START: NEW UI (รับเงินคืน/สแกนจ่าย) */}
      {/* เงื่อนไข: แสดงกล่องนี้เสมอ ถ้าบิลมี PromptPay */}
      {promptPayQRData && (
        <div className="bg-white rounded-xl shadow-md p-6 space-y-4 mb-6">
          <h2 className="text-lg font-semibold text-gray-800">
            รับเงินคืน
          </h2>
          <p className="text-sm text-gray-600">
            สแกน QR Code เพื่อจ่ายเงิน หรืออัปโหลดสลิปเพื่อยืนยันการโอน
          </p>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4 border-t">
            
            {/* 1. กล่อง QR Code (แสดงเสมอ) */}
            <div className="flex flex-col items-center justify-center p-4">
              <div style={{ background: 'white', padding: '16px', borderRadius: '8px', border: '1px solid #EEE' }}>
                <QRCode
                  value={promptPayQRData}
                  size={160}
                  level={"H"}
                />
              </div>
              <p className="mt-4 text-sm font-medium text-gray-700">
                สแกนเพื่อจ่ายให้ {bill.paidByUser?.fullName}
              </p>
              <p className="text-xs text-gray-500">
                (เบอร์: {bill.promptpayNumber})
              </p>
            </div>
            
            {/* 2. ✅ [ย้ายมา] กล่องอัปโหลดสลิป (Functional) */}
            {/* แสดงเฉพาะเมื่อ "ฉัน" เป็น "ผู้ค้างชำระ" */}
            {currentUser.id !== bill.paidByUser?.userId && remaining > 0.009 ? (
              <div>
                <label className="block text-sm font-medium text-gray-700 text-center mb-3">
                  บันทึกสลิปโอนเงิน
                </label>
                {/* input ที่ซ่อนไว้ */}
                <input
                  id="slip-upload"
                  name="slip-upload"
                  type="file"
                  className="sr-only"
                  onChange={handleFileChange}
                  accept="image/png, image/jpeg, image/gif"
                  ref={fileInputRef}
                />
                {/* กล่องลากวาง */}
                <label
                  htmlFor="slip-upload"
                  className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md cursor-pointer hover:border-indigo-400"
                >
                  <div className="space-y-1 text-center">
                    {slipPreviewUrl ? (
                      <div>
                        <img
                          src={slipPreviewUrl}
                          alt="รูปสลิป"
                          className="mx-auto h-24 w-auto object-contain rounded-md"
                        />
                        <p className="text-xs text-gray-600 mt-2">{slipFile.name}</p>
                        <button
                          type="button"
                          onClick={handleRemoveFile}
                          className="mt-1 text-sm font-medium text-red-600 hover:text-red-800"
                        >
                          ลบไฟล์
                        </button>
                      </div>
                    ) : (
                      <>
                        <svg className="mx-auto h-10 w-10 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
                          <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                        <div className="flex text-sm text-gray-600 justify-center">
                          <span className="relative bg-white rounded-md font-medium text-indigo-600">
                            อัปโหลดสลิป
                          </span>
                        </div>
                        <p className="text-xs text-gray-500">PNG, JPG, GIF</p>
                      </>
                    )}
                  </div>
                </label>
              </div>
            ) : (
              // [Placeholder] แสดงเมื่อ "ฉัน" เป็นคนจ่าย หรือ จ่ายครบแล้ว
              <div className="flex flex-col items-center justify-center p-4 border border-gray-200 rounded-lg">
                <i className="material-icons text-4xl text-green-600">
                  check_circle
                </i>
                <p className="mt-2 text-md font-semibold text-gray-800">
                  ยืนยันการชำระเงิน
                </p>
                <p className="text-sm text-gray-500 text-center mt-1">
                  {currentUser.id === bill.paidByUser?.userId ? "คุณคือผู้จ่าย" : "คุณชำระบิลนี้แล้ว"}
                </p>
              </div>
            )}

          </div>
        </div>
      )}
      {/* ✅ END: NEW UI */}


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
            <p className="text-gray-600 font-medium">ยอดรวมทั้งหมด (THB):</p>
            <p className="text-lg font-semibold text-green-700">
              {formatCurrency(bill.amountInThb)}
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
              
              {/* --- ✅ [แก้ไขแล้ว] (ส่วนการจ่ายเงิน) --- */}
              {/* แสดงเฉพาะเมื่อเรา "ไม่ใช่" ผู้จ่าย และ "ยังจ่ายไม่ครบ" */}
              {currentUser.id !== bill.paidByUser?.userId &&
                remaining > 0.009 && (
                  <div className="mt-4 space-y-4 pt-4 border-t">
                    
                    {/* 1. ช่องกรอกจำนวนเงิน */}
                    <div>
                      <label className="text-sm text-gray-600">
                        ระบุจำนวนเงินที่ต้องการจ่าย:
                      </label>
                      <input
                        type="number"
                        className="border rounded-md p-2 w-full text-right mt-1"
                        value={paymentAmount}
                        onChange={(e) => setPaymentAmount(e.target.value)}
                      />
                    </div>

                    {/* 2. ✅✅✅ [ลบออก] ✅✅✅ */}
                    {/* กล่องอัปโหลดสลิปที่อยู่ด้านล่าง ถูกลบออกจากส่วนนี้แล้ว */}
                    
                    {/* 3. ปุ่มบันทึก */}
                    <button
                      onClick={handleSavePayment}
                      disabled={loading} // ใช้ loading state เดียว
                      className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 transition disabled:bg-gray-400"
                    >
                      {loading
                        ? (isUploading ? "กำลังอัปโหลดสลิป..." : "กำลังบันทึก...")
                        : "บันทึกการชำระเงิน"}
                    </button>
                  </div>
                )}
              {/* --- ✅ END: MODIFIED (ส่วนการจ่ายเงิน) --- */}

            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BillPaymentPage;