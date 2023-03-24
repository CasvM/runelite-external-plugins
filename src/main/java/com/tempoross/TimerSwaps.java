package com.tempoross;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class TimerSwaps {
    public enum TimerModes {
        OFF,
        PIE,
        TICKS,
        SECONDS
    }
}
