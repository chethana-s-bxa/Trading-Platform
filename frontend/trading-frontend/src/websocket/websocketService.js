
import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";

let stompClient = null;

export const connectMarketSocket = (onMessage) => {

  const socket = new SockJS("http://localhost:8081/ws-market");

  stompClient = new Client({
    webSocketFactory: () => socket,
    reconnectDelay: 5000,
    onConnect: () => {

      console.log("Connected to Market WebSocket");

      stompClient.subscribe("/topic/market/prices", (message) => {

        const stockUpdate = JSON.parse(message.body);
        onMessage(stockUpdate);

      });

    },
  });

  stompClient.activate();
};

export const disconnectMarketSocket = () => {

  if (stompClient) {
    stompClient.deactivate();
  }

};

export const subscribePortfolio = (userId, onMessage) => {

  if (!stompClient || !stompClient.connected) {
    return;
  }

  stompClient.subscribe(`/topic/portfolio/${userId}`, (message) => {

    const data = JSON.parse(message.body);

    onMessage(data);

  });

};