package br.com.mmgabri;

import com.timgroup.statsd.StatsDClient;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final StatsDClient statsDClient;
    private final MeterRegistry meterRegistry;

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
            //Calcula duration
            var duration = 0L;
            duration = Duration.between(startTime, OffsetDateTime.now()).toMillis();
            logger.info("Request processado em {} ms", duration);

            //Incrementa métrica via Micrometer
            meterRegistry.counter("app_transaction6_count_micrometer", "app", "autorizador", "status", returnCode).increment();
            DistributionSummary summary = meterRegistry.summary("app_transaction6_duration_micrometer", "app", "autorizador", "status", returnCode);
            summary.record(duration);

            //Incrementa métrica via DogStatsD
            statsDClient.incrementCounter("app_transaction6_count", "app:autorizador", "status:" + returnCode);
            statsDClient.recordExecutionTime("app_transaction6_duration_time", duration, "app:autorizador", "status:" + returnCode);
            statsDClient.recordHistogramValue("app_transaction6_duration_histogram", duration, "app:autorizador", "status:" + returnCode);
            statsDClient.recordDistributionValue("app_transaction6_duration_distribution", duration, "app:autorizador", "status:" + returnCode);
        } catch (Exception e) {
            logger.error("Error ao enviar métrica", e);
            throw new RuntimeException(e);
        }




    }
}
