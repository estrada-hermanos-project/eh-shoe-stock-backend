package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoeStockCrud extends JpaRepository<ShoeStock, Integer>
{
    Optional<ShoeStock> findByShoeIdAndColorAndSize(String shoeId, String color, Integer size);
}
