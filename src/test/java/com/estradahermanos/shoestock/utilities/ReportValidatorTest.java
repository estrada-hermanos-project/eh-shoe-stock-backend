package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportValidatorTest
{
    private final ReportValidator validator = new ReportValidator();

    @Test
    void optionalRangeAllowsBothNull()
    {
        assertDoesNotThrow(() -> validator.validateOptionalRange(null, null));
    }

    @Test
    void optionalRangeRejectsOnlyOneDate()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validateOptionalRange(LocalDate.of(2026, 9, 1), null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("start_date and end_date must be sent together", exception.getMessage());
    }

    @Test
    void requiredRangeRejectsMissingDates()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validateRequiredRange(null, LocalDate.of(2026, 9, 30)));

        assertEquals("start_date and end_date are required", exception.getMessage());
    }

    @Test
    void rejectsStartAfterEnd()
    {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> validator.validateRequiredRange(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1)));

        assertEquals("start_date must not be after end_date", exception.getMessage());
    }

    @Test
    void rejectsLimitBelowOne()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateLimit(0));

        assertEquals("limit must be greater than or equal to 1", exception.getMessage());
    }

    @Test
    void resolveDaysDefaultsToFortyFive()
    {
        assertEquals(45, validator.resolveDays(null));
    }

    @Test
    void parseVolumeGroupRejectsInvalidValue()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.parseVolumeGroup("YEAR"));

        assertEquals("group_by must be DAY, WEEK or MONTH", exception.getMessage());
    }

    @Test
    void parseInventoryGroupAcceptsType()
    {
        assertEquals(ReportInventoryGroup.TYPE, validator.parseInventoryGroup("TYPE"));
    }

    @Test
    void rejectsInvalidShoeType()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> validator.validateType("Unisex"));

        assertEquals("Shoe type must be Dama, Caballero, Niño or Niña", exception.getMessage());
    }
}
