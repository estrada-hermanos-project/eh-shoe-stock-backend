package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.request.InventoryReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.MerchandiseReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.SalesReportFilterDTO;
import com.estradahermanos.shoestock.dto.request.SendReportEmailRequestDTO;
import com.estradahermanos.shoestock.dto.request.SummaryReportFilterDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportEmailFilterMapper
{
    SalesReportFilterDTO toSalesFilter(SendReportEmailRequestDTO request);

    InventoryReportFilterDTO toInventoryFilter(SendReportEmailRequestDTO request);

    MerchandiseReportFilterDTO toMerchandiseFilter(SendReportEmailRequestDTO request);

    SummaryReportFilterDTO toSummaryFilter(SendReportEmailRequestDTO request);
}
