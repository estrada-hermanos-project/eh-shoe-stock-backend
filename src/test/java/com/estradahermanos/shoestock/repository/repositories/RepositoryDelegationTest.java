package com.estradahermanos.shoestock.repository.repositories;

import com.estradahermanos.shoestock.repository.cruds.OrderCrud;
import com.estradahermanos.shoestock.repository.cruds.OrderDetailCrud;
import com.estradahermanos.shoestock.repository.cruds.SaleCrud;
import com.estradahermanos.shoestock.repository.cruds.ShoeCrud;
import com.estradahermanos.shoestock.repository.cruds.ShoeStockCrud;
import com.estradahermanos.shoestock.repository.cruds.SupplierCrud;
import com.estradahermanos.shoestock.repository.cruds.UserAccountCrud;
import com.estradahermanos.shoestock.repository.cruds.UserAdminCrud;
import com.estradahermanos.shoestock.repository.cruds.UserSessionCrud;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.entities.UserSession;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepositoryDelegationTest
{
    @Test
    void shoeRepositoryDelegatesToTheCrud()
    {
        ShoeCrud crud = mock(ShoeCrud.class);
        ShoeRepository repository = new ShoeRepository(crud);
        Shoe shoe = Shoe.builder().code("OXF-001").build();
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);
        when(crud.save(shoe)).thenReturn(shoe);
        when(crud.findById("OXF-001")).thenReturn(Optional.of(shoe));
        when(crud.findAll()).thenReturn(List.of(shoe));
        when(crud.findAllById(List.of("OXF-001"))).thenReturn(List.of(shoe));
        when(crud.findBySupplier(2)).thenReturn(List.of(shoe));
        when(crud.findByCreatedAtBetween(start, end)).thenReturn(List.of(shoe));
        when(crud.existsById("OXF-001")).thenReturn(true);

        assertSame(shoe, repository.save(shoe));
        assertEquals(shoe, repository.findByCode("OXF-001").orElseThrow());
        assertEquals(1, repository.findAll().size());
        assertEquals(1, repository.findAllById(List.of("OXF-001")).size());
        assertEquals(1, repository.findBySupplier(2).size());
        assertEquals(1, repository.findByCreatedAtBetween(start, end).size());
        assertTrue(repository.existsByCode("OXF-001"));
        repository.deleteByCode("OXF-001");
        verify(crud).deleteById("OXF-001");
    }

    @Test
    void shoeStockRepositoryDelegatesToTheCrud()
    {
        ShoeStockCrud crud = mock(ShoeStockCrud.class);
        ShoeStockRepository repository = new ShoeStockRepository(crud);
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(1).build();
        when(crud.save(variant)).thenReturn(variant);
        when(crud.findByShoeIdAndColorAndSize("OXF-001", "Negro", 42)).thenReturn(Optional.of(variant));
        when(crud.findByShoeNameAndColorAndSize("Oxford", "Negro", 42)).thenReturn(List.of(variant));
        when(crud.findById(15)).thenReturn(Optional.of(variant));
        when(crud.findAllById(List.of(15))).thenReturn(List.of(variant));
        when(crud.findAll()).thenReturn(List.of(variant));
        when(crud.findLowStock()).thenReturn(List.of(variant));
        when(crud.findByStock(0)).thenReturn(List.of(variant));

        assertSame(variant, repository.save(variant));
        assertEquals(variant, repository.findByShoeIdAndColorAndSize("OXF-001", "Negro", 42).orElseThrow());
        assertEquals(1, repository.findByShoeNameAndColorAndSize("Oxford", "Negro", 42).size());
        assertEquals(variant, repository.findById(15).orElseThrow());
        assertEquals(1, repository.findAllById(List.of(15)).size());
        assertEquals(1, repository.findAll().size());
        assertEquals(1, repository.findLowStock().size());
        assertEquals(1, repository.findByStock(0).size());
    }

    @Test
    void saleRepositoryDelegatesToTheCrud()
    {
        SaleCrud crud = mock(SaleCrud.class);
        SaleRepository repository = new SaleRepository(crud);
        Sale sale = Sale.builder().id(1).shoeStockId(15).amount(2).size(42).saleDate(LocalDate.of(2026, 9, 2)).build();
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);
        when(crud.save(sale)).thenReturn(sale);
        when(crud.findBySaleDateBetween(start, end)).thenReturn(List.of(sale));
        when(crud.findBySaleDate(start)).thenReturn(List.of(sale));
        when(crud.sumAmountGroupedByStock(start, end)).thenReturn(List.of());
        when(crud.findLastSaleDateGrouped()).thenReturn(List.of());
        when(crud.sumAmountGroupedByType(start, end)).thenReturn(List.of());
        when(crud.sumAmountGroupedBySize(start, end, "Caballero", "OXF-001")).thenReturn(List.of());
        when(crud.sumAmountGroupedBySupplier(start, end)).thenReturn(List.of());

        assertSame(sale, repository.save(sale));
        assertEquals(1, repository.findBySaleDateBetween(start, end).size());
        assertEquals(1, repository.findBySaleDate(start).size());
        assertTrue(repository.sumAmountGroupedByStock(start, end).isEmpty());
        assertTrue(repository.findLastSaleDateGrouped().isEmpty());
        assertTrue(repository.sumAmountGroupedByType(start, end).isEmpty());
        assertTrue(repository.sumAmountGroupedBySize(start, end, "Caballero", "OXF-001").isEmpty());
        assertTrue(repository.sumAmountGroupedBySupplier(start, end).isEmpty());
    }

    @Test
    void orderRepositoriesDelegateToTheCrud()
    {
        OrderCrud orderCrud = mock(OrderCrud.class);
        OrderDetailCrud detailCrud = mock(OrderDetailCrud.class);
        OrderRepository orders = new OrderRepository(orderCrud);
        OrderDetailRepository details = new OrderDetailRepository(detailCrud);
        Order order = Order.builder().id("ORDEN-1").supplier(2).status(OrderStatusEnum.PENDIENTE).build();
        OrderDetail detail = OrderDetail.builder().id(1).orderId("ORDEN-1").shoeStockId(15).amount(2).build();
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);
        when(orderCrud.save(order)).thenReturn(order);
        when(orderCrud.findById("ORDEN-1")).thenReturn(Optional.of(order));
        when(orderCrud.existsById("ORDEN-1")).thenReturn(true);
        when(orderCrud.findByOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(start, end)).thenReturn(List.of(order));
        when(orderCrud.findByStatusOrderByOrderDeliveryDateAsc(OrderStatusEnum.PENDIENTE)).thenReturn(List.of(order));
        when(orderCrud.findBySupplierOrderByOrderDeliveryDateAsc(2)).thenReturn(List.of(order));
        when(orderCrud.findAll()).thenReturn(List.of(order));
        when(orderCrud.findByStatusAndSupplierOrderByOrderDeliveryDateAsc(OrderStatusEnum.PENDIENTE, 2))
                .thenReturn(List.of(order));
        when(orderCrud.findByStatusAndOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(
                OrderStatusEnum.RECIBIDA, start, end)).thenReturn(List.of(order));
        when(orderCrud.findByStatusAndSupplierAndOrderDeliveryDateBetweenOrderByOrderDeliveryDateAsc(
                OrderStatusEnum.RECIBIDA, 2, start, end)).thenReturn(List.of(order));
        when(detailCrud.save(detail)).thenReturn(detail);
        when(detailCrud.findByOrderId("ORDEN-1")).thenReturn(List.of(detail));
        when(detailCrud.findByOrderIdIn(List.of("ORDEN-1"))).thenReturn(List.of(detail));

        assertSame(order, orders.save(order));
        assertEquals(order, orders.findById("ORDEN-1").orElseThrow());
        assertTrue(orders.existsById("ORDEN-1"));
        assertEquals(1, orders.findByOrderDeliveryDateBetween(start, end).size());
        assertEquals(1, orders.findByStatus(OrderStatusEnum.PENDIENTE).size());
        assertEquals(1, orders.findBySupplier(2).size());
        assertEquals(1, orders.findAll().size());
        assertEquals(1, orders.findByStatusAndSupplier(OrderStatusEnum.PENDIENTE, 2).size());
        assertEquals(1, orders.findByStatusAndDateRange(OrderStatusEnum.RECIBIDA, start, end).size());
        assertEquals(1, orders.findByStatusAndSupplierAndDateRange(OrderStatusEnum.RECIBIDA, 2, start, end).size());
        assertSame(detail, details.save(detail));
        assertEquals(1, details.findByOrderId("ORDEN-1").size());
        assertEquals(1, details.findByOrderIdIn(List.of("ORDEN-1")).size());
    }

    @Test
    void userRepositoriesDelegateToTheCrud()
    {
        SupplierCrud supplierCrud = mock(SupplierCrud.class);
        UserAdminCrud adminCrud = mock(UserAdminCrud.class);
        UserAccountCrud accountCrud = mock(UserAccountCrud.class);
        UserSessionCrud sessionCrud = mock(UserSessionCrud.class);
        SupplierRepository suppliers = new SupplierRepository(supplierCrud);
        UserAdminRepository admins = new UserAdminRepository(adminCrud);
        UserAccountRepository accounts = new UserAccountRepository(accountCrud);
        UserSessionRepository sessions = new UserSessionRepository(sessionCrud);
        Supplier supplier = Supplier.builder().id(2).fullName("Maria Lopez").phone("555").build();
        UserAdmin admin = UserAdmin.builder().dpi("123").fullName("Diego").phone("555").email("a@b.com").build();
        UserAccount account = UserAccount.builder().username("diego").passwordHash("hash").dpi("123").build();
        UserSession session = UserSession.builder()
                .id(1)
                .sessionCode("Ab12C")
                .userAccountId("diego")
                .creationDate(LocalDateTime.of(2026, 9, 2, 8, 0))
                .expirationDate(LocalDateTime.of(2026, 9, 2, 9, 0))
                .build();
        when(supplierCrud.save(supplier)).thenReturn(supplier);
        when(supplierCrud.findById(2)).thenReturn(Optional.of(supplier));
        when(supplierCrud.findAll()).thenReturn(List.of(supplier));
        when(supplierCrud.existsById(2)).thenReturn(true);
        when(supplierCrud.existsByPhone("555")).thenReturn(true);
        when(supplierCrud.existsByPhoneAndIdNot("555", 2)).thenReturn(false);
        when(adminCrud.save(admin)).thenReturn(admin);
        when(adminCrud.findById("123")).thenReturn(Optional.of(admin));
        when(adminCrud.findAll()).thenReturn(List.of(admin));
        when(adminCrud.existsById("123")).thenReturn(true);
        when(accountCrud.save(account)).thenReturn(account);
        when(accountCrud.findById("diego")).thenReturn(Optional.of(account));
        when(accountCrud.existsById("diego")).thenReturn(true);
        when(sessionCrud.save(session)).thenReturn(session);

        assertSame(supplier, suppliers.save(supplier));
        assertEquals(supplier, suppliers.findById(2).orElseThrow());
        assertEquals(1, suppliers.findAll().size());
        assertTrue(suppliers.existsById(2));
        assertTrue(suppliers.existsByPhone("555"));
        assertTrue(!suppliers.existsByPhoneAndIdNot("555", 2));
        suppliers.deleteById(2);
        verify(supplierCrud).deleteById(2);
        assertSame(admin, admins.save(admin));
        assertEquals(admin, admins.findByDpi("123").orElseThrow());
        assertEquals(1, admins.findAll().size());
        assertTrue(admins.existsByDpi("123"));
        admins.deleteByDpi("123");
        verify(adminCrud).deleteById("123");
        assertSame(account, accounts.save(account));
        assertEquals(account, accounts.findByUsername("diego").orElseThrow());
        assertTrue(accounts.existsByUsername("diego"));
        assertSame(session, sessions.save(session));
    }
}
