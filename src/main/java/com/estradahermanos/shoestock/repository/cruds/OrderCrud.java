package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OrderCrud extends JpaRepository<Order, String>
{
    List<Order> findByOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(
            LocalDate startDate, LocalDate endDate);

    List<Order> findByStatusOrderByOrderDeliveryDateAsc(OrderStatusEnum status);

    List<Order> findBySupplierOrderByOrderDeliveryDateAsc(Integer supplier);

    List<Order> findByStatusAndSupplierOrderByOrderDeliveryDateAsc(OrderStatusEnum status, Integer supplier);

    List<Order> findByStatusAndOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(
            OrderStatusEnum status, LocalDate startDate, LocalDate endDate);

    List<Order> findByStatusAndSupplierAndOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(
            OrderStatusEnum status, Integer supplier, LocalDate startDate, LocalDate endDate);
}
