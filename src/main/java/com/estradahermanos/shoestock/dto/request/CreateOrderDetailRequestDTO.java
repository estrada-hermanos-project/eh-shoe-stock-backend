package com.estradahermanos.shoestock.dto.request;

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
public class CreateOrderDetailRequestDTO
{
    @JsonProperty("shoe_stock_id")
    private Integer shoeStockId;

    private Integer amount;
}
