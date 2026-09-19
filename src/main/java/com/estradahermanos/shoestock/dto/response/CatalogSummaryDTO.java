package com.estradahermanos.shoestock.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogSummaryDTO
{
    @JsonProperty("style_count")
    private Long styleCount;

    @JsonProperty("new_styles")
    private Long newStyles;
}
