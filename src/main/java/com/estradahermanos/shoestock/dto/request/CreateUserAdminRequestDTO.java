package com.estradahermanos.shoestock.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CreateUserAdminRequestDTO
{
    private String dpi;

    @JsonProperty("full_name")
    private String fullName;

    private String phone;

    private String email;
}
