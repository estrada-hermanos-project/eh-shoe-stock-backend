package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.response.PendingOrderReportDTO;
import com.estradahermanos.shoestock.dto.response.ReceivedOrderReportDTO;
import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.repository.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReportMerchandiseMapper
{
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "supplierName", source = "supplierName")
    @Mapping(target = "creationDate", source = "order.orderDeliveryDate")
    @Mapping(target = "daysPending", source = "daysPending")
    @Mapping(target = "totalPairs", source = "totalPairs")
    @Mapping(target = "details", source = "details")
    PendingOrderReportDTO toPending(Order order, String supplierName, Long daysPending,
                                    Integer totalPairs, List<OrderDetailResponseDTO> details);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "supplierName", source = "supplierName")
    @Mapping(target = "creationDate", source = "order.orderDeliveryDate")
    @Mapping(target = "totalPairs", source = "totalPairs")
    @Mapping(target = "details", source = "details")
    ReceivedOrderReportDTO toReceived(Order order, String supplierName, Integer totalPairs,
                                      List<OrderDetailResponseDTO> details);
}
