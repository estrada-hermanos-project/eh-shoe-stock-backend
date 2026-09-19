package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.Shoe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShoeCrud extends JpaRepository<Shoe, String>
{
    List<Shoe> findBySupplier(Integer supplier);

    List<Shoe> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate);
}
