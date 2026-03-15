import { useEffect, useState } from "react";
import { getOrderBook } from "../services/orderService";
import { connectMarketSocket, subscribeOrderBook } from "../websocket/websocketService";

function OrderBook({ symbol }) {

  const [buyOrders, setBuyOrders] = useState([]);
  const [sellOrders, setSellOrders] = useState([]);

  useEffect(() => {

    fetchOrderBook();

    connectMarketSocket(() => {});

    subscribeOrderBook(symbol, handleOrderUpdate);

  }, [symbol]);

  const fetchOrderBook = async () => {

    try {

      const data = await getOrderBook(symbol);
      console.log("OrderBook response:", data);

      setBuyOrders(data.buyOrders);
      setSellOrders(data.sellOrders);

    } catch (error) {

      console.error("Error fetching order book", error);

    }

  };

  const handleOrderUpdate = (update) => {

    setBuyOrders(update.buyOrders);
    setSellOrders(update.sellOrders);

  };

  return (

    <div>

      <h2>Order Book ({symbol})</h2>

      <div style={{ display: "flex", gap: "40px" }}>

        <div>

          <h3>Buy Orders</h3>

          <table border="1">

            <thead>
              <tr>
                <th>Price</th>
                <th>Quantity</th>
              </tr>
            </thead>

            <tbody>

              {buyOrders.map((order, index) => (

                <tr key={index}>
                  <td>{order.price}</td>
                  <td>{order.quantity}</td>
                </tr>

              ))}

            </tbody>

          </table>

        </div>

        <div>

          <h3>Sell Orders</h3>

          <table border="1">

            <thead>
              <tr>
                <th>Price</th>
                <th>Quantity</th>
              </tr>
            </thead>

            <tbody>

              {sellOrders.map((order, index) => (

                <tr key={index}>
                  <td>{order.price}</td>
                  <td>{order.quantity}</td>
                </tr>

              ))}

            </tbody>

          </table>

        </div>

      </div>

    </div>

  );

}

export default OrderBook;