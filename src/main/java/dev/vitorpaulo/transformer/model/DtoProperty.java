package dev.vitorpaulo.transformer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DtoProperty {

    private final String name;
    private final String type;
    private final String subtype;
}
