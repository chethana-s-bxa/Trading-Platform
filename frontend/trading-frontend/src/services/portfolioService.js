import api from "../api/axios";

export const getPortfolio = async () => {

  const response = await api.get("/portfolio");

  return response.data;

};

export const getPortfolioValue = async () => {

  const response = await api.get("/portfolio/value");

  return response.data;

};