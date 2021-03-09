package com.templetracker;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import lombok.Getter;

@Data
public class TempleTracker
{
	Long startTime = System.currentTimeMillis();
	Long endTime = -1L;

	StartLocation startLocation = null;

	int route = -1;
	int points = -1;

	int check = 0;
	boolean templeTrekking;

	Encounter latestEncounter;

	ArrayList<Encounter> encounterList = new ArrayList<>();

	public void addEncounterToList()
	{
		encounterList.add(latestEncounter);
		setLatestEncounter(null);
	}
}

@AllArgsConstructor
enum StartLocation
{
	BURGH_DE_ROTT("Burgh de rott", 13874),
	PATERDOMUS("Paterdomus", 13622);

	@Getter
	private final String name;

	@Getter
	private final int RegionID;
}

enum Companion {

}




