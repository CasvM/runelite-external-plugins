package com.raidtracker.data;
import lombok.Value;

@Value
public class RaidState
{
    private final boolean inRaid;
    private final int raidType;
}