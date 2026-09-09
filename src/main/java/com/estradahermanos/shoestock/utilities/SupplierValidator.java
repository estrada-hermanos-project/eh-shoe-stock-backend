package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class SupplierValidator
{
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");
    private static final int     FULL_NAME_MAX = 100;

    public void validateFullName(String fullName)
    {
        if (!StringUtils.hasText(fullName))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Full name is required")
                    .build();
        }
        if (fullName.length() > FULL_NAME_MAX)
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Full name must not exceed 100 characters")
                    .build();
        }
    }

    public void validatePhone(String phone)
    {
        if (!StringUtils.hasText(phone) || !PHONE_PATTERN.matcher(phone).matches())
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Phone must contain exactly 8 digits")
                    .build();
        }
    }

    public void validateWritableFields(String fullName, String phone)
    {
        validateFullName(fullName);
        validatePhone(phone);
    }
}
