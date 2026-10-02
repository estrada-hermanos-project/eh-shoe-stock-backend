package com.estradahermanos.shoestock.mapper;

import com.estradahermanos.shoestock.dto.request.CreateShoeRequestDTO;
import com.estradahermanos.shoestock.dto.request.CreateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.request.CreateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateSupplierRequestDTO;
import com.estradahermanos.shoestock.dto.request.UpdateUserAdminRequestDTO;
import com.estradahermanos.shoestock.dto.response.OrderDetailResponseDTO;
import com.estradahermanos.shoestock.repository.entities.Order;
import com.estradahermanos.shoestock.repository.entities.OrderDetail;
import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.entities.UserAccount;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.utilities.OrderStatusEnum;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapperCoverageTest
{
    @Test
    void mapsSalesReports()
    {
        ReportSalesMapper mapper = Mappers.getMapper(ReportSalesMapper.class);
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(4).build();
        Sale sale = Sale.builder().id(1).shoeStockId(15).amount(3).size(42).saleDate(LocalDate.of(2026, 9, 2)).build();

        assertEquals(15, mapper.toRank(variant, "Oxford", 8L, 4, LocalDate.of(2026, 9, 2)).getShoeStockId());
        assertEquals("Oxford", mapper.toPeriodSale(sale, "Oxford", "Negro").getShoeName());
    }

    @Test
    void mapsInventoryReports()
    {
        ReportInventoryMapper mapper = Mappers.getMapper(ReportInventoryMapper.class);
        ShoeStock variant = ShoeStock.builder().id(15).color("Negro").size(42).stock(1).minStock(4).build();

        assertEquals("Oxford", mapper.toStatus(variant, "Oxford").getShoeName());
        assertEquals(3, mapper.toLowStock(variant, "Oxford", 3, "Maria Lopez").getPairsBelowMin());
    }

    @Test
    void mapsMerchandiseAndOrders()
    {
        ReportMerchandiseMapper merchandiseMapper = Mappers.getMapper(ReportMerchandiseMapper.class);
        OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
        OrderDetailMapper detailMapper = Mappers.getMapper(OrderDetailMapper.class);
        Order order = Order.builder()
                .id("ORDEN-1")
                .supplier(2)
                .orderDeliveryDate(LocalDate.of(2026, 9, 2))
                .status(OrderStatusEnum.PENDIENTE)
                .build();
        OrderDetail detail = OrderDetail.builder().id(1).orderId("ORDEN-1").shoeStockId(15).amount(4).build();
        OrderDetailResponseDTO detailDto = detailMapper.toResponse(detail, "Oxford");

        assertEquals("Oxford", detailDto.getShoeName());
        assertEquals(4, merchandiseMapper.toPending(order, "Maria Lopez", 3L, 4, List.of(detailDto)).getTotalPairs());
        assertEquals("ORDEN-1", merchandiseMapper.toReceived(order, "Maria Lopez", 4, List.of(detailDto)).getOrderId());
        assertEquals(OrderStatusEnum.PENDIENTE, orderMapper.toResponse(order, "Maria Lopez", List.of(detailDto)).getStatus());
    }

    @Test
    void mapsShoesSuppliersSalesAndStock()
    {
        ShoeMapper shoeMapper = Mappers.getMapper(ShoeMapper.class);
        SupplierMapper supplierMapper = Mappers.getMapper(SupplierMapper.class);
        SaleMapper saleMapper = Mappers.getMapper(SaleMapper.class);
        ShoeStockMapper stockMapper = Mappers.getMapper(ShoeStockMapper.class);
        Shoe shoe = shoeMapper.toEntity(CreateShoeRequestDTO.builder()
                .code("OXF-001")
                .type("Caballero")
                .name("Oxford")
                .description("Clasico")
                .supplierId(2)
                .build());
        Supplier supplier = Supplier.builder().id(2).fullName("Maria Lopez").phone("555").build();
        ShoeStock variant = ShoeStock.builder().id(15).shoeId("OXF-001").color("Negro").size(42).stock(4).minStock(1).build();
        Sale sale = Sale.builder().id(1).shoeStockId(15).amount(3).size(42).saleDate(LocalDate.of(2026, 9, 2)).build();

        assertEquals(2, shoe.getSupplier());
        assertNull(shoe.getCreatedAt());
        assertEquals("Maria Lopez", shoeMapper.toResponse(shoe, "Maria Lopez").getSupplierName());
        assertEquals("Maria Lopez", supplierMapper.toResponse(supplier).getName());
        assertEquals("Maria Lopez", supplierMapper.toResponseList(List.of(supplier)).get(0).getName());
        assertNull(supplierMapper.toEntity(CreateSupplierRequestDTO.builder().fullName("Ana").phone("111").build()).getId());
        supplierMapper.updateEntity(UpdateSupplierRequestDTO.builder().fullName("Ana Ruiz").phone("222").build(), supplier);
        assertEquals("Ana Ruiz", supplier.getFullName());
        assertEquals(3, saleMapper.toResponse(sale, "Oxford", "Negro").getStock());
        assertEquals(15, stockMapper.toResponse(variant).getId());
        assertEquals("Oxford", stockMapper.toStockQuery(variant, "Oxford").getName());
    }

    @Test
    void mapsUsersAndTreatsBlankEmailAsNull()
    {
        UserAdminMapper adminMapper = Mappers.getMapper(UserAdminMapper.class);
        UserAccountMapper accountMapper = Mappers.getMapper(UserAccountMapper.class);
        UserAdmin admin = UserAdmin.builder().dpi("123").fullName("Diego Estrada").phone("555").email("diego@mail.com").build();

        assertEquals("Diego Estrada", adminMapper.toResponse(admin).getName());
        assertEquals(1, adminMapper.toResponseList(List.of(admin)).size());
        assertNull(adminMapper.toEntity(CreateUserAdminRequestDTO.builder()
                .dpi("123")
                .fullName("Diego")
                .phone("555")
                .email("   ")
                .build()).getEmail());
        assertEquals("diego@mail.com", adminMapper.toEntity(CreateUserAdminRequestDTO.builder()
                .dpi("123")
                .fullName("Diego")
                .phone("555")
                .email("  diego@mail.com  ")
                .build()).getEmail());
        assertNull(adminMapper.blankToNull(null));

        UserAdmin target = UserAdmin.builder().dpi("123").email("old@mail.com").build();
        adminMapper.updateEntity(UpdateUserAdminRequestDTO.builder()
                .fullName("Diego Estrada")
                .phone("555")
                .email("")
                .build(), target);
        assertEquals("123", target.getDpi());
        assertNull(target.getEmail());

        assertEquals("diego", accountMapper.toResponse(UserAccount.builder()
                .username("diego")
                .passwordHash("hash")
                .dpi("123")
                .build()).getUsername());
    }
}
