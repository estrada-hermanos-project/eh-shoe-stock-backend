package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.mail.MailAttachment;
import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.dto.request.SendReportEmailRequestDTO;
import com.estradahermanos.shoestock.dto.response.MessageResponseDTO;
import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.mapper.ReportEmailMailMapper;
import com.estradahermanos.shoestock.utilities.ExceptionLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEmailSendService
{
    static final String SUCCESS_MESSAGE = "The report was sent to the administrator email";

    private final SessionAdministratorEmailService sessionAdministratorEmailService;
    private final ReportPdfDocumentService reportPdfDocumentService;
    private final ReportPdfGenerateService reportPdfGenerateService;
    private final MailSendService mailSendService;
    private final ReportEmailMailMapper reportEmailMailMapper;

    /** Genera el PDF del reporte pedido y lo envía al correo del usuario de la sesión. */
    public MessageResponseDTO send(String authorization, SendReportEmailRequestDTO request)
    {
        log.info("Sending report email");
        try
        {
            if (Objects.isNull(request))
            {
                throw BusinessException.builder()
                        .code(HttpStatus.BAD_REQUEST)
                        .message("Request body is required")
                        .build();
            }
            String email = sessionAdministratorEmailService.resolve(authorization);
            ReportPdfDocument document = reportPdfDocumentService.build(request);
            byte[] pdf = reportPdfGenerateService.generate(document);
            MailAttachment attachment = reportEmailMailMapper.toAttachment(document.getFileName(), pdf);
            mailSendService.send(reportEmailMailMapper.toMessage(
                    email,
                    "Reporte Estrada Hermanos — " + document.getTitle(),
                    "Adjunto encontrará el reporte \"" + document.getTitle() + "\".",
                    attachment));
            log.info("Report email sent");
            return MessageResponseDTO.builder().message(SUCCESS_MESSAGE).build();
        }
        catch (BusinessException exception)
        {
            throw exception;
        }
        catch (Exception exception)
        {
            ExceptionLog.unexpected("Failed to send the report email", exception);
            throw BusinessException.builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Could not send the report email")
                    .build();
        }
    }
}
