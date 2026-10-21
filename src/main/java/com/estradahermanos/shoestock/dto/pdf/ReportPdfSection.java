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
public class ReportPdfSection
{
    private String heading;

    private List<String> headers;

    private List<List<String>> rows;
}
