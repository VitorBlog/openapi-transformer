package dev.vitorpaulo.transformer.service;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    public List<OpenAPI> readAll(List<String> input) {
        return input.stream().map(this::readUrl).toList();
    }

    public OpenAPI readUrl(String url) {
        return new OpenAPIParser()
                .readLocation(url, null, null)
                .getOpenAPI();
    }

    public OpenAPI readFile(String file) {
        return new OpenAPIParser()
                .readContents(file, null, null)
                .getOpenAPI();
    }
}
