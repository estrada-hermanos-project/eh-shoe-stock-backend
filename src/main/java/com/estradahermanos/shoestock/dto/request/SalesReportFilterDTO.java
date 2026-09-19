package com.estradahermanos.shoestock.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportFilterDTO
{
    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    private Integer limit;

    private String type;

    @JsonProperty("supplier_id")
    private Integer supplierId;

    @JsonProperty("shoe_code")
    private String shoeCode;

    @JsonProperty("with_stock_only")
    private Boolean withStockOnly;

    @JsonProperty("group_by")
    private String groupBy;
}
