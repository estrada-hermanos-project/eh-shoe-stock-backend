package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.OrderCrud;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository
{
    private final OrderCrud orderCrud;

    public Order save(Order order)
    {
        return orderCrud.save(order);
    }

    public Optional<Order> findById(String id)
    {
        return orderCrud.findById(id);
    }

    public boolean existsById(String id)
    {
        return orderCrud.existsById(id);
    }

    public List<Order> findByOrderDeliveryDateBetween(LocalDate startDate, LocalDate endDate)
    {
        return orderCrud.findByOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(startDate, endDate);
    }

    public List<Order> findByStatus(OrderStatusEnum status)
    {
        return orderCrud.findByStatusOrderByOrderDeliveryDateAsc(status);
    }

    public List<Order> findBySupplier(Integer supplierId)
    {
        return orderCrud.findBySupplierOrderByOrderDeliveryDateAsc(supplierId);
    }
}
