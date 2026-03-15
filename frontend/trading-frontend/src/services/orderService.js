import api from "../api/axios";

export const getOrderBook = async (symbol) => {

  const response = await api.get(`/orders/book/${symbol}`);

  return response.data;

};

export const getUserOrders = async () => {

  const response = await api.get("/orders/user");

  return response.data;

};

export const cancelOrder = async (orderId) => {

  const response = await api.delete(`/orders/${orderId}`);

  return response.data;

};


export const placeBuyOrder = async (symbol, quantity, price) => {

  const response = await api.post("/orders/buy", {
    symbol: symbol,
    orderCategory: "MARKET",
    price: price,
    quantity: quantity
  });

  return response.data;

};

export const placeSellOrder = async (symbol, quantity, price) => {

  const response = await api.post("/orders/sell", {
    symbol: symbol,
    orderCategory: "MARKET",
    price: price,
    quantity: quantity
  });

  return response.data;

};