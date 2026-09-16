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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shoe_stock")
public class ShoeStock
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "shoe_id", length = 50, nullable = false)
    private String shoeId;

    @Column(name = "color", length = 30, nullable = false)
    private String color;

    @Column(name = "size", nullable = false)
    private Integer size;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock;
}
