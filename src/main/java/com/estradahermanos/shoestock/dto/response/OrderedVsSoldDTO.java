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
public class OrderedVsSoldDTO
{
    @JsonProperty("shoe_stock_id")
    private Integer shoeStockId;

    @JsonProperty("shoe_name")
    private String shoeName;

    private String color;

    private Integer size;

    @JsonProperty("pairs_ordered")
    private Long pairsOrdered;

    @JsonProperty("pairs_sold")
    private Long pairsSold;

    private Long difference;

    @JsonProperty("current_stock")
    private Integer currentStock;
}
