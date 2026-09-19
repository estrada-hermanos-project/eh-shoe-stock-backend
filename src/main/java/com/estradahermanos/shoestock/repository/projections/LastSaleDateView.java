package com.estradahermanos.shoestock.repository.projections;

import java.time.LocalDate;

public interface LastSaleDateView
{
    Integer getShoeStockId();

    LocalDate getLastSaleDate();
}
