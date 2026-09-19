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
public class OrdersBySupplierDTO
{
    @JsonProperty("supplier_id")
    private Integer supplierId;

    @JsonProperty("supplier_name")
    private String supplierName;

    @JsonProperty("pending_count")
    private Long pendingCount;

    @JsonProperty("received_count")
    private Long receivedCount;

    @JsonProperty("pairs_requested")
    private Long pairsRequested;

    @JsonProperty("pairs_received")
    private Long pairsReceived;
}
