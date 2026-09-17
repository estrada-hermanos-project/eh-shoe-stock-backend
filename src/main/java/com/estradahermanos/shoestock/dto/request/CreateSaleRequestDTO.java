package com.estradahermanos.shoestock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaleRequestDTO
{
    private Integer size;

    private String name;

    private String color;

    private Integer stock;
}
