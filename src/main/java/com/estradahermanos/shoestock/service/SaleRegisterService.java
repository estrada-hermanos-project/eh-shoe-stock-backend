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
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.InventoryValidator;
import com.estradahermanos.shoestock.utilities.InventoryVariantLookup;
import com.estradahermanos.shoestock.utilities.SaleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleRegisterService
{
    private final SaleRepository           saleRepository;
    private final SaleValidator            saleValidator;
    private final InventoryValidator       inventoryValidator;
    private final InventoryVariantLookup   inventoryVariantLookup;
    private final InventoryDecreaseService inventoryDecreaseService;
    private final SaleMapper               saleMapper;
    private final BusinessDate             businessDate;

    /** Registra una venta, descuenta stock y devuelve el detalle de la venta. */
    @Transactional
    public SaleResponseDTO register(CreateSaleRequestDTO request)
    {
        log.info("Registering sale");
        try
        {
            if (Objects.isNull(request))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }

            saleValidator.validateCreate(request.getName(), request.getColor());
            inventoryValidator.validateRegister(request.getStock(), request.getSize());

            ShoeStock variant = inventoryVariantLookup
                    .findVariant(request.getName(), request.getColor(), request.getSize());

            inventoryDecreaseService.decrease(RegisterStockRequestDTO.builder()
                    .shoeId(variant.getShoeId())
                    .color(request.getColor())
                    .size(request.getSize())
                    .stock(request.getStock())
                    .build());

            Sale saved = saleRepository.save(Sale.builder()
                    .shoeStockId(variant.getId())
                    .amount(request.getStock())
                    .size(request.getSize())
                    .saleDate(businessDate.today())
                    .build());

            log.info("Sale registered");
            return saleMapper.toResponse(saved, request.getName(), request.getColor());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to register sale", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not register sale")
                    .build();
        }
    }
}
