package br.com.mmgabri.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.springframework.stereotype.Component;

@Component
public class AttributeFactory {

    public Attributes from(String... tags) {
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

