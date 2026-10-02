package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportCatalogLookupTest
{
    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private SupplierRepository supplierRepository;

    private ReportCatalogLookup lookup;

    @BeforeEach
    void setUp()
    {
        lookup = new ReportCatalogLookup(shoeStockRepository, shoeRepository, supplierRepository);
    }

    @Test
    void returnsEmptyMapsWhenTheIdentifiersAreMissing()
    {
        assertTrue(lookup.variantsById(null).isEmpty());
        assertTrue(lookup.variantsById(List.of()).isEmpty());
        assertTrue(lookup.shoesByCode(null).isEmpty());
        assertTrue(lookup.shoesByCode(List.of()).isEmpty());
        verifyNoInteractions(shoeStockRepository, shoeRepository);
    }

    @Test
    void indexesCatalogRowsByTheirIdentifiers()
    {
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").stock(2).size(42).color("Negro").build();
        Shoe shoe = Shoe.builder().code("OXF-001").name("Oxford").supplier(2).build();
        when(shoeStockRepository.findAllById(List.of(15))).thenReturn(List.of(variant));
        when(shoeStockRepository.findAll()).thenReturn(List.of(variant));
        when(shoeRepository.findAllById(List.of("OXF-001"))).thenReturn(List.of(shoe));
        when(shoeRepository.findAll()).thenReturn(List.of(shoe));
        when(supplierRepository.findAll()).thenReturn(List.of(
                Supplier.builder().id(2).fullName("Maria Lopez").phone("555").build()));

        assertEquals(variant, lookup.variantsById(List.of(15)).get(15));
        assertEquals(variant, lookup.allVariants().get(15));
        assertEquals(shoe, lookup.shoesByCode(List.of("OXF-001")).get("OXF-001"));
        assertEquals(shoe, lookup.allShoes().get("OXF-001"));
        assertEquals("Maria Lopez", lookup.supplierNames().get(2));
    }

    @Test
    void requireExistingSupplierIgnoresNullAndRejectsUnknownIds()
    {
        lookup.requireExistingSupplier(null);
        when(supplierRepository.existsById(9)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> lookup.requireExistingSupplier(9));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Supplier not found", exception.getMessage());
    }
}
