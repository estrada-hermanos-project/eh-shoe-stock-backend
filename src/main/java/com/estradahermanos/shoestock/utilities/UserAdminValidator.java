package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Component
public class UserAdminValidator
{
    private static final Pattern DPI_PATTERN   = Pattern.compile("^\\d{13}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern GMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@gmail\\.com$", Pattern.CASE_INSENSITIVE);
    private static final int     FULL_NAME_MAX = 100;

    public void validateDpi(String dpi)
    {
        if (!StringUtils.hasText(dpi) || !DPI_PATTERN.matcher(dpi).matches())
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("DPI must contain exactly 13 digits")
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

    public void validateEmail(String email)
    {
        if (!StringUtils.hasText(email))
        {
            return;
        }
        if (!GMAIL_PATTERN.matcher(email.trim()).matches())
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Email must be a valid @gmail.com address")
                    .build();
        }
    }

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

    public void validateWritableFields(String fullName, String phone, String email)
    {
        validateFullName(fullName);
        validatePhone(phone);
        validateEmail(email);
    }
}
