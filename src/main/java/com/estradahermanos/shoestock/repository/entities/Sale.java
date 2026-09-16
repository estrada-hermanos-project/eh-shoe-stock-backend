package com.estradahermanos.shoestock.repository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale")
public class Sale
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "shoe_stock_id", nullable = false)
    private Integer shoeStockId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "size", nullable = false)
    private Integer size;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;
}
