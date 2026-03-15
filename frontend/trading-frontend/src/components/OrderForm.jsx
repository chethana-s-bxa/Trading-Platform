import { useState } from "react";
import { placeBuyOrder, placeSellOrder } from "../services/orderService";

function OrderForm({ symbol }) {

  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");

  const handleBuyOrder = async () => {

    try {

      await placeBuyOrder(symbol, quantity, price);

      alert("Buy order placed");

      setQuantity("");
      setPrice("");

    } catch (error) {

      const message = error.response?.data || "Order failed";

      alert(message);

    }

  };

  const handleSellOrder = async () => {

    try {

      await placeSellOrder(symbol, quantity, price);

      alert("Sell order placed");

      setQuantity("");
      setPrice("");

    } catch (error) {

      const message = error.response?.data || "Order failed";

      alert(message);

    }

  };

  return (

    <div>

      <h3>Place Limit Order ({symbol})</h3>

      <input
        type="number"
        placeholder="Price"
        value={price}
        onChange={(e) => setPrice(e.target.value)}
      />

      <input
        type="number"
        placeholder="Quantity"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
      />

      <button onClick={handleBuyOrder}>Buy Order</button>
      <button onClick={handleSellOrder}>Sell Order</button>

    </div>

  );

}

export default OrderForm;