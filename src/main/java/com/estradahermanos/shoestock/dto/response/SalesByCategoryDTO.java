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
public class SalesByCategoryDTO
{
    private String type;

    @JsonProperty("amount_sold")
    private Long amountSold;

    @JsonProperty("sale_count")
    private Long saleCount;

    @JsonProperty("share_percent")
    private Integer sharePercent;
}
