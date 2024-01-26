package dev.vitorpaulo.transformer.transformer;

import dev.vitorpaulo.transformer.model.*;
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
    public void run(String basePackage, File output, Api api) {
        this.basePackage = basePackage;

        generateFolders(output);

        log.info("Transforming '{}'...", api.getName());
        generateDto(api);
    }

    @Override
    public void generateFolders(File output) {
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

    @Override
    @SneakyThrows
    public void generateDto(Api api) {
        for (Dto dto : api.getDtoList()) {
            final var file = new File(dto.getRequest()? requestFolder : responseFolder, "%s.%s".formatted(StringUtils.capitalize(dto.getName()), "java"));

            log.info("Creating {} '{}'...", dto.getPackageName(), dto.getName());

            if (!file.createNewFile()) {
                log.error("Cannot create '{}' file.", file.getName());
                System.exit(1);
            }

            FileUtils.write(
                    file,
                    classTemplate.formatted(
                            basePackage,
                            dto.getPackageName(),
                            StringUtils.join(imports, "\n"),
                            dto.getName(),
                            generateContent(dto)
                    ),
                    StandardCharsets.UTF_8
            );
        }
    }

    @Override
    public String generateController(Controller controller) {
        return null;
    }

    @Override
    public String generateContent(Dto dto) {
        final var content = new StringBuilder();
        for (DtoProperty property : dto.getProperties()) {
            content.append("\n\tprivate final ")
                    .append(formatType(property))
                    .append(" ")
                    .append(property.getName())
                    .append(";");
        }

        return content.toString();
    }

    @Override
    public String formatType(DtoProperty property) {
        var type = StringUtils.capitalize(property.getType());

        if (Objects.equals(type, "Array")) {
            var arrayType = "*";

            if (StringUtils.isNotBlank(property.getSubtype())) {
                arrayType = property.getSubtype();
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

    private String getPackage(String name) {
        return name.endsWith("Request")? "request" : "response";
    }
}
