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
public class ShoeStockResponseDTO
{
    private Integer id;

    @JsonProperty("shoe_id")
    private String shoeId;

    private String color;

    private Integer size;

    private Integer stock;
}
