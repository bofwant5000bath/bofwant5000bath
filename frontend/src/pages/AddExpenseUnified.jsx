// src/components/AddExpenseUnified.jsx
import React, { useState, useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from '../api/api.js';

// ✅ NEW: ใส่ API Key ของคุณที่นี่
// คุณต้องไปสมัครที่ https://api.imgbb.com/ เพื่อให้ได้คีย์มา
const IMGBB_API_KEY = "fd22cd3f2efd8a2f7a98628df12a7889"; 

const AddExpenseUnified = () => {
  const navigate = useNavigate();
  const { groupId } = useParams();

  // ... (State อื่นๆ เหมือนเดิม) ...
  const [members, setMembers] = useState([]);
  const [amount, setAmount] = useState("1000.00"); 
  const [description, setDescription] = useState("ค่าอาหารเย็น");
  const [payerId, setPayerId] = useState("");
  const [splitMethod, setSplitMethod] = useState("equally");
  const [loading, setLoading] = useState(false);
  const [entryAmount, setEntryAmount] = useState("1000.00"); 
  const [selectedCurrency, setSelectedCurrency] = useState("THB");
  const [currencies, setCurrencies] = useState({ THB: "Thai Baht" });
  const [exchangeRate, setExchangeRate] = useState(1);
  const baseCurrency = "THB";
  const [promptpayNumber, setPromptpayNumber] = useState(""); 
  const [isUploading, setIsUploading] = useState(false); 
  const [equalParticipants, setEqualParticipants] = useState([]);
  const [customParticipants, setCustomParticipants] = useState([]);
  const [tags, setTags] = useState([]);

  // ✅ MODIFIED: State สำหรับไฟล์
  const [receiptFile, setReceiptFile] = useState(null); // ตัวไฟล์ (File Object)
  const [receiptPreviewUrl, setReceiptPreviewUrl] = useState(null); // URL สำหรับโชว์ Preview
  const fileInputRef = useRef(null); // ✅ NEW: Ref สำหรับอ้างอิง input

  // ... (useEffect ทั้ง 3 ตัว (fetchMembers, fetchCurrencies, fetchRate) เหมือนเดิม) ...
  useEffect(() => {
    const fetchMembers = async () => {
      try {
        const res = await apiClient.get(
          `/bills/group/${groupId}`
        );
        const groupMembers = res.data.groupMembers || [];
        setMembers(groupMembers);
        setPayerId(groupMembers[0]?.userId || "");
        setEqualParticipants(
          groupMembers.map((m) => ({
            id: m.userId,
            name: m.fullName,
            isChecked: true,
          }))
        );
        setCustomParticipants(
          groupMembers.map((m) => ({
            id: m.userId,
            name: m.fullName,
            isChecked: true,
            share: "0.00",
          }))
        );
        setTags([
          {
            id: 1,
            name: "อาหาร",
            amount: "700.00",
            members: groupMembers.map((m) => ({
              userId: m.userId,
              name: m.fullName,
              isChecked: true,
              share: "0.00",
            })),
          },
        ]);
      } catch (err) {
        console.error("❌ โหลดสมาชิกกลุ่มล้มเหลว:", err);
        alert("โหลดรายชื่อสมาชิกไม่สำเร็จ กรุณาตรวจสอบ API");
      }
    };

    const fetchCurrencies = async () => {
      try {
        const res = await fetch("https://api.frankfurter.app/currencies");
        if (!res.ok) throw new Error("Network response was not ok");
        const data = await res.json();
        setCurrencies(data);
      } catch (err) {
        console.error("❌ โหลดสกุลเงินล้มเหลว:", err);
      }
    };

    fetchMembers();
    fetchCurrencies();
  }, [groupId]);

  useEffect(() => {
    const fetchRate = async () => {
      if (selectedCurrency === baseCurrency) {
        setExchangeRate(1);
        return;
      }
      try {
        const res = await fetch(
          `https://api.frankfurter.app/latest?from=${selectedCurrency}&to=${baseCurrency}`
        );
        if (!res.ok) throw new Error("Network response was not ok");
        const data = await res.json();
        setExchangeRate(data.rates[baseCurrency] || 1);
      } catch (err) {
        console.error("❌ โหลดอัตราแลกเปลี่ยนล้มเหลว:", err);
        setExchangeRate(1); // Fallback
      }
    };

    fetchRate();
  }, [selectedCurrency, baseCurrency]);

  useEffect(() => {
    const newTotalThb = parseFloat(entryAmount || 0) * exchangeRate;
    setAmount(newTotalThb.toFixed(2));
  }, [entryAmount, exchangeRate]);

  // ... (formatCurrency และ handle* อื่นๆ เหมือนเดิม) ...
  const formatCurrency = (num) =>
    new Intl.NumberFormat("th-TH", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(num || 0);

  // ... (handleEqualCheck, handleCustomCheck, handleCustomShareChange, etc. ... )
  const handleEqualCheck = (id) =>
    setEqualParticipants((prev) =>
      prev.map((p) =>
        p.id === id ? { ...p, isChecked: !p.isChecked } : p
      )
    );

  const handleCustomCheck = (id) =>
    setCustomParticipants((prev) =>
      prev.map((p) =>
        p.id === id
          ? {
              ...p,
              isChecked: !p.isChecked,
              share: !p.isChecked ? p.share : "0.00",
            }
          : p
      )
    );

  const handleCustomShareChange = (id, value) =>
    setCustomParticipants((prev) =>
      prev.map((p) =>
        p.id === id ? { ...p, share: value } : p
      )
    );

  const handleTagChange = (id, field, value) =>
    setTags((prev) =>
      prev.map((t) =>
        t.id === id ? { ...t, [field]: value } : t
      )
    );

  const handleTagMemberCheck = (tagId, memberId) =>
    setTags((prev) =>
      prev.map((t) =>
        t.id === tagId
          ? {
              ...t,
              members: t.members.map((m) =>
                m.userId === memberId
                  ? { ...m, isChecked: !m.isChecked }
                  : m
              ),
            }
          : t
      )
    );

  const handleTagMemberShareChange = (tagId, memberId, value) =>
    setTags((prev) =>
      prev.map((t) =>
        t.id === tagId
          ? {
              ...t,
              members: t.members.map((m) =>
                m.userId === memberId ? { ...m, share: value } : m
              ),
            }
          : t
      )
    );

  const addTag = () =>
    setTags((prev) => [
      ...prev,
      {
        id: Date.now(),
        name: "",
        amount: "0.00",
        members: members.map((m) => ({
          userId: m.userId,
          name: m.fullName,
          isChecked: true,
          share: "0.00",
        })),
      },
    ]);

  const getCustomTotal = () =>
    customParticipants.reduce(
      (sum, p) => sum + (p.isChecked ? parseFloat(p.share) || 0 : 0),
      0
    );

  const getTagsTotal = () =>
    tags.reduce((sum, t) => sum + (parseFloat(t.amount) || 0), 0);

  // ✅ MODIFIED: อัปเดต handleFileChange ให้สร้าง Preview
  const handleFileChange = (e) => {
    const file = e.target.files && e.target.files[0];
    if (file) {
      setReceiptFile(file);
      // สร้าง URL ชั่วคราวสำหรับแสดงผล
      const reader = new FileReader();
      reader.onloadend = () => {
        setReceiptPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  // ✅ NEW: ฟังก์ชันสำหรับลบไฟล์ (และเคลียร์ preview)
  const handleRemoveFile = (e) => {
    // ป้องกันไม่ให้ label ที่ครอบอยู่ทำงาน (ป้องกันหน้าต่างเลือกไฟล์เด้ง)
    e.preventDefault(); 
    e.stopPropagation();

    setReceiptFile(null);
    setReceiptPreviewUrl(null);
    // รีเซ็ตค่าใน input file ที่ซ่อนอยู่
    if (fileInputRef.current) {
      fileInputRef.current.value = null;
    }
  };

  // ✅✅✅ [แก้ไขแล้ว] ✅✅✅
  // เพิ่มการตรวจสอบ PromptPay
  const validateBeforeSave = () => {
    const totalAmount = parseFloat(amount);

    if (!description.trim()) {
      alert("กรุณากรอกคำอธิบาย");
      return false;
    }
    if (totalAmount <= 0) {
      alert("กรุณากรอกจำนวนเงินรวมให้ถูกต้อง (ต้องมากกว่า 0)");
      return false;
    }

    // ✅ [เพิ่ม] ตรวจสอบ PromptPay
    if (!promptpayNumber.trim()) {
      alert("กรุณากรอกเบอร์PromptPay");
      return false;
    }
    // ✅ [เพิ่ม] ตรวจสอบว่าเป็นเบอร์ 10 หลักหรือไม่
    if (!/^\d{10}$/.test(promptpayNumber)) {
        alert("กรุณากรอกเบอร์PromptPay 10 หลักให้ถูกต้อง");
        return false;
    }


    if (splitMethod === "equally") {
      const checked = equalParticipants.filter((p) => p.isChecked);
      if (checked.length === 0) {
        alert("กรุณาเลือกอย่างน้อย 1 คนในการแบ่งเท่า ๆ กัน");
        return false;
      }
    }
    if (splitMethod === "custom") {
      for (const p of customParticipants) {
        if (p.isChecked && parseFloat(p.share) <= 0) {
          alert(`กรุณากรอกจำนวนเงินของ '${p.name}' ให้มากกว่า 0`);
          return false;
        }
      }
      const totalCustom = getCustomTotal();
      if (totalCustom <= 0) {
        alert("กรุณากรอกจำนวนเงินของแต่ละคนให้ถูกต้อง");
        return false;
      }
      if (Math.abs(totalCustom - totalAmount) > 0.01) {
        alert(
          `ยอดรวมของแต่ละคน (${formatCurrency(
            totalCustom
          )}) ไม่ตรงกับยอดรวมทั้งหมด (${formatCurrency(totalAmount)})`
        );
        return false;
      }
    }
    if (splitMethod === "tags") {
      for (const tag of tags) {
        if (!tag.name.trim()) {
          alert("กรุณาตั้งชื่อแท็กทั้งหมด");
          return false;
        }
        let memberShareTotal = 0;
        for (const member of tag.members) {
          if (member.isChecked) {
            const memberShare = parseFloat(member.share) || 0;
            if (memberShare <= 0) {
              alert(
                `ในแท็ก '${tag.name}', กรุณากรอกจำนวนเงินของ '${
                  member.name
                }' ให้มากกว่า 0`
              );
              return false;
            }
            memberShareTotal += memberShare;
          }
        }
        const tagTotal = parseFloat(tag.amount) || 0;
        if (tag.members.filter(m => m.isChecked).length > 0 && Math.abs(memberShareTotal - tagTotal) > 0.01) {
          alert(
            `ยอดรวมของผู้เข้าร่วมในแท็ก '${tag.name}' (${formatCurrency(
              memberShareTotal
            )}) ไม่ตรงกับยอดรวมของแท็ก (${formatCurrency(tagTotal)})`
          );
          return false;
        }
      }
      const totalTags = getTagsTotal();
      if (totalTags <= 0) {
        alert("กรุณากรอกจำนวนเงินในแต่ละแท็กให้ถูกต้อง");
        return false;
      }
      if (Math.abs(totalTags - totalAmount) > 0.01) {
        alert(
          `ยอดรวมของแต่ละแท็ก (${formatCurrency(
            totalTags
          )}) ไม่ตรงกับยอดรวมทั้งหมด (${formatCurrency(totalAmount)})`
        );
        return false;
      }
    }
    return true;
  };

  // ✅ MODIFIED: อัปเดต handleSave
  const handleSave = async () => {
    if (!validateBeforeSave()) return;

    if (IMGBB_API_KEY === "YOUR_IMGBB_API_KEY_HERE") {
      alert("กรุณาใส่ IMGBB_API_KEY ในโค้ดก่อนครับ");
      return;
    }

    setLoading(true); // เริ่ม loading
    let finalReceiptUrl = null; // ใช้ null เป็นค่าเริ่มต้น (ตาม JSON ตัวอย่าง)

    // --- 1. อัปโหลดไฟล์ไป ImgBB (ถ้ามีไฟล์) ---
    if (receiptFile) {
      setIsUploading(true); // แสดง "กำลังอัปโหลด..."
      
      const formData = new FormData();
      formData.append('image', receiptFile);

      try {
        console.log("... กำลังอัปโหลดไป ImgBB...");
        const res = await fetch(
          `https://api.imgbb.com/1/upload?key=${IMGBB_API_KEY}`,
          {
            method: 'POST',
            body: formData,
          }
        );

        const data = await res.json();
        
        if (data.success) {
          finalReceiptUrl = data.data.url; // เอา URL ที่ได้มา
          console.log("... อัปโหลดสำเร็จ:", finalReceiptUrl);
        } else {
          // ถ้า ImgBB ส่ง error กลับมา
          throw new Error(data.error?.message || 'อัปโหลดไฟล์ล้มเหลว');
        }

      } catch (uploadErr) {
        console.error("❌ การอัปโหลดไฟล์ล้มเหลว:", uploadErr);
        alert(`อัปโหลดใบเสร็จล้มเหลว: ${uploadErr.message}`);
        setLoading(false);
        setIsUploading(false);
        return; // หยุดการทำงาน
      }
      
      setIsUploading(false); // อัปโหลดเสร็จ
    }

    // --- 2. สร้าง Payload ---
    let payload = {
      groupId: Number(groupId),
      title: description,
      description: description,
      
      // (1) amount หลัก (ใช้ค่าที่กรอก)
      amount: parseFloat(entryAmount), 
      
      paidByUserId: Number(payerId),
      splitMethod:
        splitMethod === "equally"
          ? "equal"
          : splitMethod === "custom"
          ? "unequal"
          : "by_tag",
      
      currencyCode: selectedCurrency,
      exchangeRate: exchangeRate,
      promptpayNumber: promptpayNumber, // [แก้ไข] ไม่ต้อง || null แล้ว เพราะมัน required
      receiptImageUrl: finalReceiptUrl, // ✅ ใส่ URL ที่ได้จาก ImgBB
    };

    // --- (2) Logic เพิ่ม participants / tags ---
    
    if (splitMethod === "equally") {
      payload.selectedParticipantIds = equalParticipants
        .filter((p) => p.isChecked)
        .map((p) => p.id);
    } else if (splitMethod === "custom") {
      
      // ✅✅✅ CHANGE HERE ✅✅✅
      // แปลงค่า p.share (THB) กลับเป็นสกุลเงินเดิมก่อนส่ง
      payload.participants = customParticipants
        .filter((p) => p.isChecked)
        .map((p) => ({
          userId: p.id,
          amount: parseFloat(p.share) / exchangeRate, // <-- หารด้วยอัตราแลกเปลี่ยน
        }));
        
    } else if (splitMethod === "tags") {
      
      // ✅✅✅ CHANGE HERE ✅✅✅
      // แปลงค่า tag.amount และ m.share (THB) กลับเป็นสกุลเงินเดิม
      payload.tags = tags.map((tag) => ({
        tagName: tag.name,
        tagAmount: parseFloat(tag.amount) / exchangeRate, // <-- หารด้วยอัตราแลกเปลี่ยน
        participants: tag.members
          .filter((m) => m.isChecked)
          .map((m) => ({
            userId: m.userId,
            amount: (parseFloat(m.share) || 0) / exchangeRate, // <-- หารด้วยอัตราแลกเปลี่ยน
          })),
      }));
    }

    console.log("📦 Final Payload:", payload);

    // --- 3. ส่งข้อมูลไป Backend (เหมือนเดิม) ---
    try {
      const endpoint =
        splitMethod === "tags"
          ? "/bills/create/by-tag"
          : "/bills/create";
      await apiClient.post(endpoint, payload);
      alert("✅ บันทึกค่าใช้จ่ายสำเร็จ!");
      navigate(-1);
    } catch (err) {
      console.error("❌ เกิดข้อผิดพลาด:", err);
      alert("เกิดข้อผิดพลาดจาก backend");
    } finally {
      setLoading(false);
      setIsUploading(false);
    }
  };

  // ... (renderSplitContent เหมือนเดิม 100%) ...
  // (ส่วนนี้ยังคงใช้ `amount` (ยอด THB) ในการแสดงผลและคำนวณ)
  const renderSplitContent = () => {
    // ... (โค้ดส่วนนี้ไม่เปลี่ยนแปลง) ...
    switch (splitMethod) {
      case "equally":
        const checkedCount = equalParticipants.filter((p) => p.isChecked).length;
        const perPerson =
          checkedCount > 0 ? parseFloat(amount) / checkedCount : 0;
        return (
          <div className="space-y-4">
            {equalParticipants.map((p) => (
              <div key={p.id} className="flex justify-between items-center">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={p.isChecked}
                    onChange={() => handleEqualCheck(p.id)}
                    className="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                  />
                  <span className="ml-3 text-sm text-gray-700">
                    {p.name}
                  </span>
                </div>
                <span className="text-sm text-gray-500">
                  ฿{formatCurrency(p.isChecked ? perPerson : 0)}
                </span>
              </div>
            ))}
          </div>
        );

      case "custom":
        const total = getCustomTotal();
        const remaining = parseFloat(amount) - total;
        return (
          <div className="space-y-4">
            {customParticipants.map((p) => (
              <div key={p.id} className="flex justify-between items-center">
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    checked={p.isChecked}
                    onChange={() => handleCustomCheck(p.id)}
                    className="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                  />
                  <span className="ml-3 text-sm text-gray-700">{p.name}</span>
                </div>
                <div className="relative rounded-md shadow-sm w-32">
                  <div className="pointer-events-none absolute inset-y-0 left-0 pl-3 flex items-center">
                    <span className="text-gray-500 sm:text-sm">฿</span>
                  </div>
                  <input
                    type="number"
                    value={p.share}
                    onChange={(e) =>
                      handleCustomShareChange(p.id, e.target.value)
                    }
                    disabled={!p.isChecked}
                    className="block w-full pl-7 sm:text-sm border-gray-300 rounded-md disabled:bg-gray-100"
                  />
                </div>
              </div>
            ))}
            <div className="border-t border-gray-200 pt-4 flex justify-between text-sm font-medium">
              <p>ยอดรวม:</p>
              <p>฿{formatCurrency(total)}</p>
            </div>
            <div className="flex justify-between text-sm font-medium">
              <p
                className={
                  Math.abs(remaining) > 0.01
                    ? "text-red-500"
                    : "text-gray-500"
                }
              >
                {remaining > 0 ? "เหลือที่ต้องแบ่ง:" : "เกินยอดรวมไป:"}
              </p>
              <p
                className={
                  Math.abs(remaining) > 0.01
                    ? "text-red-500"
                    : "text-gray-500"
                }
              >
                ฿{formatCurrency(remaining)}
              </p>
            </div>
          </div>
        );

      case "tags":
        const totalTags = getTagsTotal();
        const remainingTags = parseFloat(amount) - totalTags;
        return (
          <div className="space-y-6">
            {tags.map((tag, index) => (
              <div key={tag.id} className="border-t border-gray-200 pt-4">
                <div className="flex items-center gap-4">
                  <input
                    type="text"
                    value={tag.name}
                    onChange={(e) =>
                      handleTagChange(tag.id, "name", e.target.value)
                    }
                    placeholder={`แท็ก ${index + 1}`}
                    className="flex-grow shadow-sm border-gray-300 rounded-md"
                  />
                  <div className="relative rounded-md shadow-sm w-32">
                    <div className="pointer-events-none absolute inset-y-0 left-0 pl-3 flex items-center">
                      <span className="text-gray-500 sm:text-sm">฿</span>
                    </div>
                    <input
                      type="number"
                      value={tag.amount}
                      onChange={(e) =>
                        handleTagChange(tag.id, "amount", e.target.value)
                      }
                      className="block w-full pl-7 sm:text-sm border-gray-300 rounded-md"
                    />
                  </div>
                </div>

                <div className="mt-2 pl-4 space-y-2">
                  {tag.members.map((m) => (
                    <div
                      key={m.userId}
                      className="flex justify-between items-center"
                    >
                      <div className="flex items-center">
                        <input
                          type="checkbox"
                          checked={m.isChecked}
                          onChange={() =>
                            handleTagMemberCheck(tag.id, m.userId)
                          }
                          className="h-4 w-4 text-indigo-600 border-gray-300 rounded"
                        />
                        <label className="ml-3 text-sm text-gray-700">
                          {m.name}
                        </label>
                      </div>
                      <div className="relative rounded-md shadow-sm w-32">
                        <div className="pointer-events-none absolute inset-y-0 left-0 pl-3 flex items-center">
                          <span className="text-gray-500 sm:text-sm">฿</span>
                        </div>
                        <input
                          type="number"
                          value={m.share}
                          onChange={(e) =>
                            handleTagMemberShareChange(
                              tag.id,
                              m.userId,
                              e.target.value
                            )
                          }
                          disabled={!m.isChecked}
                          className="block w-full pl-7 sm:text-sm border-gray-300 rounded-md disabled:bg-gray-100"
                        />
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
            <button
              type="button"
              onClick={addTag}
              className="flex items-center space-x-2 text-indigo-600 hover:text-indigo-800"
            >
              <i className="material-icons">add_circle_outline</i>
              <span className="text-sm font-medium">เพิ่มแท็ก</span>
            </button>

            <div className="border-t border-gray-200 pt-4 flex justify-between text-sm font-medium">
              <p>ยอดรวม:</p>
              <p>฿{formatCurrency(totalTags)}</p>
            </div>
            <div className="flex justify-between text-sm font-medium">
              <p
                className={
                  Math.abs(remainingTags) > 0.01
                    ? "text-red-500"
                    : "text-gray-500"
                }
              >
                {remainingTags > 0 ? "เหลือที่ต้องแบ่ง:" : "เกินยอดรวมไป:"}
              </p>
              <p
                className={
                  Math.abs(remainingTags) > 0.01
                    ? "text-red-500"
                    : "text-gray-500"
                }
              >
                ฿{formatCurrency(remainingTags)}
              </p>
            </div>
          </div>
        );

      default:
        return null;
    }
  };


  return (
    <div className="bg-gray-100 min-h-screen font-sans">
      <header className="bg-white shadow-md p-4">
        {/* ... (Header เหมือนเดิม) ... */}
        <div className="container mx-auto flex items-center space-x-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 rounded-full hover:bg-gray-200"
          >
            <i className="material-icons text-gray-600">arrow_back</i>
          </button>
          <h1 className="text-xl font-bold text-gray-800">
            เพิ่มค่าใช้จ่ายใน "กลุ่ม"
          </h1>
        </div>
      </header>

      <main className="flex-grow p-4 container mx-auto">
        <div className="bg-white p-6 rounded-xl shadow-md">
          <div className="space-y-6">
            
            {/* ... (Amount, Currency, Description sections เหมือนเดิม) ... */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                จำนวนเงิน
              </label>
              <div className="mt-1 flex gap-2">
                <div className="relative rounded-md shadow-sm flex-grow">
                  <input
                    type="number"
                    value={entryAmount}
                    onChange={(e) => setEntryAmount(e.target.value)}
                    className="block w-full sm:text-sm border-gray-300 rounded-md"
                    placeholder="0.00"
                  />
                </div>
                <div className="relative w-40">
                  <select
                    value={selectedCurrency}
                    onChange={(e) => setSelectedCurrency(e.target.value)}
                    className="block w-full border-gray-300 rounded-md shadow-sm"
                  >
                    {Object.keys(currencies).sort().map((code) => (
                      <option key={code} value={code}>
                        {code}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {selectedCurrency !== baseCurrency && (
              <div className="bg-blue-50 text-blue-700 p-3 rounded-md text-sm">
                <p>
                  1 {selectedCurrency} = {exchangeRate.toFixed(4)} {baseCurrency}
                  <span className="text-blue-500"> (อัตราแลกเปลี่ยนตามเวลาจริง)</span>
                </p>
                <p className="font-bold mt-1">
                  ยอดรวมโดยประมาณ: {formatCurrency(amount)} {baseCurrency}
                </p>
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700">
                คำอธิบาย
              </label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="mt-1 shadow-sm block w-full sm:text-sm border-gray-300 rounded-md"
                required
              />
            </div>

            {/* ✅ START: MODIFIED UI (RECEIPT UPLOAD) */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                แนบใบเสร็จ (ไม่บังคับ)
              </label>
              
              {/* input ที่ซ่อนไว้ ถูกเรียกใช้โดย label */}
              <input 
                id="receipt-upload" 
                name="receipt-upload" 
                type="file" 
                className="sr-only" 
                onChange={handleFileChange} 
                accept="image/png, image/jpeg, image/gif" // ImgBB รับเฉพาะรูปภาพ
                ref={fileInputRef} // เชื่อม ref
              />
              
              {/* นี่คือกรอบทั้งหมดที่คลิกได้ */}
              <label
                htmlFor="receipt-upload"
                className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md cursor-pointer hover:border-indigo-400"
              >
                <div className="space-y-1 text-center">
                  
                  {/* แสดงรูป Preview หรือ ไอคอน */}
                  {receiptPreviewUrl ? (
                    <div>
                      <img 
                        src={receiptPreviewUrl} 
                        alt="รูปใบเสร็จ" 
                        className="mx-auto h-32 w-auto object-contain rounded-md" // เพิ่ม h-32
                      />
                      <p className="text-xs text-gray-600 mt-2">{receiptFile.name}</p>
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
                      <svg className="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
                        <path d="M28 8H12a4 4
 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                      <div className="flex text-sm text-gray-600 justify-center">
                        <span className="relative bg-white rounded-md font-medium text-indigo-600">
                          อัปโหลดไฟล์
                        </span>
                        <p className="pl-1">หรือลากมาวาง</p>
                      </div>
                      <p className="text-xs text-gray-500">
                        PNG, JPG, GIF (ImgBB)
                      </p>
                    </>
                  )}

                </div>
              </label>
            </div>
            {/* ✅ END: MODIFIED UI (RECEIPT UPLOAD) */}


            {/* ... (Payer) ... */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                ผู้จ่าย
              </label>
              <select
                value={payerId}
                onChange={(e) => setPayerId(e.target.value)}
                className="mt-1 block w-full border-gray-300 rounded-md"
              >
                {members.map((m) => (
                  <option key={m.userId} value={m.userId}>
                    {m.fullName}
                  </option>
                ))}
              </select>
            </div>

            {/* ✅✅✅ [แก้ไขแล้ว] ✅✅✅ */}
            <div>
              <label className="block text-sm font-medium text-gray-700">
                เบอร์PromptPay
              </label>
              <input
                type="tel"
                value={promptpayNumber}
                onChange={(e) => setPromptpayNumber(e.target.value)}
                className="mt-1 shadow-sm block w-full sm:text-sm border-gray-300 rounded-md"
                placeholder="08xxxxxxxx"
                maxLength={10}
                required // เพิ่ม attribute นี้
              />
            </div>
            {/* ✅ END: MODIFIED UI (PROMPTPAY) */}


            {/* --- SECTION: SPLIT METHOD (เหมือนเดิม) --- */}
            <div>
              <h3 className="text-lg font-medium text-gray-900">
                ผู้เข้าร่วมและวิธีแบ่ง
              </h3>
              {/* ... (fieldset เหมือนเดิม) ... */}
              <fieldset className="mt-4 flex space-x-6">
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="split"
                    value="equally"
                    checked={splitMethod === "equally"}
                    onChange={() => setSplitMethod("equally")}
                    className="h-4 w-4 text-indigo-600 border-gray-300"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    แบ่งเท่า ๆ กัน
                  </span>
                </label>
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="split"
                    value="custom"
                    checked={splitMethod === "custom"}
                    onChange={() => setSplitMethod("custom")}
                    className="h-4 w-4 text-indigo-600 border-gray-300"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    กำหนดจำนวนเงินเอง
                  </span>
                </label>
                <label className="flex items-center">
                  <input
                    type="radio"
                    name="split"
                    value="tags"
                    checked={splitMethod === "tags"}
                    onChange={() => setSplitMethod("tags")}
                    className="h-4 w-4 text-indigo-600 border-gray-300"
                  />
                  <span className="ml-2 text-sm text-gray-700">
                    แบ่งตามแท็ก
                  </span>
                </label>
              </fieldset>
            </div>

            <div className="border-t border-gray-200 pt-4">
              {renderSplitContent()}
            </div>
          </div>
        </div>

        {/* ... (Footer buttons เหมือนเดิม) ... */}
        <div className="mt-8 flex justify-end space-x-3">
          <button
            onClick={() => navigate(-1)}
            className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            ยกเลิก
          </button>
          <button
            onClick={handleSave}
            disabled={loading}
            className="inline-flex justify-center py-2 px-4 border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 disabled:bg-gray-400"
          >
            {loading 
              ? (isUploading ? "กำลังอัปโหลด..." : "กำลังบันทึก...") 
              : "บันทึกค่าใช้จ่าย"}
          </button>
        </div>
      </main>
    </div>
  );
};

export default AddExpenseUnified;