package com.estradahermanos.shoestock.dto.response;

import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
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
public class OrderResponseDTO
{
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("supplier_name")
    private String supplierName;

    private OrderStatusEnum status;

    @JsonProperty("creation_date")
    private LocalDate creationDate;

    private List<OrderDetailResponseDTO> details;
}
