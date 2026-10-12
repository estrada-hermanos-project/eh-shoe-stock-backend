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

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryStyleQueryService
{
    private final ShoeRepository      shoeRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeStockMapper     shoeStockMapper;

    /** Lista color, talla y pares de un estilo. Color y talla son opcionales y reducen la lista. */
    public List<StockQueryResponseDTO> findByShoeCode(String shoeId, String color, Integer size)
    {
        log.info("Querying stock variants by shoe code");
        try
        {
            Shoe shoe = shoeRepository.findByCode(shoeId)
                    .orElseThrow(() -> BusinessException.builder()
                            .code(HttpStatus.NOT_FOUND)
                            .message("Shoe not found")
                            .build());
            return variantsOf(shoe.getCode(), normalizeColor(color), size).stream()
                    .map(variant -> shoeStockMapper.toStockQuery(variant, shoe.getName()))
                    .toList();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query stock by shoe code", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query stock")
                    .build();
        }
    }

    private List<ShoeStock> variantsOf(
            String shoeId, String color, Integer size)
    {
        if (!Objects.isNull(color) && !Objects.isNull(size))
        {
            return shoeStockRepository.findByShoeIdAndColorAndSize(shoeId, color, size)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        if (!Objects.isNull(color))
        {
            return shoeStockRepository.findByShoeIdAndColor(shoeId, color);
        }
        if (!Objects.isNull(size))
        {
            return shoeStockRepository.findByShoeIdAndSize(shoeId, size);
        }
        return shoeStockRepository.findByShoeId(shoeId);
    }

    private String normalizeColor(String color)
    {
        if (Objects.isNull(color) || color.isBlank())
        {
            return null;
        }
        return color.trim();
    }
}
