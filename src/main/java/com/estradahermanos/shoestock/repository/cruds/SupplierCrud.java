package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierCrud extends JpaRepository<Supplier, Integer>
{
    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Integer id);
}
