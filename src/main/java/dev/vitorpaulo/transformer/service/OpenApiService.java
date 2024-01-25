package dev.vitorpaulo.transformer.service;

import dev.vitorpaulo.transformer.model.Api;
import dev.vitorpaulo.transformer.model.Dto;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    public List<Api> readAll(List<String> input) {
        return Stream.concat(
                input.stream()
                        .filter(value -> value.startsWith("http"))
                        .map(this::readUrl)
                        .map(this::mapToApi),
                input.stream()
                        .filter(value -> !value.startsWith("http"))
                        .map(this::readFile)
                        .map(this::mapToApi)
        ).toList();
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

    private Api mapToApi(OpenAPI openAPI) {
        return Api.builder()
                .name(StringUtils.defaultString(openAPI.getInfo().getTitle(), "No Title #%s".formatted(RandomUtils.nextInt())))
                .controllers(Collections.emptyList())
                .dtoList(mapDtoList(openAPI))
                .openAPI(openAPI)
                .build();
    }

    private List<Dto> mapDtoList(OpenAPI openAPI) {
        final var dtoList = new ArrayList<Dto>();
        for (Map.Entry<String, ?> entry : openAPI.getComponents().getSchemas().entrySet()) {
            if (entry.getKey().startsWith("##")) {
                continue;
            }

            final var isRequest = entry.getKey().toLowerCase().endsWith("request");
            dtoList.add(
                    Dto.builder()
                            .request(isRequest)
                            .name(entry.getKey())
                            .packageName(isRequest? "request" : "response")
                            .build()
            );
        }

        return dtoList;
    }
}
