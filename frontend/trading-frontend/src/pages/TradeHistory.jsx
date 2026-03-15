import { useEffect, useState } from "react";
import { getTradeHistory } from "../services/tradeHistoryService";

function TradeHistory() {

  const [trades, setTrades] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    fetchTrades(page);
  }, [page]);

  const fetchTrades = async (pageNumber) => {

    try {

      const history = await getTradeHistory(pageNumber, 5);

      setTrades(history.content);
      setTotalPages(history.totalPages);

    } catch (error) {

      console.error("Error fetching trade history", error);

    }

  };

  return (

    <div>

      <h1>Trade History</h1>

      <table border="1">

        <thead>

          <tr>
            <th>Symbol</th>
            <th>Type</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Total</th>
            <th>Time</th>
          </tr>

        </thead>

        <tbody>

          {trades.map((trade, index) => (

            <tr key={index}>

              <td>{trade.symbol}</td>
              <td>{trade.tradeType}</td>
              <td>{trade.price}</td>
              <td>{trade.quantity}</td>
              <td>{trade.totalAmount}</td>
              <td>{trade.timestamp}</td>

            </tr>

          ))}

        </tbody>

      </table>

      <div style={{ marginTop: "20px" }}>

        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
        >
          Previous
        </button>

        <span style={{ margin: "0 10px" }}>
          Page {page + 1} of {totalPages}
        </span>

        <button
          disabled={page + 1 === totalPages}
          onClick={() => setPage(page + 1)}
        >
          Next
        </button>

      </div>

    </div>

  );

}

export default TradeHistory;