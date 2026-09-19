package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.InventoryReportFilterDTO;
import com.estradahermanos.shoestock.dto.response.RestockSuggestionDTO;
import com.estradahermanos.shoestock.dto.response.SlowMoverDTO;
import com.estradahermanos.shoestock.mapper.ReportInventoryMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.OrderDetailRepository;
import com.estradahermanos.shoestock.repository.repositories.OrderRepository;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ReportCatalogLookup;
import com.estradahermanos.shoestock.utilities.ReportValidator;
import com.estradahermanos.shoestock.utilities.RestockAdviceEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportInventoryQueryServiceTest
{
    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ReportCatalogLookup catalogLookup;

    @Mock
    private ReportInventoryMapper reportInventoryMapper;

    private ReportInventoryQueryService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportInventoryQueryService(
                shoeStockRepository, saleRepository, orderRepository, orderDetailRepository,
                catalogLookup, new ReportValidator(), reportInventoryMapper);
    }

    @Test
    void slowMoversIncludesNeverSoldVariantsWithStock()
    {
        ShoeStock variant = ShoeStock.builder()
                .id(15).shoeId("OXF-001").color("Negro").size(42).stock(4).minStock(1).build();
        when(catalogLookup.allVariants()).thenReturn(Map.of(15, variant));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001",
                Shoe.builder().code("OXF-001").name("Oxford").supplier(2).type("Caballero").build()));
        when(catalogLookup.supplierNames()).thenReturn(Map.of(2, "Maria Lopez"));
        when(saleRepository.findLastSaleDateGrouped()).thenReturn(List.of());

        List<SlowMoverDTO> result = service.slowMovers(InventoryReportFilterDTO.builder().days(45).build());

        assertEquals(1, result.size());
        assertEquals(15, result.get(0).getShoeStockId());
        assertNull(result.get(0).getLastSaleDate());
        assertEquals("Maria Lopez", result.get(0).getSupplierName());
    }

    @Test
    void restockMarksZeroStockAsReplenish()
    {
        ShoeStock variant = ShoeStock.builder()
                .id(15).shoeId("OXF-001").color("Negro").size(42).stock(0).minStock(2).build();
        when(catalogLookup.allVariants()).thenReturn(Map.of(15, variant));
        when(catalogLookup.allShoes()).thenReturn(Map.of("OXF-001",
                Shoe.builder().code("OXF-001").name("Oxford").supplier(2).type("Caballero").build()));
        when(saleRepository.sumAmountGroupedByStock(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        List<RestockSuggestionDTO> result = service.restock(InventoryReportFilterDTO.builder().build());

        assertEquals(1, result.size());
        assertEquals(RestockAdviceEnum.REPONER, result.get(0).getAdvice());
    }
}
