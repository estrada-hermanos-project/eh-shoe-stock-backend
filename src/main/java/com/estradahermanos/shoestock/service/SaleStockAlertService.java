package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.response.StockQueryResponseDTO;
import com.estradahermanos.shoestock.mapper.StockAlertMailMapper;
import com.estradahermanos.shoestock.repository.entities.UserAdmin;
import com.estradahermanos.shoestock.repository.repositories.UserAdminRepository;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import com.estradahermanos.shoestock.utilities.StockAlertLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleStockAlertService
{
    private final InventoryQueryService inventoryQueryService;
    private final UserAdminRepository   userAdminRepository;
    private final MailSendService       mailSendService;
    private final StockAlertMailMapper  stockAlertMailMapper;

    /**
     * Consulta el stock de la variante ya vendida y avisa a los administradores con correo.
     * Un fallo de consulta o de envío no se propaga: la venta ya quedó registrada.
     */
    public void notifyAfterSale(String shoeCode, String color, Integer size)
    {
        try
        {
            deliver(shoeCode, color, size);
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to send stock alert after sale", exception);
        }
    }

    private void deliver(String shoeCode, String color, Integer size)
    {
        StockQueryResponseDTO current = inventoryQueryService.findStock(shoeCode, color, size);
        Optional<StockAlertLevel> level = StockAlertLevel.fromStock(current.getStock());
        if (level.isEmpty())
        {
            log.info("Stock alert skipped because the variant still has enough pairs");
            return;
        }
        recipients().forEach(email -> sendOne(email, level.get(), shoeCode, current));
    }

    private List<String> recipients()
    {
        return userAdminRepository.findWithEmail().stream()
                .map(UserAdmin::getEmail)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
    }

    private void sendOne(String email, StockAlertLevel level, String shoeCode, StockQueryResponseDTO current)
    {
        try
        {
            MailMessage message = stockAlertMailMapper.toMessage(
                    email,
                    level.subject(shoeCode),
                    level.body(shoeCode, current.getName(), current.getColor(), current.getSize(), current.getStock()));
            mailSendService.send(message);
            log.info("Stock alert email sent for shoe {}", shoeCode);
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to send stock alert to one administrator", exception);
        }
    }
}
