package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.SaleCrud;
import com.estradahermanos.shoestock.repository.entities.Sale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SaleRepository
{
    private final SaleCrud saleCrud;

    public Sale save(Sale sale)
    {
        return saleCrud.save(sale);
    }

    public List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate)
    {
        return saleCrud.findBySaleDateBetween(startDate, endDate);
    }

    public List<Sale> findBySaleDate(LocalDate saleDate)
    {
        return saleCrud.findBySaleDate(saleDate);
    }
}
