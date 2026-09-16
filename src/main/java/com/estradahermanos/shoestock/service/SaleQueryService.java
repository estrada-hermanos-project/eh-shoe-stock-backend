package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.response.SaleResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.SaleMapper;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.SaleRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleQueryService
{
    private final SaleRepository      saleRepository;
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeRepository      shoeRepository;
    private final SaleMapper          saleMapper;

    /** Historial de ventas entre dos fechas, inclusive. */
    public List<SaleResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate)
    {
        log.info("Querying sales by date range");
        try
        {
            if (startDate.isAfter(endDate))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("start_date must not be after end_date")
                        .build();
            }
            return mapSales(saleRepository.findBySaleDateBetween(startDate, endDate));
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query sales by date range", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query sales")
                    .build();
        }
    }

    /** Historial de ventas de una fecha especifica. */
    public List<SaleResponseDTO> findByDate(LocalDate date)
    {
        log.info("Querying sales by date");
        try
        {
            return mapSales(saleRepository.findBySaleDate(date));
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to query sales by date", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not query sales")
                    .build();
        }
    }

    /** Resuelve name y color con mapas para no disparar N+1. */
    private List<SaleResponseDTO> mapSales(List<Sale> sales)
    {
        if (sales.isEmpty())
        {
            return List.of();
        }

        List<Integer> stockIds = sales.stream()
                .map(Sale::getShoeStockId)
                .distinct()
                .toList();

        Map<Integer, ShoeStock> variants = shoeStockRepository.findAllById(stockIds).stream()
                .collect(Collectors.toMap(ShoeStock::getId, variant -> variant));

        List<String> shoeIds = variants.values().stream()
                .map(ShoeStock::getShoeId)
                .distinct()
                .toList();

        Map<String, String> shoeNames = shoeRepository.findAllById(shoeIds).stream()
                .collect(Collectors.toMap(Shoe::getCode, Shoe::getName));

        return sales.stream()
                .map(sale ->
                {
                    ShoeStock variant = variants.get(sale.getShoeStockId());
                    String    color   = variant != null ? variant.getColor() : null;
                    String    name    = variant != null ? shoeNames.get(variant.getShoeId()) : null;
                    return saleMapper.toResponse(sale, name, color);
                })
                .toList();
    }
}
