package com.estradahermanos.shoestock.dto.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPdfDocument
{
    private String title;

    private String subtitle;

    private String fileName;

    private List<ReportPdfSection> sections;
}
