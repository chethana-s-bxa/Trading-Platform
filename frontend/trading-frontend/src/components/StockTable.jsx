import { useEffect, useState } from "react";
import { getStocks } from "../services/stocksService";
import { connectMarketSocket } from "../websocket/websocketService";

function StockTable() {

  const [stocks, setStocks] = useState([]);

  useEffect(() => {

    fetchStocks();

    connectMarketSocket(handlePriceUpdate);

  }, []);

  const fetchStocks = async () => {

    try {

      const data = await getStocks();
      setStocks(data);

    } catch (error) {

      console.error("Error fetching stocks", error);

    }

  };

  const handlePriceUpdate = (update) => {

    setStocks((prevStocks) =>
      prevStocks.map((stock) =>
        stock.symbol === update.symbol
          ? { ...stock, price: update.price }
          : stock
      )
    );

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