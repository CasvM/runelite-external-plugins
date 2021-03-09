package com.templetracker.constructors;
import lombok.Data;
import java.util.ArrayList;

@Data
public class TempleTracker
{
	Long startTime = System.currentTimeMillis();
	Long endTime = -1L;

	StartLocation startLocation = null;
	Companion companion = null;

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





