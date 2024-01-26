package dev.vitorpaulo.transformer.model;

import java.io.File;

public interface Transformer {

    TransformerType getType();

    void run(String basePackage, File output, Api api);

    void generateFolders(File output);

    void generateDto(Api api);

    String generateController(Controller controller);

    String generateContent(Dto dto);

    String formatType(DtoProperty dtoProperty);
}
