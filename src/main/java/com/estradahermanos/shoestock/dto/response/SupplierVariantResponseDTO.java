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
public class SupplierVariantResponseDTO
{
    @JsonProperty("shoe_stock_id")
    private Integer shoeStockId;

    @JsonProperty("shoe_code")
    private String shoeCode;

    @JsonProperty("shoe_name")
    private String shoeName;

    private String color;

    private Integer size;
}
