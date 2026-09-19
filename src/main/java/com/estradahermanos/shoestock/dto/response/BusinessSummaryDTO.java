package com.estradahermanos.shoestock.dto.response;

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
public class BusinessSummaryDTO
{
    private SalesSummaryDTO sales;

    private InventorySummaryDTO inventory;

    private MerchandiseSummaryDTO merchandise;

    private CatalogSummaryDTO catalog;
}
