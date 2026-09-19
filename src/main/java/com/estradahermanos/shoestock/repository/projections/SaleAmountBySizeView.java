package com.estradahermanos.shoestock.repository.projections;

public interface SaleAmountBySizeView
{
    Integer getSize();

    String getType();

    Long getTotalAmount();
}
