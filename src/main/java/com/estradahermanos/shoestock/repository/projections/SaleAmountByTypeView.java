package com.estradahermanos.shoestock.repository.projections;

public interface SaleAmountByTypeView
{
    String getType();

    Long getTotalAmount();

    Long getSaleCount();
}
