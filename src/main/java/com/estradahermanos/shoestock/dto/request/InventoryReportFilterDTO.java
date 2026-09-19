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
public class InventoryReportFilterDTO
{
    private String type;

    @JsonProperty("supplier_id")
    private Integer supplierId;

    private Integer days;

    @JsonProperty("include_out_of_stock")
    private Boolean includeOutOfStock;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("group_by")
    private String groupBy;
}
