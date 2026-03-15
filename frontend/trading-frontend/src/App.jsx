import { Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import Portfolio from "./pages/Portfolio";
import TradeHistory from "./pages/TradeHistory";
import UserOrders from "./pages/UserOrders";

function App() {

  return (

    <Routes>

      <Route path="/login" element={<Login />} />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />

      <Route
        path="/portfolio"
        element={
          <ProtectedRoute>
            <Portfolio />
          </ProtectedRoute>
        }
      />

      <Route
        path="/history"
        element={
          <ProtectedRoute>
            <TradeHistory />
          </ProtectedRoute>
        }
      />
      <Route
        path="/orders"
        element={
          <ProtectedRoute>
            <UserOrders />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Login />} />

    </Routes>

  );

}

export default App;