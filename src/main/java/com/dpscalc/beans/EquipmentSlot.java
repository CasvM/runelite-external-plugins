package com.dpscalc.beans;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.EquipmentInventorySlot;

@AllArgsConstructor
@Getter
public enum EquipmentSlot {
	HEAD(EquipmentInventorySlot.HEAD, "items-head.json", "0.png"),
	CAPE(EquipmentInventorySlot.CAPE, "items-cape.json", "1.png"),
	AMULET(EquipmentInventorySlot.AMULET, "items-neck.json", "2.png"),
	WEAPON(EquipmentInventorySlot.WEAPON, "items-weapon.json", "3.png"),
	BODY(EquipmentInventorySlot.BODY, "items-body.json", "4.png"),
	SHIELD(EquipmentInventorySlot.SHIELD, "items-shield.json", "5.png"),
	LEGS(EquipmentInventorySlot.LEGS, "items-legs.json", "7.png"),
	GLOVES(EquipmentInventorySlot.GLOVES, "items-hands.json", "9.png"),
	BOOTS(EquipmentInventorySlot.BOOTS, "items-feet.json", "10.png"),
	RING(EquipmentInventorySlot.RING, "items-ring.json", "12.png"),
	AMMO(EquipmentInventorySlot.AMMO, "items-ammo.json", "13.png");

	private final EquipmentInventorySlot slot;
	private final String dataFile;
	private final String imageFile;
}
