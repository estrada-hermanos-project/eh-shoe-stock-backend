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
public class SalesBySizeDTO
{
    private Integer size;

    private String type;

    @JsonProperty("amount_sold")
    private Long amountSold;

    @JsonProperty("current_stock")
    private Integer currentStock;
}
