package com.raidtracker.utils;

import com.raidtracker.data.RaidState;
import lombok.Value;

@Value
public class RaidStateChanged
{

    private final RaidState previousState;
    private final RaidState newState;

}