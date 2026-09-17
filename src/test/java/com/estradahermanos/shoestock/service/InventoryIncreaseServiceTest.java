package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
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
class InventoryIncreaseServiceTest
{
    @Mock
    private ShoeStockRepository shoeStockRepository;

    private InventoryIncreaseService service;

    @BeforeEach
    void setUp()
    {
        service = new InventoryIncreaseService(shoeStockRepository, new InventoryValidator());
    }

    private ShoeStock variant(int stock)
    {
        return ShoeStock.builder()
                .id(15)
                .shoeId("OXF-001")
                .color("Negro")
                .size(40)
                .stock(stock)
                .minStock(0)
                .build();
    }

    @Test
    void increaseAddsAmountToExistingVariant()
    {
        when(shoeStockRepository.findById(15)).thenReturn(Optional.of(variant(3)));
        when(shoeStockRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.increaseByStockId(15, 4);

        ArgumentCaptor<ShoeStock> captor = ArgumentCaptor.forClass(ShoeStock.class);
        verify(shoeStockRepository).save(captor.capture());
        assertEquals(7, captor.getValue().getStock());
    }

    @Test
    void increaseRejectsMissingVariant()
    {
        when(shoeStockRepository.findById(15)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.increaseByStockId(15, 4));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        assertEquals("Stock not found", exception.getMessage());
        verify(shoeStockRepository, never()).save(any());
    }

    @Test
    void increaseRejectsAmountBelowOne()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.increaseByStockId(15, 0));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(shoeStockRepository, never()).findById(any());
    }
}
