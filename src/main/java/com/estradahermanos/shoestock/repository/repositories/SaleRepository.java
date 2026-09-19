package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.SaleCrud;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySizeView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySupplierView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByTypeView;
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

    public List<SaleAmountByStockView> sumAmountGroupedByStock(LocalDate startDate, LocalDate endDate)
    {
        return saleCrud.sumAmountGroupedByStock(startDate, endDate);
    }

    public List<LastSaleDateView> findLastSaleDateGrouped()
    {
        return saleCrud.findLastSaleDateGrouped();
    }

    public List<SaleAmountByTypeView> sumAmountGroupedByType(LocalDate startDate, LocalDate endDate)
    {
        return saleCrud.sumAmountGroupedByType(startDate, endDate);
    }

    public List<SaleAmountBySizeView> sumAmountGroupedBySize(LocalDate startDate, LocalDate endDate,
                                                             String type, String shoeCode)
    {
        return saleCrud.sumAmountGroupedBySize(startDate, endDate, type, shoeCode);
    }

    public List<SaleAmountBySupplierView> sumAmountGroupedBySupplier(LocalDate startDate, LocalDate endDate)
    {
        return saleCrud.sumAmountGroupedBySupplier(startDate, endDate);
    }
}
