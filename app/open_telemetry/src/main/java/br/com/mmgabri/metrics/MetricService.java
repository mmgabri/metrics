package br.com.mmgabri.metrics;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class MetricService {

    private final MetricFactory metricFactory;
    private final AttributeFactory attributeFactory;

    public void increment(String metricName, String... tags) {
        var counter = metricFactory.getOrCreateCounter(metricName);
        counter.add(1, attributeFactory.from(tags));
    }

    public void registryDuration(String metricName, long duration, String... tags) {
        var histogram = metricFactory.getOrCreateHistogram(metricName);
        histogram.record(duration, attributeFactory.from(tags));
    }

    public void increment () {
        // Exportador OTLP para o agente (ex: Datadog, tempo real)
        OtlpGrpcMetricExporter exporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://localhost:4317") // ajuste se necessário
                .build();

        // Provedor com exportação periódica
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(
                        PeriodicMetricReader.builder(exporter)
                                .setInterval(Duration.ofSeconds(5))
                                .build()
                ).build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();

        // Meter (nome do seu serviço ou módulo)
        Meter meter = openTelemetry.getMeter("meu-app");

        // Histogram para latência (milissegundos)
        DoubleHistogram latencyHistogram = meter.histogramBuilder("api.latency")
                .setDescription("Latência da API em ms")
                .setUnit("ms")
                .build();

        // Registrando valores simulados com tags
        latencyHistogram.record(120.0, Attributes.of(
                io.opentelemetry.api.common.AttributeKey.stringKey("endpoint"), "/users",
                io.opentelemetry.api.common.AttributeKey.stringKey("method"), "GET"
        ));
    }
}
