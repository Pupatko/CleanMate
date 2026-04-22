package com.cleanmate.presentation.calendar;

import java.time.LocalDate;
import java.time.LocalTime;

public record CalendarCleaningItem(
        LocalDate date,
        LocalTime time,
        String property,
        String employee,
        String status
) {}
