// import { useEffect, useState } from "react";
// import { getStocks } from "./services/stocksService";

// function App() {

//   const [stocks, setStocks] = useState([]);

//   useEffect(() => {
//     fetchStocks();
//   }, []);

//   const fetchStocks = async () => {
//     try {
//       const data = await getStocks();
//       setStocks(data);
//     } catch (error) {
//       console.error("Error fetching stocks", error);
//     }
//   };

//   return (
//     <div>
//       <h1>Trading Platform</h1>

//       <h2>Stock List</h2>

//       <ul>
//         {stocks.map((stock) => (
//           <li key={stock.symbol}>
//             {stock.symbol} - {stock.price}
//           </li>
//         ))}
//       </ul>
//     </div>
//   );
// }

// export default App;


// import Login from "./pages/Login";

// function App() {

//   return (
//     <div>
//       <Login />
//     </div>
//   );

// }

// export default App;

import { Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";

function App() {

  return (

    <Routes>

      <Route path="/login" element={<Login />} />

      <Route path="/dashboard" element={<Dashboard />} />

      <Route path="*" element={<Login />} />

    </Routes>

  );

}

export default App;