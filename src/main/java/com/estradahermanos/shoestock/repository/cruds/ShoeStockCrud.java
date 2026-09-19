package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShoeStockCrud extends JpaRepository<ShoeStock, Integer>
{
    Optional<ShoeStock> findByShoeIdAndColorAndSize(String shoeId, String color, Integer size);

    @Query("""
           SELECT ss FROM ShoeStock ss, Shoe s
            WHERE ss.shoeId = s.code
              AND s.name = :name
              AND ss.color = :color
              AND ss.size = :size
           """)
    List<ShoeStock> findByShoeNameAndColorAndSize(@Param("name") String name,
                                                  @Param("color") String color,
                                                  @Param("size") Integer size);

    @Query("SELECT ss FROM ShoeStock ss WHERE ss.stock <= ss.minStock")
    List<ShoeStock> findLowStock();

    List<ShoeStock> findByStock(Integer stock);
}
