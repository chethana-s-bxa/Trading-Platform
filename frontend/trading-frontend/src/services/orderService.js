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