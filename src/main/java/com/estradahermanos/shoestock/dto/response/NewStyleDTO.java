package com.estradahermanos.shoestock.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewStyleDTO
{
    private String code;

    private String name;

    private String type;

    @JsonProperty("supplier_name")
    private String supplierName;

    private String description;

    @JsonProperty("created_at")
    private LocalDate createdAt;
}
