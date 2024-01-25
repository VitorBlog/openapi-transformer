package dev.vitorpaulo.transformer.transformer;

import dev.vitorpaulo.transformer.model.Transformer;
import dev.vitorpaulo.transformer.model.TransformerType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class SpringFeignTransformer implements Transformer {

    private static final String classTemplate = "package %s.%s;\n\n%s\n\n@Getter\n@Setter\n@Builder\n@AllArgsConstructor\npublic class %s {\n%s\n}";
    private final ArrayList<String> imports = new ArrayList<>();

    private String basePackage = "hello.world";
    private File responseFolder;
    private File requestFolder;
    private File clientFolder;
    private File modelFolder;

    @Override
    public TransformerType getType() {
        return TransformerType.SPRING_FEIGN;
    }

    @Override
    @SneakyThrows
    public void run(String basePackage, File output, OpenAPI openAPI) {
        this.basePackage = basePackage;

        generateFolders(output);
        generateSchemas(openAPI);
    }

    private void generateFolders(File output) {
        log.info("Generating folders...");

        modelFolder = new File(output, "model");
        clientFolder = new File(output, "client");
        requestFolder = new File(output, "request");
        responseFolder = new File(output, "response");

        for (File file : List.of(modelFolder, clientFolder, requestFolder, responseFolder)) {
            if (!file.mkdirs()) {
                log.error("Cannot create '{}' folder.", file.getName());
            }
        }
    }

    private void generateSchemas(OpenAPI openAPI) throws IOException {
        log.info("Transforming '{}'...", openAPI.getInfo().getTitle());

        for (Map.Entry<String, ?> entry : openAPI.getComponents().getSchemas().entrySet()) {
            if (entry.getKey().startsWith("##")) {
                continue;
            }

            final var schema = (Schema<?>) entry.getValue();
            final var isRequest = isRequest(entry.getKey());
            final var file = new File(isRequest? requestFolder : responseFolder, "%s.%s".formatted(StringUtils.capitalize(entry.getKey()), "java"));

            log.info("Creating {} '{}' with type '{}'...", getPackage(entry.getKey()), entry.getKey(), schema.getType());

            if (!file.createNewFile()) {
                log.error("Cannot create '{}' file.", file.getName());
                System.exit(1);
            }

            FileUtils.write(
                    file,
                    classTemplate.formatted(
                            basePackage,
                            getPackage(entry.getKey()),
                            StringUtils.join(imports, "\n"),
                            entry.getKey(),
                            generateContent(schema)
                    ),
                    StandardCharsets.UTF_8
            );
        }
    }

    private String generateContent(Schema<?> schema) {
        final var content = new StringBuilder();
        for (var key : schema.getProperties().keySet()) {
            content.append("\n\tprivate final ")
                    .append(validateType(schema.getProperties().get(key)))
                    .append(" ")
                    .append(key)
                    .append(";");
        }

        return content.toString();
    }
    
    private String validateType(Schema<?> value) {
        var type = StringUtils.capitalize(value.getType());
        
        if (Objects.equals(type, "Array")) {
            final var path = value.getItems().get$ref();
            var arrayType = "*";

            if (StringUtils.isNotBlank(path)) {
                arrayType = FilenameUtils.getName(path);
                addImport(basePackage, "%s.%s".formatted(getPackage(arrayType), arrayType));
            }

            type = "List<%s>".formatted(arrayType);
            addImport("java.util", "List");
        }
        
        return type;
    }

    private void addImport(String basePackage, String name) {
        final var value = "import %s.%s;".formatted(basePackage, name);

        if (!imports.contains(value)) {
            imports.add(value);
        }
    }

    private Boolean isRequest(String name) {
        return name.endsWith("Request");
    }

    private String getPackage(String name) {
        return isRequest(name)? "request" : "response";
    }
}
