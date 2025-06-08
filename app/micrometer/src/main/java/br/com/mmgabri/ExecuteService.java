package br.com.mmgabri;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ExecuteService {
    public Mono<String> execute(long duration) {
        long variation = Math.round(duration * 0.2); // 20% de variação
        long min = duration - variation;
        long max = duration + variation;
        long adjustedDuration = ThreadLocalRandom.current().nextLong(min, max + 1);

        return Mono.just(getReturnCode())
                .delayElement(Duration.ofMillis(adjustedDuration));

    }

    public String getReturnCode() {
        Random random = new Random();
        int chance = random.nextInt(100);

        if (chance < 70) {
            return "00";
        } else {
            int numero = 1 + random.nextInt(30);
            return String.format("%02d", numero);
        }
    }
}
