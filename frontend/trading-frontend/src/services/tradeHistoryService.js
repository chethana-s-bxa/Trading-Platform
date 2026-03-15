import api from "../api/axios";

export const getTradeHistory = async (page = 0, size = 5) => {

  const response = await api.get(`/trade/history?page=${page}&size=${size}`);

  return response.data;

};

export const getTradePnL = async () => {

  const response = await api.get("/trades/pnl");

  return response.data;

};

export const getTradeSummary = async () => {

  const response = await api.get("/trades/summary");

  return response.data;

};