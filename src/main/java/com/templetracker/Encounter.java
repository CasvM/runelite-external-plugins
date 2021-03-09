package com.templetracker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class Encounter
{
	Long startTime = System.currentTimeMillis();
	Long endTime = -1L;

	EncounterName name = null;

	int location = -1;
	int route = -1;
}

@AllArgsConstructor
enum EncounterName {
	VAMPYRES("Vampyres"),
	GHASTS("Ghasts"),
	SHADES("Shades"),
	SNAKES("Swamp snakes"),
	SNAILS("Giant snails"),
	NAIL_BEASTS("Nail beasts"),
	TENTACLES("Tentacles"),
	BRIDGE("Broken bridge"),
	BOG("Bog"),
	RIVER("River Swing"),
	ABIDOR("Abidor Crank");

	@Getter
	private final String name;
}
