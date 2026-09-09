package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.SupplierCrud;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SupplierRepository
{
    private final SupplierCrud supplierCrud;

    public Supplier save(Supplier supplier)
    {
        return supplierCrud.save(supplier);
    }

    public Optional<Supplier> findById(Integer id)
    {
        return supplierCrud.findById(id);
    }

    public List<Supplier> findAll()
    {
        return supplierCrud.findAll();
    }

    public boolean existsById(Integer id)
    {
        return supplierCrud.existsById(id);
    }

    public boolean existsByPhone(String phone)
    {
        return supplierCrud.existsByPhone(phone);
    }

    public boolean existsByPhoneAndIdNot(String phone, Integer id)
    {
        return supplierCrud.existsByPhoneAndIdNot(phone, id);
    }

    public void deleteById(Integer id)
    {
        supplierCrud.deleteById(id);
    }
}
