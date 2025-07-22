import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    default: {
      executor: 'shared-iterations',
      vus: 100,        // Concurrent virtual users
      iterations: 300, // Total number of requests
      maxDuration: '5s',
    },
  },
};

const TICKET_NAME = __ENV.TICKET_NAME || 'test1';
const API_VERSION = __ENV.API_VERSION || 'v1';
const ports = [8081, 8082, 8083];
// const ports = [8080];

export default function () {
  // Select a random port for each iteration
  const port = ports[Math.floor(Math.random() * ports.length)];
  const url = `http://localhost:${port}/api/tickets/${API_VERSION}`;
  
  const payload = JSON.stringify({
    ticketName: TICKET_NAME,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.patch(url, payload, params);
  check(res, {
    'is status 200': (r) => r.status === 200,
  });
}
