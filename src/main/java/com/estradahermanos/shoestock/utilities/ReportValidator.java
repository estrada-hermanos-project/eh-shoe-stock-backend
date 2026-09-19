package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReportValidator
{
    public static final int DEFAULT_SLOW_DAYS    = 45;
    public static final int DEFAULT_RESTOCK_DAYS = 30;

    public void validateOptionalRange(LocalDate startDate, LocalDate endDate)
    {
        if (startDate == null && endDate == null)
        {
            return;
        }
        if (startDate == null || endDate == null)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("start_date and end_date must be sent together")
                    .build();
        }
        ensureStartNotAfterEnd(startDate, endDate);
    }

    public void validateRequiredRange(LocalDate startDate, LocalDate endDate)
    {
        if (startDate == null || endDate == null)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("start_date and end_date are required")
                    .build();
        }
        ensureStartNotAfterEnd(startDate, endDate);
    }

    public void validateLimit(Integer limit)
    {
        if (limit != null && limit < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("limit must be greater than or equal to 1")
                    .build();
        }
    }

    public int resolveDays(Integer days)
    {
        if (days == null)
        {
            return DEFAULT_SLOW_DAYS;
        }
        if (days < 1)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("days must be greater than or equal to 1")
                    .build();
        }
        return days;
    }

    public void validateType(String type)
    {
        if (type == null)
        {
            return;
        }
        if (!ShoeType.isValid(type))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Shoe type must be Dama, Caballero, Niño or Niña")
                    .build();
        }
    }

    public ReportVolumeGroup parseVolumeGroup(String groupBy)
    {
        return parseEnum(groupBy, ReportVolumeGroup.class, "group_by must be DAY, WEEK or MONTH");
    }

    public ReportInventoryGroup parseInventoryGroup(String groupBy)
    {
        return parseEnum(groupBy, ReportInventoryGroup.class, "group_by must be TYPE or SUPPLIER");
    }

    private void ensureStartNotAfterEnd(LocalDate startDate, LocalDate endDate)
    {
        if (startDate.isAfter(endDate))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("start_date must not be after end_date")
                    .build();
        }
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> type, String message)
    {
        if (value == null)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message(message)
                    .build();
        }
        try
        {
            return Enum.valueOf(type, value);
        }
        catch (IllegalArgumentException exception)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message(message)
                    .build();
        }
    }
}
