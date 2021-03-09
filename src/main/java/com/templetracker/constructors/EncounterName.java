package com.templetracker.constructors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EncounterName
{
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
