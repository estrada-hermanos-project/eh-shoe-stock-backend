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
public class OutOfStockDTO
{
    @JsonProperty("shoe_stock_id")
    private Integer shoeStockId;

    @JsonProperty("shoe_name")
    private String shoeName;

    private String color;

    private Integer size;

    private String type;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("amount_sold")
    private Long amountSold;

    @JsonProperty("has_pending_order")
    private Boolean hasPendingOrder;
}
