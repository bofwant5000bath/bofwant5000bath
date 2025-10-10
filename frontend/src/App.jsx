// App.jsx
import { HashRouter, Routes, Route, Navigate } from "react-router-dom";
import Register from "./pages/Register.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import CreateGroup from "./pages/CreateGroup.jsx";
import Bill from "./pages/Bill.jsx";
import AddExpenseUnified from "./pages/AddExpenseUnified.jsx"; // ✅ เพิ่มหน้า AddExpenseUnified
import BillPaymentPage from "./pages/BillPaymentPage.jsx"; // ✅ เพิ่มบรรทัดนี้

function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/create-group" element={<CreateGroup />} />
        <Route path="/bill/:groupId" element={<Bill />} /> {/* ✅ หน้ารายละเอียดบิล */}
        <Route path="/addexpense/:groupId" element={<AddExpenseUnified />} /> {/* ✅ หน้าเพิ่มค่าใช้จ่าย */}
        <Route
          path="/bill/:groupId/payment/:billId"
          element={<BillPaymentPage />}
        />
      </Routes>
    </HashRouter>
  );
}

export default App;
