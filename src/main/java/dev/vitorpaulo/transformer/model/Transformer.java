package dev.vitorpaulo.transformer.model;

import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;

public interface Transformer {

    TransformerType getType();

    void run(String basePackage, File output, OpenAPI openAPI);
}
