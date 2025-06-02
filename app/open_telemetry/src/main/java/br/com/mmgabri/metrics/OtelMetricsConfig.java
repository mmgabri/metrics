package br.com.mmgabri.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OtelMetricsConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        OtlpGrpcMetricExporter exporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://localhost:4317")
                .setTimeout(Duration.ofSeconds(3))
                .setRetryPolicy(RetryPolicy.builder()
                        .setMaxAttempts(3)
                        .build())
                .build();

        PeriodicMetricReader reader = PeriodicMetricReader.builder(exporter)
                .setInterval(Duration.ofSeconds(10))
                .build();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(reader)
                .setResource(Resource.getDefault())
                .build();

        return OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();
    }

    @Bean
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter("poc-metrics-otlp");
    }
}

