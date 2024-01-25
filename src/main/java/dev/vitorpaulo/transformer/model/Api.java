package dev.vitorpaulo.transformer.model;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Api {

    private final String name;
    private final OpenAPI openAPI;
    private final List<Dto> dtoList;
    private final List<Controller> controllers;
}
