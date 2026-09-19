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
public class InventoryGroupDTO
{
    private String group;

    @JsonProperty("variant_count")
    private Long variantCount;

    @JsonProperty("pairs_in_stock")
    private Long pairsInStock;

    @JsonProperty("out_of_stock_count")
    private Long outOfStockCount;

    @JsonProperty("low_stock_count")
    private Long lowStockCount;
}
