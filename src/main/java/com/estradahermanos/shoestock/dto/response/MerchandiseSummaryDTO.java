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
public class MerchandiseSummaryDTO
{
    @JsonProperty("pending_orders")
    private Long pendingOrders;

    @JsonProperty("pairs_received")
    private Long pairsReceived;

    @JsonProperty("pairs_pending")
    private Long pairsPending;
}
