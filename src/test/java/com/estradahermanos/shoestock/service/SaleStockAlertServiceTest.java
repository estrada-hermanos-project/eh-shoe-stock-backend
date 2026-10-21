package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.StockAlertMailMapper;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleStockAlertServiceTest
{
    @Mock
    private InventoryQueryService inventoryQueryService;

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private MailSendService mailSendService;

    private SaleStockAlertService service;

    @BeforeEach
    void setUp()
    {
        service = new SaleStockAlertService(
                inventoryQueryService,
                userAdminRepository,
                mailSendService,
                Mappers.getMapper(StockAlertMailMapper.class));
    }

    @Test
    void sendsLowStockMailWhenThreeOrFourPairsRemain()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(3), stock(4));
        when(userAdminRepository.findWithEmail()).thenReturn(List.of(admin("ana@gmail.com")));

        service.notifyAfterSale("OXF-001", "Negro", 40);
        service.notifyAfterSale("OXF-001", "Negro", 40);

        List<MailMessage> messages = captured(2);
        assertAlert(messages.get(0), "Stock bajo", "El stock es bajo y se recomienda realizar un pedido.", 3);
        assertAlert(messages.get(1), "Stock bajo", "El stock es bajo y se recomienda realizar un pedido.", 4);
    }

    @Test
    void sendsRunningOutMailWhenOneOrTwoPairsRemain()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(1), stock(2));
        when(userAdminRepository.findWithEmail()).thenReturn(List.of(admin("ana@gmail.com")));

        service.notifyAfterSale("OXF-001", "Negro", 40);
        service.notifyAfterSale("OXF-001", "Negro", 40);

        List<MailMessage> messages = captured(2);
        assertAlert(messages.get(0), "Stock agotándose",
                "El stock se está agotando y se recomienda hacer un pedido de ese estilo de zapato.", 1);
        assertAlert(messages.get(1), "Stock agotándose",
                "El stock se está agotando y se recomienda hacer un pedido de ese estilo de zapato.", 2);
    }

    @Test
    void sendsOutOfStockMailWhenNoPairsRemain()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(0));
        when(userAdminRepository.findWithEmail()).thenReturn(List.of(admin("ana@gmail.com")));

        service.notifyAfterSale("OXF-001", "Negro", 40);

        MailMessage message = captured(1).get(0);
        assertAlert(message, "Stock agotado",
                "El stock de ese estilo de zapato quedó agotado y es necesario realizar un pedido de ese estilo de zapato.",
                0);
    }

    @Test
    void doesNotSendWhenFiveOrMorePairsRemain()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(5), stock(12));

        service.notifyAfterSale("OXF-001", "Negro", 40);
        service.notifyAfterSale("OXF-001", "Negro", 40);

        verify(userAdminRepository, never()).findWithEmail();
        verify(mailSendService, never()).send(any());
    }

    @Test
    void doesNotSendWhenStockIsMissing()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40))
                .thenReturn(StockQueryResponseDTO.builder().name("Oxford clasico").color("Negro").size(40).build());

        service.notifyAfterSale("OXF-001", "Negro", 40);

        verify(userAdminRepository, never()).findWithEmail();
        verify(mailSendService, never()).send(any());
    }

    @Test
    void sendsOnlyToAdministratorsWithEmail()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(0));
        when(userAdminRepository.findWithEmail()).thenReturn(List.of(
                admin(null),
                admin("   "),
                admin("  ana@gmail.com  "),
                admin("luis@gmail.com")));

        service.notifyAfterSale("OXF-001", "Negro", 40);

        List<MailMessage> messages = captured(2);
        assertEquals("ana@gmail.com", messages.get(0).getRecipient());
        assertEquals("luis@gmail.com", messages.get(1).getRecipient());
        assertTrue(messages.get(0).getSubject().contains("OXF-001"));
        assertTrue(messages.get(0).getBody().contains("Oxford clasico"));
        assertTrue(messages.get(0).getBody().contains("Negro"));
        assertTrue(messages.get(0).getBody().contains("talla 40"));
    }

    @Test
    void doesNotSendWhenNoAdministratorHasEmail()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(1));
        when(userAdminRepository.findWithEmail()).thenReturn(List.of());

        assertDoesNotThrow(() -> service.notifyAfterSale("OXF-001", "Negro", 40));

        verify(mailSendService, never()).send(any());
    }

    @Test
    void keepsSendingWhenOneAdministratorFails()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40)).thenReturn(stock(2));
        when(userAdminRepository.findWithEmail()).thenReturn(List.of(
                admin("ana@gmail.com"),
                admin("luis@gmail.com")));
        doThrow(BusinessException.builder()
                .code(HttpStatus.BAD_GATEWAY)
                .message("Could not send email")
                .build())
                .doNothing()
                .when(mailSendService).send(any());

        assertDoesNotThrow(() -> service.notifyAfterSale("OXF-001", "Negro", 40));

        verify(mailSendService, times(2)).send(any());
    }

    @Test
    void doesNotThrowWhenStockQueryFails()
    {
        when(inventoryQueryService.findStock("OXF-001", "Negro", 40))
                .thenThrow(BusinessException.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message("Could not query stock")
                        .build());

        assertDoesNotThrow(() -> service.notifyAfterSale("OXF-001", "Negro", 40));

        verify(mailSendService, never()).send(any());
    }

    private List<MailMessage> captured(int expectedSends)
    {
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSendService, times(expectedSends)).send(captor.capture());
        return captor.getAllValues();
    }

    private void assertAlert(MailMessage message, String headline, String advice, int pairs)
    {
        assertEquals("ana@gmail.com", message.getRecipient());
        assertEquals(headline + ": OXF-001", message.getSubject());
        assertTrue(message.getBody().contains("Estilo OXF-001 (Oxford clasico), color Negro, talla 40."));
        assertTrue(message.getBody().contains("Stock actual: " + pairs + " pares."));
        assertTrue(message.getBody().contains(advice));
        assertNull(message.getAttachment());
    }

    private StockQueryResponseDTO stock(int pairs)
    {
        return StockQueryResponseDTO.builder()
                .name("Oxford clasico")
                .color("Negro")
                .size(40)
                .stock(pairs)
                .build();
    }

    private UserAdmin admin(String email)
    {
        return UserAdmin.builder()
                .dpi("1234567890101")
                .fullName("Ana Ruiz")
                .phone("55554444")
                .email(email)
                .build();
    }
}
