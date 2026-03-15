import StockTable from "../components/StockTable";
import { useNavigate } from "react-router-dom";

function Dashboard() {

    const navigate = useNavigate();

    const handleLogout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("userId");

        navigate("/login");

    };

    return (

        <div>

            <h1>Trading Dashboard</h1>

            <button onClick={() => navigate("/portfolio")}>
                Portfolio
            </button>

            <button onClick={handleLogout}>
                Logout
            </button>

            <StockTable />

        </div>

    );

}

export default Dashboard;