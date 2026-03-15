import { useEffect, useState } from "react";
import { getPortfolio, getPortfolioValue } from "../services/portfolioService";
import { connectMarketSocket, subscribePortfolio } from "../websocket/websocketService";

function Portfolio() {

  const [holdings, setHoldings] = useState([]);
  const [portfolioValue, setPortfolioValue] = useState(null);

  const userId = localStorage.getItem("userId");

  useEffect(() => {

    fetchPortfolio();

    // Connect websocket
    connectMarketSocket(() => {});

    // Subscribe to portfolio updates
    subscribePortfolio(userId, handlePortfolioUpdate);

  }, []);

  const fetchPortfolio = async () => {

    try {

      const portfolioData = await getPortfolio();
      const valueData = await getPortfolioValue();

      setHoldings(portfolioData.holdings);
      setPortfolioValue(valueData);

    } catch (error) {

      console.error("Error fetching portfolio", error);

    }

  };

  const handlePortfolioUpdate = (update) => {

    setHoldings(update.holdings);

    setPortfolioValue({
      totalInvestment: update.totalInvestment,
      currentValue: update.currentValue,
      profitLoss: update.profitLoss
    });

  };

  return (

    <div>

      <h1>My Portfolio</h1>

      {portfolioValue && (

        <div>

          <p>Total Investment: {portfolioValue.totalInvestment}</p>
          <p>Current Value: {portfolioValue.currentValue}</p>
          <p>Profit / Loss: {portfolioValue.profitLoss}</p>

        </div>

      )}

      <h2>Holdings</h2>

      <table border="1">

        <thead>
          <tr>
            <th>Symbol</th>
            <th>Company</th>
            <th>Quantity</th>
            <th>Average Price</th>
          </tr>
        </thead>

        <tbody>

          {holdings.map((stock) => (

            <tr key={stock.symbol}>
              <td>{stock.symbol}</td>
              <td>{stock.companyName}</td>
              <td>{stock.quantity}</td>
              <td>{stock.averagePrice}</td>
            </tr>

          ))}

        </tbody>

      </table>

    </div>

  );

}

export default Portfolio;