package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.request.CreateSaleRequestDTO;
import com.estradahermanos.shoestock.dto.request.RegisterStockRequestDTO;
import com.estradahermanos.shoestock.dto.response.SaleResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SaleMapper;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.utilities.BusinessDate;
import com.estradahermanos.shoestock.utilities.InventoryValidator;
import com.estradahermanos.shoestock.utilities.InventoryVariantLookup;
import com.estradahermanos.shoestock.utilities.SaleValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleRegisterServiceTest
{
    @Mock
    private SaleRepository saleRepository;

    @Mock
    private InventoryVariantLookup inventoryVariantLookup;

    @Mock
    private InventoryDecreaseService inventoryDecreaseService;

    @Mock
    private SaleMapper saleMapper;

    private SaleRegisterService service;

    private static final LocalDate BUSINESS_DAY = LocalDate.of(2026, 9, 29);

    @BeforeEach
    void setUp()
    {
        service = serviceAt(LocalTime.of(10, 0));
    }

    private SaleRegisterService serviceAt(LocalTime guatemalaTime)
    {
        Clock clock = Clock.fixed(
                BUSINESS_DAY.atTime(guatemalaTime).atZone(BusinessDate.ZONE).toInstant(),
                BusinessDate.ZONE);
        return new SaleRegisterService(
                saleRepository,
                new SaleValidator(),
                new InventoryValidator(),
                inventoryVariantLookup,
                inventoryDecreaseService,
                saleMapper,
                new BusinessDate(clock));
    }

    private CreateSaleRequestDTO request()
    {
        return CreateSaleRequestDTO.builder()
                .size(40)
                .name("Oxford clasico")
                .color("Negro")
                .stock(2)
                .build();
    }

    private ShoeStock variant()
    {
        return ShoeStock.builder().id(7).shoeId("OXF-001").color("Negro").size(40).stock(10).minStock(0).build();
    }

    @Test
    void registerSavesSaleAndDecreasesStock()
    {
        when(inventoryVariantLookup.findVariant("Oxford clasico", "Negro", 40)).thenReturn(variant());
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleMapper.toResponse(any(), eq("Oxford clasico"), eq("Negro")))
                .thenReturn(SaleResponseDTO.builder()
                        .size(40)
                        .name("Oxford clasico")
                        .color("Negro")
                        .stock(2)
                        .saleDate(BUSINESS_DAY)
                        .build());

        SaleResponseDTO result = service.register(request());

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(saleCaptor.capture());
        Sale saved = saleCaptor.getValue();
        assertEquals(7, saved.getShoeStockId());
        assertEquals(2, saved.getAmount());
        assertEquals(40, saved.getSize());
        assertEquals(BUSINESS_DAY, saved.getSaleDate());

        ArgumentCaptor<RegisterStockRequestDTO> decreaseCaptor = ArgumentCaptor.forClass(RegisterStockRequestDTO.class);
        verify(inventoryDecreaseService).decrease(decreaseCaptor.capture());
        assertEquals("OXF-001", decreaseCaptor.getValue().getShoeId());
        assertEquals(2, decreaseCaptor.getValue().getStock());

        assertEquals(2, result.getStock());
        assertEquals("Oxford clasico", result.getName());
    }

    @Test
    void registerKeepsGuatemalaDateAfterSixPm()
    {
        SaleRegisterService eveningService = serviceAt(LocalTime.of(18, 30));
        when(inventoryVariantLookup.findVariant("Oxford clasico", "Negro", 40)).thenReturn(variant());
        when(saleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(saleMapper.toResponse(any(), eq("Oxford clasico"), eq("Negro")))
                .thenReturn(SaleResponseDTO.builder().saleDate(BUSINESS_DAY).build());

        eveningService.register(request());

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(saleCaptor.capture());
        assertEquals(BUSINESS_DAY, saleCaptor.getValue().getSaleDate());
    }

    @Test
    void registerRejectsNullRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void registerRejectsBlankName()
    {
        CreateSaleRequestDTO request = request();
        request.setName("  ");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(inventoryVariantLookup, never()).findVariant(any(), any(), any());
    }

    @Test
    void registerRejectsStockBelowOne()
    {
        CreateSaleRequestDTO request = request();
        request.setStock(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(inventoryVariantLookup, never()).findVariant(any(), any(), any());
    }

    @Test
    void registerPropagatesMissingVariant()
    {
        when(inventoryVariantLookup.findVariant("Oxford clasico", "Negro", 40))
                .thenThrow(BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Some parameter is not being sent correctly: no matching stock")
                        .build());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request()));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        verify(inventoryDecreaseService, never()).decrease(any());
        verify(saleRepository, never()).save(any());
    }

    @Test
    void registerPropagatesInsufficientStock()
    {
        when(inventoryVariantLookup.findVariant("Oxford clasico", "Negro", 40)).thenReturn(variant());
        when(inventoryDecreaseService.decrease(any()))
                .thenThrow(BusinessException.builder()
                        .code(HttpStatus.CONFLICT)
                        .message("Insufficient stock to decrease")
                        .build());

        BusinessException exception = assertThrows(BusinessException.class, () -> service.register(request()));

        assertEquals(HttpStatus.CONFLICT, exception.getCode());
        verify(saleRepository, never()).save(any());
    }
}
