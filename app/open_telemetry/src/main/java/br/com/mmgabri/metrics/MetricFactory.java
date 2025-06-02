package br.com.mmgabri.metrics;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetricFactory {

    private final Meter meter;
    private final ConcurrentHashMap<String, LongCounter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LongHistogram> histograms = new ConcurrentHashMap<>();

    public MetricFactory(Meter meter) {
        this.meter = meter;
    }

    public LongCounter getOrCreateCounter(String name) {
        return counters.computeIfAbsent(name, metricName ->
                meter.counterBuilder(metricName)
                        .setUnit("1")
                        .build()
        );
    }

    public LongHistogram getOrCreateHistogram(String name) {
        return histograms.computeIfAbsent(name, metricName ->
                meter.histogramBuilder(metricName)
                        .setUnit("ms")
                        .ofLongs()
                        .build()
        );
    }
}