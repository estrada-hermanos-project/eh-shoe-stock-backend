package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailAttachment;
import com.estradahermanos.shoestock.dto.mail.MailMessage;
import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.dto.request.SendReportEmailRequestDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportEmailMailMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportEmailSendServiceTest
{
    @Mock
    private SessionAdministratorEmailService sessionAdministratorEmailService;

    @Mock
    private ReportPdfDocumentService reportPdfDocumentService;

    @Mock
    private ReportPdfGenerateService reportPdfGenerateService;

    @Mock
    private MailSendService mailSendService;

    private ReportEmailSendService service;

    @BeforeEach
    void setUp()
    {
        service = new ReportEmailSendService(
                sessionAdministratorEmailService,
                reportPdfDocumentService,
                reportPdfGenerateService,
                mailSendService,
                Mappers.getMapper(ReportEmailMailMapper.class));
    }

    @Test
    void sendsPdfToResolvedEmail()
    {
        SendReportEmailRequestDTO request = SendReportEmailRequestDTO.builder()
                .report("sales")
                .subReport("top-products")
                .build();
        when(sessionAdministratorEmailService.resolve("Bearer jwt")).thenReturn("ana@gmail.com");
        when(reportPdfDocumentService.build(request)).thenReturn(ReportPdfDocument.builder()
                .title("Ventas · Más vendidos")
                .fileName("reporte-ventas-mas-vendidos.pdf")
                .build());
        when(reportPdfGenerateService.generate(any())).thenReturn(new byte[] {1, 2, 3});

        assertEquals(
                ReportEmailSendService.SUCCESS_MESSAGE,
                service.send("Bearer jwt", request).getMessage());

        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSendService).send(captor.capture());
        MailMessage message = captor.getValue();
        assertEquals("ana@gmail.com", message.getRecipient());
        assertEquals("Reporte Estrada Hermanos — Ventas · Más vendidos", message.getSubject());
        assertTrue(message.getBody().contains("Ventas · Más vendidos"));
        MailAttachment attachment = message.getAttachment();
        assertEquals("reporte-ventas-mas-vendidos.pdf", attachment.getFileName());
        assertEquals("application/pdf", attachment.getContentType());
        assertEquals(3, attachment.getContent().length);
    }

    @Test
    void rejectsNullRequest()
    {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.send("Bearer jwt", null));
        assertEquals("Request body is required", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getCode());
    }

    @Test
    void propagatesMissingEmail()
    {
        when(sessionAdministratorEmailService.resolve("Bearer jwt")).thenThrow(BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message("The administrator has no email registered")
                .build());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.send("Bearer jwt", SendReportEmailRequestDTO.builder().report("summary").build()));
        assertEquals("The administrator has no email registered", exception.getMessage());
    }

    @Test
    void wrapsUnexpectedFailure()
    {
        when(sessionAdministratorEmailService.resolve("Bearer jwt")).thenThrow(new IllegalStateException("boom"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.send("Bearer jwt", SendReportEmailRequestDTO.builder().report("summary").build()));
        assertEquals("Could not send the report email", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getCode());
    }

    @Test
    void propagatesMailFailure()
    {
        SendReportEmailRequestDTO request = SendReportEmailRequestDTO.builder().report("summary").build();
        when(sessionAdministratorEmailService.resolve("Bearer jwt")).thenReturn("ana@gmail.com");
        when(reportPdfDocumentService.build(request)).thenReturn(ReportPdfDocument.builder()
                .title("Resumen")
                .fileName("reporte-resumen.pdf")
                .build());
        when(reportPdfGenerateService.generate(any())).thenReturn(new byte[] {1});
        doThrow(BusinessException.builder()
                .code(HttpStatus.BAD_GATEWAY)
                .message("Could not send email")
                .build()).when(mailSendService).send(any());

        assertEquals("Could not send email", assertThrows(BusinessException.class, () -> service.send("Bearer jwt", request))
                .getMessage());
    }
}
