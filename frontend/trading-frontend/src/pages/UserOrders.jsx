import { useEffect, useState } from "react";
import { getUserOrders, cancelOrder } from "../services/orderService";

function UserOrders() {

  const [orders, setOrders] = useState([]);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {

    try {

      const data = await getUserOrders();

      setOrders(data);

    } catch (error) {

      console.error("Error fetching user orders", error);

    }

  };

  const handleCancel = async (orderId) => {

    try {

      await cancelOrder(orderId);

      alert("Order cancelled");

      fetchOrders();

    } catch (error) {

      const message = error.response?.data || "Cancel failed";

      alert(message);

    }

  };

  return (

    <div>

      <h1>My Orders</h1>

      <table border="1">

        <thead>
          <tr>
            <th>Symbol</th>
            <th>Type</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>

          {orders.map((order) => (

            <tr key={order.orderId}>

              <td>{order.symbol}</td>
              <td>{order.tradeType}</td>
              <td>{order.price}</td>
              <td>{order.quantity}</td>
              <td>{order.orderStatus}</td>

              <td>

                {order.orderStatus === "OPEN" && (

                  <button
                    onClick={() => handleCancel(order.orderId)}
                  >
                    Cancel
                  </button>

                )}

              </td>

            </tr>

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default UserOrders;