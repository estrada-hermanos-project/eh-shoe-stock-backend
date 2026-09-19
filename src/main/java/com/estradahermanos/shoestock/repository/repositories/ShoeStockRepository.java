package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.ShoeStockCrud;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShoeStockRepository
{
    private final ShoeStockCrud shoeStockCrud;

    public ShoeStock save(ShoeStock shoeStock)
    {
        return shoeStockCrud.save(shoeStock);
    }

    public Optional<ShoeStock> findByShoeIdAndColorAndSize(String shoeId, String color, Integer size)
    {
        return shoeStockCrud.findByShoeIdAndColorAndSize(shoeId, color, size);
    }

    public List<ShoeStock> findByShoeNameAndColorAndSize(String name, String color, Integer size)
    {
        return shoeStockCrud.findByShoeNameAndColorAndSize(name, color, size);
    }

    public Optional<ShoeStock> findById(Integer id)
    {
        return shoeStockCrud.findById(id);
    }

    public List<ShoeStock> findAllById(Collection<Integer> ids)
    {
        return shoeStockCrud.findAllById(ids);
    }

    public List<ShoeStock> findAll()
    {
        return shoeStockCrud.findAll();
    }

    public List<ShoeStock> findLowStock()
    {
        return shoeStockCrud.findLowStock();
    }

    public List<ShoeStock> findByStock(Integer stock)
    {
        return shoeStockCrud.findByStock(stock);
    }
}
