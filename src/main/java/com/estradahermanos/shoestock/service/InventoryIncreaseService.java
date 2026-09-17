package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.InventoryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryIncreaseService
{
    private final ShoeStockRepository shoeStockRepository;
    private final InventoryValidator  inventoryValidator;

    /** Suma amount al stock de la variante. Pensado para el modulo de Mercaderia. */
    @Transactional
    public void increaseByStockId(Integer shoeStockId, Integer amount)
    {
        log.info("Increasing stock by id");
        try
        {
            inventoryValidator.validateAmount(amount);

            ShoeStock variant = shoeStockRepository.findById(shoeStockId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Stock not found")
                            .build());

            variant.setStock(variant.getStock() + amount);
            shoeStockRepository.save(variant);
            log.info("Stock increased");
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to increase stock", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not increase stock")
                    .build();
        }
    }
}
