import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import apiClient from "../api/api.js";

// --- ส่วนของ PDF ---
import {
  PDFViewer,
  PDFDownloadLink,
  Document,
  Page,
  Text,
  View,
  StyleSheet,
  Font,
} from "@react-pdf/renderer";

// --- ส่วนของ Add Member ---
// ❗️ ตรวจสอบ Path ให้ถูกต้อง
import AddMemberModal from "./AddMemberModal"; // ✅ แก้ไข Path เป็นอันนี้

// --- [แก้ไขแล้ว] ลงทะเบียนฟอนต์ภาษาไทย (Sarabun) ---
Font.register({
  family: "Sarabun",
  fonts: [
    {
      src: "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/sarabun/Sarabun-Regular.ttf", // ✅ แก้ไข Path ที่ถูกต้องแล้ว
    },
    {
      src: "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/sarabun/Sarabun-Bold.ttf", // ✅ แก้ไข Path ที่ถูกต้องแล้ว
      fontWeight: "bold",
    },
  ],
});

// --- [PDF] Stylesheet ---
const styles = StyleSheet.create({
  page: {
    fontFamily: "Sarabun",
    fontSize: 10,
    padding: 30,
    backgroundColor: "#FFFFFF",
    color: "#333",
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
    marginBottom: 20,
    paddingBottom: 10,
    borderBottomWidth: 1,
    borderBottomColor: "#EEE",
  },
  title: {
    fontSize: 20,
    fontWeight: "bold",
    color: "#111",
  },
  memberCount: {
    fontSize: 12,
    color: "#666",
    marginTop: 4,
  },
  summaryContainer: {
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 25,
  },
  summaryBox: {
    flex: 1,
    borderWidth: 1,
    borderColor: "#E5E7EB",
    borderRadius: 8,
    padding: 12,
    marginHorizontal: 4,
    backgroundColor: "#F9FAFB",
  },
  summaryLabel: {
    fontSize: 9,
    color: "#6B7280",
    textTransform: "uppercase",
  },
  summaryValue: {
    fontSize: 16,
    fontWeight: "bold",
    color: "#1F2937",
    marginTop: 4,
  },
  breakdownTitle: {
    fontSize: 14,
    fontWeight: "bold",
    color: "#111",
    marginBottom: 10,
  },
  billItem: {
    borderWidth: 1,
    borderColor: "#E5E7EB",
    borderRadius: 8,
    marginBottom: 15,
    overflow: "hidden",
  },
  billHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    backgroundColor: "#F9FAFB",
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderBottomWidth: 1,
    borderBottomColor: "#E5E7EB",
  },
  billTitle: {
    fontSize: 12,
    fontWeight: "bold",
  },
  billAmount: {
    fontSize: 12,
    fontWeight: "bold",
  },
  billPaidBy: {
    fontSize: 9,
    color: "#6B7280",
    paddingHorizontal: 12,
    paddingTop: 10,
    marginBottom: 5,
  },
  participantRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderTopWidth: 1,
    borderTopColor: "#F3F4F6",
  },
  participantName: {
    fontSize: 10,
  },
  participantAmount: {
    fontSize: 10,
    fontWeight: "bold",
  },
  positiveAmount: {
    color: "#10B981", // Green
  },
  negativeAmount: {
    color: "#EF4444", // Red
  },
});

// --- [PDF] Component สำหรับสร้างเอกสาร PDF ---
const BillPDF = ({
  groupName,
  memberCount,
  summary,
  bills,
  currentUserId,
  formatCurrency,
  formatDate,
  totalPaidByYou,
  averagePerPerson,
}) => (
  <Document>
    <Page size="A4" style={styles.page}>
      {/* 1. Header: Title & Member Count */}
      <View style={styles.header}>
        <View>
          <Text style={styles.title}>{groupName}</Text>
          <Text style={styles.memberCount}>{memberCount} Members</Text>
        </View>
      </View>

      {/* 2. Summary Boxes */}
      <View style={styles.summaryContainer}>
        <View style={styles.summaryBox}>
          <Text style={styles.summaryLabel}>ยอดรวมของกลุ่ม</Text>
          <Text style={styles.summaryValue}>
            {formatCurrency(summary.totalAmount)}
          </Text>
        </View>
      </View>

      {/* 3. Expense Breakdown */}
      <Text style={styles.breakdownTitle}>Expense Breakdown</Text>
      {bills.map((bill) => {
        const payerName =
          bill.paidByUser.userId === currentUserId
            ? "You"
            : bill.paidByUser.fullName;

        return (
          <View key={bill.billId} style={styles.billItem}>
            {/* Bill Header (Title, Amount) */}
            <View style={styles.billHeader}>
              <Text style={styles.billTitle}>{bill.title}</Text>
              {/* ✅ MODIFIED: เปลี่ยนเป็น bill.amountInThb */}
              <Text style={styles.billAmount}>{formatCurrency(bill.amountInThb)}</Text>
            </View>

            {/* Paid by */}
            <Text style={styles.billPaidBy}>
              Paid by: {payerName} on {formatDate(bill.billDate)}
            </Text>

            {/* --- รายการคนที่เป็นหนี้คนจ่าย --- */}
            {bill.participants
              .filter((p) => p.user.userId !== bill.paidByUser.userId) 
              .map((p, index) => {
                const debtorName =
                  p.user.userId === currentUserId ? "You" : p.user.fullName;
                const description = `${debtorName} owes ${payerName}`;

                return (
                  <View key={`debt-${index}`} style={styles.participantRow}>
                    <Text style={styles.participantName}>{description}</Text>
                    <Text
                      style={[styles.participantAmount, styles.negativeAmount]}
                    >
                      - {formatCurrency(p.splitAmount)}
                    </Text>
                  </View>
                );
              })}

            {/* --- รายการที่คนอื่นเป็นหนี้เรา (ถ้าเราเป็นคนจ่าย) --- */}
            {bill.paidByUser.userId === currentUserId &&
              bill.participants
                .filter((p) => p.user.userId !== currentUserId) 
                .map((p, index) => {
                  const description = `${p.user.fullName} owes You`;
                  return (
                    <View key={`credit-${index}`} style={styles.participantRow}>
                      <Text style={styles.participantName}>{description}</Text>
                      <Text
                        style={[styles.participantAmount, styles.positiveAmount]}
                      >
                        + {formatCurrency(p.splitAmount)}
                      </Text>
                    </View>
                  );
                })}
          </View>
        );
      })}
    </Page>
  </Document>
);

//
// --- ส่วน Component Bill หลัก ---
//
const Bill = () => {
  const { groupId } = useParams();
  const navigate = useNavigate();
  const [bills, setBills] = useState([]);
  const [smartPlan, setSmartPlan] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [summary, setSummary] = useState({ totalAmount: 0 });

  // State สำหรับ Modal PDF
  const [showPdfPreview, setShowPdfPreview] = useState(false);

  // ⭐ [เพิ่มใหม่] State สำหรับ Modal เพิ่มสมาชิก
  const [showAddMemberModal, setShowAddMemberModal] = useState(false);
  
  // ⭐ [เพิ่มใหม่] State สำหรับการ "Refetch" ข้อมูล
  const [refetchKey, setRefetchKey] = useState(0); 

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

  // โหลดข้อมูลบิล + Smart Settlement
  useEffect(() => {
    const fetchBills = async () => {
      try {
        setLoading(true);
        setError(null);

        const billRes = await apiClient.get(`/bills/group/${groupId}`);
        const { groupMembers, bills } = billRes.data;

        if (!Array.isArray(bills)) {
          throw new Error("ข้อมูล bills ไม่ถูกต้องจาก backend");
        }
        setBills(bills);

        // ❗️ เก็บ groupMembers ไว้ใน localStorage เพื่อให้ AddMemberModal อ่านได้
        if (groupMembers) {
          localStorage.setItem("groupMembers", JSON.stringify(groupMembers));
        }

        // ✅ MODIFIED: เปลี่ยนเป็น bill.amountInThb
        const totalAmount = bills.reduce(
          (sum, bill) => sum + (bill.amountInThb || 0),
          0
        );
        setSummary({ totalAmount });

        // โหลด Smart Settlement
        try {
          const settleRes = await apiClient.get(
            `/groups/${groupId}/settle`
          );
          console.log("💡 Smart Settlement:", settleRes.data);
          setSmartPlan(settleRes.data || []);
        } catch (e) {
          console.error("❌ โหลด Smart Settlement ไม่สำเร็จ:", e);
          setSmartPlan([]);
        }
      } catch (err) {
        console.error("❌ โหลดข้อมูลล้มเหลว:", err);
        setError("ไม่สามารถโหลดข้อมูลบิลได้");
      } finally {
        setLoading(false);
      }
    };

    fetchBills();
  }, [groupId, refetchKey]); // ⭐ [แก้ไข] เพิ่ม refetchKey เป็น dependency

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

  // ⭐ [เพิ่มใหม่] Function ที่จะส่งให้ Modal (Callback)
  const handleMembersAdded = (newlyAddedMembers) => {
    console.log("Successfully added members:", newlyAddedMembers);
    setShowAddMemberModal(false); 
    
    // ❗️ สั่งให้โหลดข้อมูลทั้งหมดในหน้า Bill.jsx ใหม่
    setRefetchKey(prevKey => prevKey + 1); 
  };

  const groupName = bills[0]?.group?.groupName || "กลุ่มของฉัน";
  const memberCount = new Set(
    bills.flatMap((b) => [
      b.paidByUser?.userId,
      ...(b.participants || []).map((p) => p.user.userId),
    ])
  ).size;

  // --- [PDF] คำนวณข้อมูลสำหรับส่งให้ PDF ---
  const totalPaidByYou = bills
    .filter((b) => b.paidByUser?.userId === currentUserId)
    .reduce((sum, b) => sum + (b.amountInThb || 0), 0); // ✅ MODIFIED: เปลี่ยนเป็น amountInThb (เพื่อให้สอดคล้อง)

  const averagePerPerson =
    memberCount > 0 ? summary.totalAmount / memberCount : 0;

  const pdfDataProps = {
    groupName,
    memberCount,
    summary,
    bills,
    currentUserId,
    formatCurrency,
    formatDate,
    totalPaidByYou,
    averagePerPerson,
  };
  // ---

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

          {/* ปุ่มเพิ่มสมาชิก + ปุ่ม PDF */}
          <div className="ml-auto flex items-center gap-3">
            
            {/* ⭐ [แก้ไข] ปุ่มเพิ่มสมาชิก */}
            <button
              onClick={() => setShowAddMemberModal(true)} // <-- แก้ไขตรงนี้
              className="p-2 rounded-full hover:bg-gray-100"
            >
              <span className="material-icons text-gray-700">group_add</span>
            </button>

            {/* [PDF] ปุ่ม PDF */}
            <button
              onClick={() => setShowPdfPreview(true)} 
              className="p-2 rounded-full hover:bg-gray-100"
            >
              <span className="material-icons text-gray-700">
                picture_as_pdf
              </span>
            </button>
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
              {/* นี่จะอัปเดตอัตโนมัติเพราะ summary.totalAmount ถูกคำนวณใหม่แล้ว */}
              {formatCurrency(summary.totalAmount)}
            </p>
          </div>
        </div>

        {/* Smart Settlement Box */}
        {smartPlan.length > 0 && (
          <div className="bg-blue-50 border border-blue-200 rounded-xl p-5 mb-6">
            <h2 className="text-blue-700 font-semibold text-base mb-2">
              ชำระหนี้อัจฉริยะ (Smart Settlement)
            </h2>
            <p className="text-sm text-blue-600 mb-4">
              ยอดรวมขึ้นอยู่กับการชำระหนี้ด้วยวิธีนี้เท่านั้น
            </p>
            {smartPlan.map((item, index) => (
              <div
                key={index}
                className="flex items-center bg-white p-4 rounded-lg shadow mb-3"
              >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                  <img
                    src={item.fromUser.profilePictureUrl}
                    className="w-10 h-10 rounded-full object-cover flex-shrink-0"
                    alt={item.fromUser.fullName}
                  />
                  <span className="text-gray-800 font-medium truncate">
                    {item.fromUser.fullName}
                  </span>
                </div>
                <p className="text-blue-700 font-bold text-base sm:text-lg whitespace-nowrap px-4">
                  → {formatCurrency(item.amount)}
                </p>
                <div className="flex items-center gap-3 flex-1 min-w-0 justify-end">
                  <span className="text-gray-800 font-medium truncate">
                    {item.toUser.fullName}
                  </span>
                  <img
                    src={item.toUser.profilePictureUrl}
                    className="w-10 h-10 rounded-full object-cover flex-shrink-0"
                    alt={item.toUser.fullName}
                  />
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Add Expense Button */}
        <div className="flex justify-end px-6 mt-4">
          <button
            onClick={() => navigate(`/addexpense/${groupId}`)}
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
              onClick={() =>
                navigate(`/bill/${groupId}/payment/${bill.billId}`)
              }
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
                  {/* ✅ MODIFIED: เปลี่ยนเป็น bill.amountInThb */}
                  <p className="font-bold text-lg sm:text-xl text-gray-900">
                    {formatCurrency(bill.amountInThb)}
                  </p>
                  <p className="text-xs text-gray-400">
                    {formatDate(bill.billDate)}
                  </p>
                </div>
              </div>
              <div className="mt-4 pt-3 border-t border-gray-200 space-y-2 text-sm">
                {bill.participants?.map((p, index) => (
                  <div key={index} className="flex justify-between items-center">
                    <div className="flex items-center gap-2">
                      <img
                        src={
                          p.user?.profilePictureUrl ||
                          "https://via.placeholder.com/150"
                        }
                        className="w-7 h-7 rounded-full object-cover"
                      />
                      <p className="text-gray-600">
                        {p.user?.fullName} เป็นหนี้{" "}
                        {bill.paidByUser?.userId === currentUserId
                          ? "คุณ"
                          : bill.paidByUser?.fullName}
                      </p>
                    </div>
                    <p
                      className={`font-semibold ${
                        p.status === "paid"
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

      {/* --- [PDF] PDF Preview Modal --- */}
      {showPdfPreview && (
        <div className="fixed inset-0 bg-black bg-opacity-75 z-50 flex justify-center items-center p-4">
          <div className="bg-white rounded-lg shadow-2xl w-full max-w-4xl h-[90vh] flex flex-col">
            
            {/* Modal Header */}
            <div className="flex justify-between items-center p-4 border-b">
              <h3 className="text-lg font-bold font-sarabun text-gray-800">
                ตัวอย่างใบเสร็จ (PDF)
              </h3>
              <button
                onClick={() => setShowPdfPreview(false)}
                className="text-gray-400 hover:text-gray-800 text-3xl font-light"
              >
                &times;
              </button>
            </div>

            {/* PDF Viewer */}
            <div className="flex-1 overflow-hidden">
              <PDFViewer width="100%" height="100%" className="border-0">
                <BillPDF {...pdfDataProps} />
              </PDFViewer>
            </div>

            {/* Modal Footer (Download Button) */}
            <div className="p-4 border-t bg-gray-50 flex justify-end">
              <PDFDownloadLink
                document={<BillPDF {...pdfDataProps} />}
                fileName={`${groupName.replace(/\s+/g, "_") || "bill"}.pdf`}
                className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded font-sarabun transition duration-150"
              >
                {({ blob, url, loading, error }) =>
                  loading ? "กำลังสร้างเอกสาร..." : "ดาวน์โหลด PDF"
                }
              </PDFDownloadLink>
            </div>

          </div>
        </div>
      )}

      {/* --- ⭐ [เพิ่มใหม่] Add Member Modal --- */}
      {/* เราใช้ {groupId && (...)} เพื่อให้แน่ใจว่า groupId พร้อมใช้งานก่อน Render */}
      {groupId && (
        <AddMemberModal
          isOpen={showAddMemberModal}
          onClose={() => setShowAddMemberModal(false)}
          groupId={groupId}
          currentUserId={currentUserId}
          onMembersAdded={handleMembersAdded}
        />
      )}
    </div>
  );
};

export default Bill;