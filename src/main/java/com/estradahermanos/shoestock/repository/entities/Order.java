package com.estradahermanos.shoestock.repository.entities;

import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "`order`")
public class Order
{
    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "order_delivery_date", nullable = false)
    private LocalDate orderDeliveryDate;

    @Column(name = "supplier", nullable = false)
    private Integer supplier;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatusEnum status = OrderStatusEnum.PENDIENTE;
}
