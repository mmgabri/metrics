package br.com.mmgabri;


import io.opentelemetry.api.metrics.Meter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/increment")
@AllArgsConstructor
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private final ExecuteService service;
    private final Meter meter;
    private final MetricService metric;


    @PostMapping
    public Mono<ResponseEntity<String>> executeTransaction(@RequestBody DelayRequest request) {
        var startTime = OffsetDateTime.now();

        return service.execute(request.delay)
                .map(returnCode -> {
                    incrementMetric(startTime, returnCode);
                    return ResponseEntity.ok("sucess");
                })
                .onErrorResume(e -> {
                    logger.error("Erro no processamento {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error"));
                });
    }

    private void incrementMetric(OffsetDateTime startTime, String returnCode) {
        try {
            var duration = 0L;
            duration = Duration.between(startTime, OffsetDateTime.now()).toMillis();
            logger.info("Request processado em {} ms", duration);

            metric.increment("app", "poc-metrics-otlp", "status", returnCode);
            metric.registryDuration(duration, "app", "poc-metrics-otlp", "status", returnCode);
        } catch (Exception e) {
            logger.error("Error ao enviar métrica", e);
            throw new RuntimeException(e);
        }
    }
}
