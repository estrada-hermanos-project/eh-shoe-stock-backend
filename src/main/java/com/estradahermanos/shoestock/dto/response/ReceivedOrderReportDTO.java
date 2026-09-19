package com.estradahermanos.shoestock.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedOrderReportDTO
{
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("creation_date")
    private LocalDate creationDate;

    @JsonProperty("total_pairs")
    private Integer totalPairs;

    private List<OrderDetailResponseDTO> details;
}
