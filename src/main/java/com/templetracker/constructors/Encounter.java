package com.templetracker.constructors;

import lombok.Data;

@Data
public class Encounter
{
	Long startTime = System.currentTimeMillis();
	Long endTime = -1L;

	EncounterName name = null;

	int location = -1;
	int route = -1;
}
