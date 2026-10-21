package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportPdfGenerateService
{
    private final OpenPdfReportWriter openPdfReportWriter;

    /** Genera los bytes del PDF. Un fallo se registra y se responde de forma controlada. */
    public byte[] generate(ReportPdfDocument document)
    {
        if (Objects.isNull(document) || !StringUtils.hasText(document.getFileName()))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Could not generate the report PDF")
                    .build();
        }
        try
        {
            return openPdfReportWriter.write(document);
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to generate the report PDF", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not generate the report PDF")
                    .build();
        }
    }
}
