package dev.vitorpaulo.transformer.transformer;

import dev.vitorpaulo.transformer.model.Transformer;
import dev.vitorpaulo.transformer.model.TransformerType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class SpringFeignTransformer implements Transformer {

    private static final String classTemplate = "package %s.%s;\n\n@Getter\n@Setter\n@Builder\n@AllArgsConstructor\npublic class %s {\n%s\n}";

    private File dtoFolder;
    private File modelFolder;
    private File clientFolder;
    private File requestFolder;
    private File responseFolder;

    @Override
    public TransformerType getType() {
        return TransformerType.SPRING_FEIGN;
    }

    @Override
    @SneakyThrows
    public void run(String basePackage, File output, OpenAPI openAPI) {
        generateFolders(output);

        log.info("Transforming '{}'...", openAPI.getInfo().getTitle());
        generateSchemas(basePackage, openAPI);
    }

    private void generateFolders(File output) {
        log.info("Generating folders...");

        dtoFolder = new File(output, "dto");
        modelFolder = new File(output, "model");
        clientFolder = new File(output, "client");
        requestFolder = new File(output, "request");
        responseFolder = new File(output, "response");

        dtoFolder.mkdirs();
        modelFolder.mkdirs();
        clientFolder.mkdirs();
        requestFolder.mkdirs();
        responseFolder.mkdirs();
    }

    private void generateSchemas(String basePackage, OpenAPI openAPI) throws IOException {
        for (Map.Entry<String, Schema> entry : openAPI.getComponents().getSchemas().entrySet()) {
            if (entry.getKey().startsWith("##")) {
                continue;
            }

            final var isRequest = entry.getKey().endsWith("Request");
            final var file = new File(isRequest? requestFolder : responseFolder, "%s.%s".formatted(StringUtils.capitalize(entry.getKey()), "java"));

            log.info("Creating {} '{}' with type '{}'...", isRequest? "request" : "response", entry.getKey(), entry.getValue().getType());
            file.createNewFile();

            final var content = new StringBuilder();
            for (var key : entry.getValue().getProperties().keySet()) {
                final var value = (Schema) entry.getValue().getProperties().get(key);

                content.append("\n\tprivate final ")
                        .append(StringUtils.capitalize(value.getType()))
                        .append(" ")
                        .append(key)
                        .append(";");
            }

            FileUtils.write(file, classTemplate.formatted(basePackage, isRequest? "request" : "response", entry.getKey(), content), StandardCharsets.UTF_8);
        }
    }
}
