package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.ShoeCrud;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShoeRepository
{
    private final ShoeCrud shoeCrud;

    public Shoe save(Shoe shoe)
    {
        return shoeCrud.save(shoe);
    }

    public Optional<Shoe> findByCode(String code)
    {
        return shoeCrud.findById(code);
    }

    public List<Shoe> findAll()
    {
        return shoeCrud.findAll();
    }

    public List<Shoe> findAllById(Collection<String> codes)
    {
        return shoeCrud.findAllById(codes);
    }

    public List<Shoe> findBySupplier(Integer supplier)
    {
        return shoeCrud.findBySupplier(supplier);
    }

    public boolean existsByCode(String code)
    {
        return shoeCrud.existsById(code);
    }

    public void deleteByCode(String code)
    {
        shoeCrud.deleteById(code);
    }
}
