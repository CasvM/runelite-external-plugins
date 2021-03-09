package com.templetracker.constructors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum StartLocation
{
	BURGH_DE_ROTT("Burgh de rott", 13874),
	PATERDOMUS("Paterdomus", 13622);

	@Getter
	private final String name;

	@Getter
	private final int RegionID;
}
