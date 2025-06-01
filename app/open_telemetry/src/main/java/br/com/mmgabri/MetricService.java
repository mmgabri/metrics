package br.com.mmgabri;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import org.springframework.stereotype.Service;

@Service
public class MetricService {

    private final LongCounter counter;
    private final LongHistogram histogram;

    public MetricService(Meter meter) {
        this.counter = meter
                .counterBuilder("app_otlp_counter_teste1")
                .setDescription("Total de requisições feitas")
                .setUnit("1")
                .build();
        this.histogram = meter
                .histogramBuilder("app_otlp_duration_teste1")
                .setDescription("Tempo de execução em milissegundos")
                .setUnit("ms")
                .ofLongs()
                .build();
    }

    public void increment(String... tags) {
        counter.add(1, buildAttributes(tags));
    }

    public void registryDuration(long duration, String... tags) {
        histogram.record(duration, buildAttributes(tags));
    }

    private Attributes buildAttributes(String... tags) {
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Tags devem ser pares: chave1, valor1...");
        }
        Attributes attributes = Attributes.empty();
        for (int i = 0; i < tags.length; i += 2) {
            attributes = attributes.toBuilder()
                    .put(AttributeKey.stringKey(tags[i]), tags[i + 1])
                    .build();
        }
        return attributes;
    }
}
