import { useState } from "react";
import { buyStock, sellStock } from "../services/tradeService";

function TradeForm({ symbol }) {

  const [quantity, setQuantity] = useState("");

  const handleBuy = async () => {

    try {

      await buyStock(symbol, quantity);

      alert("Stock bought successfully");

      setQuantity("");

    } catch (error) {

      console.error("Buy failed", error);

      const message =
        error.response?.data || "Buy failed";

      alert(message);

    }

  };

  const handleSell = async () => {

    try {

      await sellStock(symbol, quantity);

      alert("Stock sold successfully");

      setQuantity("");

    } catch (error) {

      console.error("Sell failed", error);

      const message =
        error.response?.data || "Sell failed";

      alert(message);

    }

  };

  return (

    <div>

      <input
        type="number"
        placeholder="Qty"
        value={quantity}
        onChange={(e) => setQuantity(e.target.value)}
        style={{ width: "60px" }}
      />

      <button onClick={handleBuy}>
        Buy
      </button>

      <button onClick={handleSell}>
        Sell
      </button>

    </div>

  );

}

export default TradeForm;