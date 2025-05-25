import http from 'k6/http';
import { check } from 'k6';

export let options = {
    stages: [
        { duration: '10m', target: 50 } 
        ],
    thresholds: {
        http_reqs: ['rate>=400'],
    }
};

export default function () {
    let url = 'http://localhost:8080/increment';

    let payload = JSON.stringify({
        delay :  10
    });

    let params = {
        headers: {
            'Content-Type': 'application/json'
        }
    };

    let res = http.post(url, payload, params);

    check(res, {
        'status é 200': (r) => r.status === 200,
        'resposta menor que 999ms': (r) => r.timings.duration < 999,
    });
}
