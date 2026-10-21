package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.dto.pdf.ReportPdfDocument;
import com.estradahermanos.shoestock.dto.pdf.ReportPdfSection;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

@Component
public class OpenPdfReportWriter
{
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, Color.BLACK);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
    private static final Font HEADING_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Color HEADER_BACKGROUND = new Color(92, 44, 44);

    /** Escribe el documento de reporte en bytes PDF. */
    public byte[] write(ReportPdfDocument document) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(pdf, output);
        pdf.open();
        pdf.add(new Paragraph(" "));
        pdf.add(paragraph(document.getTitle(), TITLE_FONT, Element.ALIGN_LEFT));
        pdf.add(paragraph(document.getSubtitle(), SUBTITLE_FONT, Element.ALIGN_LEFT));
        sections(document).forEach(section -> writeSection(pdf, section));
        pdf.close();
        return output.toByteArray();
    }

    private void writeSection(Document pdf, ReportPdfSection section)
    {
        try
        {
            pdf.add(paragraph(section.getHeading(), HEADING_FONT, Element.ALIGN_LEFT));
            List<String> headers = list(section.getHeaders());
            List<List<String>> rows = list(section.getRows());
            if (headers.isEmpty())
            {
                return;
            }
            pdf.add(table(headers, rows));
        }
        catch (Exception exception)
        {
            throw new IllegalStateException("Could not write PDF section", exception);
        }
    }

    private PdfPTable table(List<String> headers, List<List<String>> rows)
    {
        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);
        table.setSpacingAfter(12);
        headers.forEach(header -> table.addCell(headerCell(header)));
        if (rows.isEmpty())
        {
            table.addCell(bodyCell("Sin datos para este reporte.", headers.size()));
            return table;
        }
        rows.forEach(row -> addRow(table, row, headers.size()));
        return table;
    }

    private void addRow(PdfPTable table, List<String> row, int columns)
    {
        List<String> values = list(row);
        values.stream().limit(columns).forEach(value -> table.addCell(bodyCell(value, 1)));
        int missing = columns - Math.min(values.size(), columns);
        if (missing < 1)
        {
            return;
        }
        table.addCell(bodyCell("", missing));
    }

    private PdfPCell headerCell(String text)
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BACKGROUND);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell bodyCell(String text, int colspan)
    {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setColspan(colspan);
        cell.setPadding(4);
        return cell;
    }

    private Paragraph paragraph(String text, Font font, int alignment)
    {
        Paragraph paragraph = new Paragraph(safe(text), font);
        paragraph.setAlignment(alignment);
        paragraph.setSpacingAfter(6);
        return paragraph;
    }

    private List<ReportPdfSection> sections(ReportPdfDocument document)
    {
        return list(document.getSections());
    }

    private <T> List<T> list(List<T> values)
    {
        if (Objects.isNull(values))
        {
            return List.of();
        }
        return values;
    }

    private String safe(String value)
    {
        if (!StringUtils.hasText(value))
        {
            return "";
        }
        return value;
    }
}
