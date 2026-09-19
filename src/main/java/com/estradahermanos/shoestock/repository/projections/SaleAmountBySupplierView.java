package com.estradahermanos.shoestock.repository.projections;

public interface SaleAmountBySupplierView
{
    Integer getSupplierId();

    Long getTotalAmount();

    Long getStylesSold();
}
