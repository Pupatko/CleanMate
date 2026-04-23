package com.cleanmate.presentation.myschedule;

import java.time.LocalTime;

public record MyTaskItem(
        LocalTime time,
        String property,
        String customer,
        String status,
        int stepsTotal,
        int stepsDone
) {}
