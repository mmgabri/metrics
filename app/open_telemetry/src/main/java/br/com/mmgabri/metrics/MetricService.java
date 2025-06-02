package br.com.mmgabri.metrics;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
}
