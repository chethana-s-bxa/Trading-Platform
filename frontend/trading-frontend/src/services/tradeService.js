import api from "../api/axios";

export const buyStock = async (symbol, quantity) => {

  const response = await api.post("/trade/buy", {
    symbol,
    quantity
  });

  return response.data;

};

export const sellStock = async (symbol, quantity) => {

  const response = await api.post("/trade/sell", {
    symbol,
    quantity
  });

  return response.data;

};
