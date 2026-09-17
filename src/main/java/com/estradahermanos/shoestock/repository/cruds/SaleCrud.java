package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SaleCrud extends JpaRepository<Sale, Integer>
{
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    List<Sale> findBySaleDate(LocalDate saleDate);
}
