import { useEffect, useState } from "react";
import { connectMarketSocket, subscribeTrades } from "../websocket/websocketService";

function LiveTradeFeed() {

  const [trades, setTrades] = useState([]);

  useEffect(() => {

    connectMarketSocket(() => {});

    subscribeTrades(handleNewTrade);

  }, []);

  const handleNewTrade = (trade) => {

    setTrades((prev) => [trade, ...prev.slice(0, 9)]);

  };

  return (

    <div>

      <h2>Live Trades</h2>

      <table border="1">

        <thead>
          <tr>
            <th>Symbol</th>
            <th>Type</th>
            <th>Price</th>
            <th>Qty</th>
          </tr>
        </thead>

        <tbody>

          {trades.map((trade, index) => (

            <tr key={index}>
              <td>{trade.symbol}</td>
              <td>{trade.tradeType}</td>
              <td>{trade.price}</td>
              <td>{trade.quantity}</td>
            </tr>

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default LiveTradeFeed;