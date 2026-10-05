package com.estradahermanos.shoestock.utilities;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Component
public class BusinessDate
{
    public static final ZoneId ZONE = ZoneId.of("America/Guatemala");

    private final Clock clock;

    public BusinessDate()
    {
        this(Clock.system(ZONE));
    }

    public BusinessDate(Clock clock)
    {
        if (Objects.isNull(clock))
        {
            throw new IllegalArgumentException("Clock is required");
        }
        this.clock = clock;
    }

    public LocalDate today()
    {
        return LocalDate.now(clock);
    }
}
