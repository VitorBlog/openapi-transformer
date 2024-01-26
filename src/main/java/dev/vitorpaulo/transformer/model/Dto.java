package dev.vitorpaulo.transformer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Dto {

    private final String name;
    private final Boolean request;
    private final String packageName;
    private final List<DtoProperty> properties;
}
