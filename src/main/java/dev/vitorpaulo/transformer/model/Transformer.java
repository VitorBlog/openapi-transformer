package dev.vitorpaulo.transformer.model;

import java.io.File;

public interface Transformer {

    TransformerType getType();

    void run(String basePackage, File output, Api openAPI);
}
