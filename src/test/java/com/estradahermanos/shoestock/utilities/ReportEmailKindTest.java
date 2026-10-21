package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReportEmailKindTest
{
    @Test
    void resolvesSummaryWithoutSubReport()
    {
        assertEquals(ReportEmailKind.SUMMARY, ReportEmailKind.of("summary", null));
        assertEquals(ReportEmailKind.SUMMARY, ReportEmailKind.of(" SUMMARY ", "  "));
    }

    @Test
    void resolvesKnownSubReportsIgnoringCase()
    {
        assertEquals(ReportEmailKind.SALES_TOP_PRODUCTS, ReportEmailKind.of("Sales", "Top-Products"));
        assertEquals(ReportEmailKind.INVENTORY_STATUS, ReportEmailKind.of("inventory", "status"));
        assertEquals(ReportEmailKind.MERCHANDISE_PENDING, ReportEmailKind.of("merchandise", "pending"));
        assertEquals(ReportEmailKind.SUMMARY_NEW_STYLES, ReportEmailKind.of("summary", "new-styles"));
    }

    @Test
    void rejectsUnknownReport()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> ReportEmailKind.of("catalog", null));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Unknown report", exception.getMessage());
    }

    @Test
    void rejectsUnknownSubReport()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> ReportEmailKind.of("sales", "unknown"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
        assertEquals("Unknown sub-report", exception.getMessage());
    }
}
