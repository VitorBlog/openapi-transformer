package dev.vitorpaulo.transformer.command;

import dev.vitorpaulo.transformer.model.Api;
import dev.vitorpaulo.transformer.service.OpenApiService;
import dev.vitorpaulo.transformer.service.TransformerService;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransformCommand implements Callable<Integer> {

    private final OpenApiService openApiService;
    private final TransformerService transformerService;

    @CommandLine.Option(names = { "--i", "-input" }, description = "OpenApi file(s) and url(s)", required = true)
    List<String> input;

    @CommandLine.Option(names = { "--t", "-transformer" }, description = "Transformers: ANGULAR_AXIOS, ANGULAR_HTTP, SPRING_FEIGN", required = true)
    String transformer;

    @CommandLine.Option(names = { "--o", "-output" }, description = "Output folder. With multiple inputs use placeholders: {name}, {index}", required = true)
    String output;

    @CommandLine.Option(names = { "--p", "-package" }, description = "Base package. Example: dev.vitorpaulo.transformer", required = true)
    String basePackage;

    @CommandLine.Option(names = { "--tp", "-parameter" }, description = "Transformer parameters (KEY=VALUE). Parameters: GENERATE_CONTROLLER")
    List<String> parameters;

    public Integer call() {
        if (input == null) {
            log.error("Input files and urls are empty.");
            return 1;
        }

        final var apis = openApiService.readAll(input);

       try {
           for (Api value : apis) {
               transformerService.executeTransformer(apis.indexOf(value), basePackage, transformer, output, value);
           }

           return 0;
       } catch (Exception exception) {
           return 1;
       }
    }
}
