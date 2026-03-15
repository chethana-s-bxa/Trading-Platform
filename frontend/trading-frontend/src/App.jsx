import { useEffect, useState } from "react";
import { getStocks } from "./services/stocksService";

function App() {

  const [stocks, setStocks] = useState([]);

  useEffect(() => {
    fetchStocks();
  }, []);

  const fetchStocks = async () => {
    try {
      const data = await getStocks();
      setStocks(data);
    } catch (error) {
      console.error("Error fetching stocks", error);
    }
  };

  return (
    <div>
      <h1>Trading Platform</h1>

      <h2>Stock List</h2>

      <ul>
        {stocks.map((stock) => (
          <li key={stock.symbol}>
            {stock.symbol} - {stock.price}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default App;