package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.Shoe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoeCrud extends JpaRepository<Shoe, String>
{
    List<Shoe> findBySupplier(Integer supplier);
}
