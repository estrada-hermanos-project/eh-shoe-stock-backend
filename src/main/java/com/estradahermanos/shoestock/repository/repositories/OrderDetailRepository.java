package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.OrderDetailCrud;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderDetailRepository
{
    private final OrderDetailCrud orderDetailCrud;

    public OrderDetail save(OrderDetail orderDetail)
    {
        return orderDetailCrud.save(orderDetail);
    }

    public List<OrderDetail> findByOrderId(String orderId)
    {
        return orderDetailCrud.findByOrderId(orderId);
    }

    public List<OrderDetail> findByOrderIdIn(Collection<String> orderIds)
    {
        return orderDetailCrud.findByOrderIdIn(orderIds);
    }
}
