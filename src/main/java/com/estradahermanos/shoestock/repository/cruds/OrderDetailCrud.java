package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderDetailCrud extends JpaRepository<OrderDetail, Integer>
{
    List<OrderDetail> findByOrderId(String orderId);

    List<OrderDetail> findByOrderIdIn(Collection<String> orderIds);
}
