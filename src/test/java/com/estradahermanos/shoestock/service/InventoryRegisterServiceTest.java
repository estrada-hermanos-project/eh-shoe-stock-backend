package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
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
class InventoryRegisterServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    @Mock
    private ShoeStockRepository shoeStockRepository;

    @Mock
    private ShoeStockMapper shoeStockMapper;

    private InventoryRegisterService service;

    @BeforeEach
    void setUp()
    {
        service = new InventoryRegisterService(shoeRepository, shoeStockRepository, shoeStockMapper, new InventoryValidator());
    }

    private RegisterStockRequestDTO request(int stock)
    {
        return RegisterStockRequestDTO.builder()
                .shoeId("OXF-001")
                .color("Negro")
                .size(40)
                .stock(stock)
                .build();
    }

    @Test
    void registerCreatesNewVariantWhenNotExists()
    {
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.empty());
        when(shoeStockRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(shoeStockMapper.toResponse(any())).thenReturn(ShoeStockResponseDTO.builder().stock(12).build());

        service.register(request(12));

        ArgumentCaptor<ShoeStock> captor = ArgumentCaptor.forClass(ShoeStock.class);
        verify(shoeStockRepository).save(captor.capture());
        assertEquals(12, captor.getValue().getStock());
    }

    @Test
    void registerIncrementsStockWhenVariantExists()
    {
        ShoeStock existing = ShoeStock.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(5).build();
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);
        when(shoeStockRepository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 40)).thenReturn(Optional.of(existing));
        when(shoeStockRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(shoeStockMapper.toResponse(any())).thenReturn(ShoeStockResponseDTO.builder().stock(8).build());

        service.register(request(3));

        ArgumentCaptor<ShoeStock> captor = ArgumentCaptor.forClass(ShoeStock.class);
        verify(shoeStockRepository).save(captor.capture());
        assertEquals(8, captor.getValue().getStock());
    }

    @Test
    void registerRejectsStockLessThanOne()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request(0)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(shoeRepository, never()).existsByCode(any());
    }

    @Test
    void registerRejectsMissingShoe()
    {
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request(5)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(shoeStockRepository, never()).save(any());
    }
}
