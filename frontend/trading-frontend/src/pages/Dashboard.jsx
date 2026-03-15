import { useState } from "react";
import { useNavigate } from "react-router-dom";
import StockTable from "../components/StockTable";
import OrderBook from "../components/OrderBook";

function Dashboard() {

  const navigate = useNavigate();

  const [selectedStock, setSelectedStock] = useState(null);

  const handleLogout = () => {

    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("userId");

    navigate("/login");
  };

  return (

    <div>

      <h1>Trading Dashboard</h1>

      <button onClick={handleLogout}>Logout</button>

      <StockTable setSelectedStock={setSelectedStock} />

      {selectedStock && (
        <OrderBook symbol={selectedStock} />
      )}

    </div>

  );

}

export default Dashboard;