package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryVariantLookupTest
{
    @Mock
    private ShoeStockRepository shoeStockRepository;

    private InventoryVariantLookup lookup;

    @BeforeEach
    void setUp()
    {
        lookup = new InventoryVariantLookup(shoeStockRepository);
    }

    @Test
    void findVariantReturnsFirstMatch()
    {
        ShoeStock first  = ShoeStock.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(10).build();
        ShoeStock second = ShoeStock.builder().id(8).shoeId("OXF-002").color("Negro").size(40).stock(4).build();
        when(shoeStockRepository.findByShoeNameAndColorAndSize("Oxford clasico", "Negro", 40))
                .thenReturn(List.of(first, second));

        ShoeStock result = lookup.findVariant("Oxford clasico", "Negro", 40);

        assertEquals(7, result.getId());
    }

    @Test
    void findVariantRejectsMissingStock()
    {
        when(shoeStockRepository.findByShoeNameAndColorAndSize("Oxford clasico", "Negro", 40))
                .thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> lookup.findVariant("Oxford clasico", "Negro", 40));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Some parameter is not being sent correctly: no matching stock", exception.getMessage());
    }
}
