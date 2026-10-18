package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateVariantRequestDTO;
import com.estradahermanos.shoestock.dto.response.ShoeStockResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.InventoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryVariantCreateServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeStockMapper shoeStockMapper;

    private InventoryVariantCreateService service;

    @BeforeEach
    void setUp()
    {
        service = new InventoryVariantCreateService(
                shoeRepository, shoeStockRepository, shoeStockMapper, new InventoryValidator());
    }

    private CreateVariantRequestDTO request()
    {
        return CreateVariantRequestDTO.builder().shoeId("OXF-001").color("Negro").size(40).build();
    }

    @Test
    void createRegistersVariantWithZeroStock()
    {
        ShoeStock created = ShoeStock.builder().shoeId("OXF-001").color("Negro").size(40).stock(0).minStock(0).build();
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.empty());
        when(shoeStockMapper.toNewVariant(any())).thenReturn(created);
        when(shoeStockRepository.save(created)).thenReturn(created);
        when(shoeStockMapper.toResponse(created)).thenReturn(
                ShoeStockResponseDTO.builder().shoeId("OXF-001").color("Negro").size(40).stock(0).build());

        ShoeStockResponseDTO response = service.create(request());

        ArgumentCaptor<ShoeStock> captor = ArgumentCaptor.forClass(ShoeStock.class);
        verify(shoeStockRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getStock());
        assertEquals(0, response.getStock());
    }

    @Test
    void createRejectsExistingVariantWithoutChangingStock()
    {
        ShoeStock existing = ShoeStock.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(5).build();
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.of(existing));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request()));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        assertEquals("A variant with this color and size already exists", exception.getMessage());
        assertEquals(5, existing.getStock());
        verify(shoeStockRepository, never()).save(any());
    }

    @Test
    void createRejectsMissingShoe()
    {
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(request()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(shoeStockRepository, never()).save(any());
    }

    @Test
    void createRejectsBlankColor()
    {
        CreateVariantRequestDTO blank = request();
        blank.setColor(" ");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(blank));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(shoeRepository, never()).existsByCode(any());
    }
}
