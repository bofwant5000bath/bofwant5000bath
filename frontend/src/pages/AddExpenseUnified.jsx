// src/components/AddExpenseUnified.jsx
import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from '../api/api.js';

const AddExpenseUnified = () => {
  const navigate = useNavigate();
  const { groupId } = useParams();

  const [members, setMembers] = useState([]);
  const [amount, setAmount] = useState("1000.00");
  const [description, setDescription] = useState("ค่าอาหารเย็น");
  const [payerId, setPayerId] = useState("");
  const [splitMethod, setSplitMethod] = useState("equally");
  const [loading, setLoading] = useState(false);

  const [equalParticipants, setEqualParticipants] = useState([]);
  const [customParticipants, setCustomParticipants] = useState([]);
  const [tags, setTags] = useState([]);

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
    fetchMembers();
  }, [groupId]);

  const formatCurrency = (num) =>
    new Intl.NumberFormat("th-TH", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(num || 0);

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

  // ✅ Validation ป้องกันยอดรวมเป็น 0.00
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

        // ✅ START: เพิ่มโค้ดส่วนตรวจสอบยอดรวมในแต่ละแท็ก
        const tagTotal = parseFloat(tag.amount) || 0;
        if (tag.members.filter(m => m.isChecked).length > 0 && Math.abs(memberShareTotal - tagTotal) > 0.01) {
          alert(
            `ยอดรวมของผู้เข้าร่วมในแท็ก '${tag.name}' (${formatCurrency(
              memberShareTotal
            )}) ไม่ตรงกับยอดรวมของแท็ก (${formatCurrency(tagTotal)})`
          );
          return false;
        }
        // ✅ END: เพิ่มโค้ดส่วนตรวจสอบ
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

  const handleSave = async () => {
    if (!validateBeforeSave()) return;
    let payload = {
      groupId: Number(groupId),
      title: description,
      description: description,
      amount: parseFloat(amount),
      paidByUserId: Number(payerId),
      splitMethod:
        splitMethod === "equally"
          ? "equal"
          : splitMethod === "custom"
          ? "unequal"
          : "by_tag",
    };

    if (splitMethod === "equally") {
      payload.selectedParticipantIds = equalParticipants
        .filter((p) => p.isChecked)
        .map((p) => p.id);
    } else if (splitMethod === "custom") {
      payload.participants = customParticipants
        .filter((p) => p.isChecked)
        .map((p) => ({
          userId: p.id,
          amount: parseFloat(p.share),
        }));
    } else if (splitMethod === "tags") {
      payload.tags = tags.map((tag) => ({
        tagName: tag.name,
        tagAmount: parseFloat(tag.amount),
        participants: tag.members
          .filter((m) => m.isChecked)
          .map((m) => ({
            userId: m.userId,
            amount: parseFloat(m.share) || 0,
          })),
      }));
    }

    console.log("📦 Payload:", payload);

    try {
      setLoading(true);
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
    }
  };

  // ✅ UI เดิมทั้งหมด (ไม่แตะ layout)
  const renderSplitContent = () => {
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
        <div className="container mx-auto flex items-center space-x-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 rounded-full hover:bg-gray-200"
          >
            <i className="material-icons text-gray-600">arrow_back</i>
          </button>
          <h1 className="text-xl font-bold text-gray-800">
            เพิ่มค่าใช้จ่ายใน "ทริปเที่ยวทะเล"
          </h1>
        </div>
      </header>

      <main className="flex-grow p-4 container mx-auto">
        <div className="bg-white p-6 rounded-xl shadow-md">
          <div className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700">
                จำนวนเงิน
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="pointer-events-none absolute inset-y-0 left-0 pl-3 flex items-center">
                  <span className="text-gray-500 sm:text-sm">฿</span>
                </div>
                <input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="block w-full pl-7 sm:text-sm border-gray-300 rounded-md"
                />
              </div>
            </div>

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

            <div>
              <h3 className="text-lg font-medium text-gray-900">
                ผู้เข้าร่วมและวิธีแบ่ง
              </h3>
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
            className="inline-flex justify-center py-2 px-4 border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
          >
            {loading ? "กำลังบันทึก..." : "บันทึกค่าใช้จ่าย"}
          </button>
        </div>
      </main>
    </div>
  );
};

export default AddExpenseUnified;
