import { useEffect, useState } from "react";
import { getStocks } from "../services/stocksService";

function StockTable() {

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

      <h2>Market Stocks</h2>

      <table border="1">

        <thead>
          <tr>
            <th>Symbol</th>
            <th>Company</th>
            <th>Price</th>
          </tr>
        </thead>

        <tbody>

          {stocks.map((stock) => (

            <tr key={stock.symbol}>
              <td>{stock.symbol}</td>
              <td>{stock.companyName}</td>
              <td>{stock.price}</td>
            </tr>

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default StockTable;