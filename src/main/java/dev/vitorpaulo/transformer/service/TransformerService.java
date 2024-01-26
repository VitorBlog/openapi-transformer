package dev.vitorpaulo.transformer.service;

import dev.vitorpaulo.transformer.model.Api;
import dev.vitorpaulo.transformer.model.Transformer;
import dev.vitorpaulo.transformer.model.TransformerType;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransformerService {

    private final List<Transformer> transformers;

    public void executeTransformer(Integer index, String basePackage, String type, String output, Api api) throws FileAlreadyExistsException {
        final var outputFolder = new File(
                output.replace("{index}", index.toString())
                        .replace("{name}", api.getName())
        );

        if (outputFolder.exists() || !outputFolder.mkdirs()) {
            log.error("Output folder '{}' already exists.", output);
            outputFolder.delete();
            // TODO: throw new FileAlreadyExistsException(output);
        }

        final var transformer = findTransformer(type);
        log.info("Starting '{}' transformer...", transformer.getType().name());

        transformer.run(basePackage, outputFolder, api);
    }

    private Transformer findTransformer(String type) {
        return findTransformer(
                TransformerType.valueOf(
                        type.toUpperCase()
                                .replace(" ", "_")
                                .replace("-", "_")
                )
        );
    }

    private Transformer findTransformer(TransformerType type) {
        return transformers.stream()
                .filter(value -> value.getType().equals(type))
                .findFirst()
                .orElseThrow();
    }
}
