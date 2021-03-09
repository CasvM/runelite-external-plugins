package com.templetracker.constructors;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Companion {
	FYIONY_FRAY("Fyiona Fray (easy)", 1567),
	DALCIAN_FANG("Dalcian Fang (easy)", 1566),
	MAGE("Mage (easy)", 1578),
	ADVENTURER("Adventurer (easy)", 1577),
	JAYENE_KLIYN("Jayene Kliyn (medium)", 1564),
	VALANTINE_EPPEL("Valantay Eppel (medium)", 1565),
	RANGER("Ranger (medium)", 1576),
	APPRENTICE("Apprentice (medium)", 1575),
	ROLAYNE_TWICKIT("Rolayne Twickit (hard)", 1563),
	SMIDDI_RYAK("Smiddi Ryak (hard)", 1562),
	WOMAN_AT_ARMS("Woman-at-arms (Hard)", 1574),
	FORESTER("Forester (hard)", 1573);

	@Getter
	private final String name;

	@Getter
	private final int NpcId;

	public static Companion getCompanion(String npcName) {
		for (Companion c : Companion.values()) {
			if (c.name.toLowerCase().equals(npcName.toLowerCase())) {
				return c;
			}
		}
		return null;
	}
}