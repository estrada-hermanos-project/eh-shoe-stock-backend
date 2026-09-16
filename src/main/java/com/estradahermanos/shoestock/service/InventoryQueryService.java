package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ShoeStockMapper;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryQueryService
{
    private final ShoeRepository      shoeRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeStockMapper     shoeStockMapper;

    /** Consulta el stock de una variante por estilo, color y talla; incluye el nombre del zapato. */
    public StockQueryResponseDTO findStock(String shoeId, String color, Integer size)
    {
        log.info("Querying stock by shoe, color and size");
        try
        {
            ShoeStock variant = shoeStockRepository.findByShoeIdAndColorAndSize(shoeId, color, size)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Stock not found for the given shoe, color and size")
                            .build());

            Shoe shoe = shoeRepository.findByCode(shoeId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Shoe not found")
                            .build());

            return shoeStockMapper.toStockQuery(variant, shoe.getName());
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query stock", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query stock")
                    .build();
        }
    }
}
