package com.estradahermanos.shoestock.utilities;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BusinessDateTest
{
    @Test
    void todayMatchesGuatemalaCalendarDay()
    {
        Clock clock = Clock.fixed(
                LocalDate.of(2026, 9, 29).atTime(10, 0).atZone(BusinessDate.ZONE).toInstant(),
                BusinessDate.ZONE);

        assertEquals(LocalDate.of(2026, 9, 29), new BusinessDate(clock).today());
    }

    @Test
    void todayStaysOnTheSameDayAfterSixPm()
    {
        Clock clock = Clock.fixed(
                LocalDate.of(2026, 9, 29).atTime(18, 30).atZone(BusinessDate.ZONE).toInstant(),
                BusinessDate.ZONE);

        assertEquals(LocalDate.of(2026, 9, 29), new BusinessDate(clock).today());
    }

    @Test
    void systemClockUsesGuatemalaZone()
    {
        assertEquals(LocalDate.now(BusinessDate.ZONE), new BusinessDate().today());
    }

    @Test
    void rejectsNullClock()
    {
        assertThrows(IllegalArgumentException.class, () -> new BusinessDate(null));
    }
}
