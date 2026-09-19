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
public class SalesBySupplierDTO
{
    @JsonProperty("supplier_id")
    private Integer supplierId;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("amount_sold")
    private Long amountSold;

    @JsonProperty("styles_sold")
    private Long stylesSold;

    @JsonProperty("share_percent")
    private Integer sharePercent;
}
