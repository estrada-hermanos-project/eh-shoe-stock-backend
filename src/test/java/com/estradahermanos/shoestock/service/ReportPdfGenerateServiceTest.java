package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.dto.pdf.ReportPdfSection;
import com.estradahermanos.shoestock.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportPdfGenerateServiceTest
{
    @Mock
    private OpenPdfReportWriter openPdfReportWriter;

    private ReportPdfGenerateService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportPdfGenerateService(openPdfReportWriter);
    }

    @Test
    void returnsGeneratedBytes() throws Exception
    {
        ReportPdfDocument document = document("reporte-resumen.pdf");
        when(openPdfReportWriter.write(document)).thenReturn("%PDF-test".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals("%PDF-test".getBytes(StandardCharsets.UTF_8), service.generate(document));
    }

    @Test
    void rejectsNullOrNamelessDocument()
    {
        BusinessException missing = assertThrows(BusinessException.class, () -> service.generate(null));
        BusinessException nameless = assertThrows(BusinessException.class, () -> service.generate(document("  ")));
        assertEquals("Could not generate the report PDF", missing.getMessage());
        assertEquals("Could not generate the report PDF", nameless.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, missing.getCode());
    }

    @Test
    void wrapsWriterFailure() throws Exception
    {
        ReportPdfDocument document = document("reporte-resumen.pdf");
        when(openPdfReportWriter.write(document)).thenThrow(new IllegalStateException("broken"));

        BusinessException exception = assertThrows(BusinessException.class, () -> service.generate(document));
        assertEquals("Could not generate the report PDF", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
    }

    @Test
    void writerProducesPdfBytes() throws Exception
    {
        byte[] pdf = new OpenPdfReportWriter().write(ReportPdfDocument.builder()
                .title("Resumen")
                .subtitle("Período 2026-09-01 a 2026-09-30")
                .fileName("reporte-resumen.pdf")
                .sections(List.of(
                        ReportPdfSection.builder()
                                .heading("Indicadores")
                                .headers(List.of("Indicador", "Valor"))
                                .rows(List.of(List.of("Pares vendidos", "18")))
                                .build(),
                        ReportPdfSection.builder()
                                .heading("Vacío")
                                .headers(List.of("Columna"))
                                .rows(List.of())
                                .build(),
                        ReportPdfSection.builder()
                                .heading("Corto")
                                .headers(List.of("A", "B"))
                                .rows(shortAndLongRows())
                                .build(),
                        ReportPdfSection.builder()
                                .heading("Sin encabezado")
                                .headers(List.of())
                                .rows(List.of(List.of("x")))
                                .build()))
                .build());

        assertTrue(new String(pdf, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    @Test
    void writerAcceptsNullSectionsAndTitle() throws Exception
    {
        byte[] pdf = new OpenPdfReportWriter().write(ReportPdfDocument.builder()
                .fileName("reporte-resumen.pdf")
                .build());
        assertTrue(new String(pdf, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    private ReportPdfDocument document(String fileName)
    {
        return ReportPdfDocument.builder()
                .title("Resumen")
                .fileName(fileName)
                .build();
    }

    private List<List<String>> shortAndLongRows()
    {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("solo"));
        rows.add(List.of("uno", "dos", "tres"));
        rows.add(null);
        return rows;
    }
}
