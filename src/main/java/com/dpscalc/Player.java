package com.dpscalc;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.dpscalc.beans.EquipmentSlotItem;
import com.dpscalc.beans.ItemStats;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@Slf4j
public class Player implements Serializable {
	// sets
	private HashMap<Integer, EquipmentSlotItem> currentSet = new HashMap<>();
	private LinkedHashMap<String, HashMap<Integer, EquipmentSlotItem>> equipmentSets = new LinkedHashMap<String, HashMap<Integer, EquipmentSlotItem>>() {{
		put("New", new HashMap<>());
	}};
	private String selectedSetName = "New";
	
	// spells
	private int spellIndex = 0;
	private LinkedHashMap<String, Integer> spells = new LinkedHashMap<String, Integer>() {{
		put("None", 0);

		put("Air Strike", 2);
		put("Water Strike", 4);
		put("Earth Strike", 6);
		put("Fire Strike", 8);
		put("Air Bolt", 9);
		put("Water Bolt", 10);
		put("Earth Bolt", 11);
		put("Fire Bolt", 12);
		put("Air Blast", 13);
		put("Water Blast", 14);
		put("Earth Blast", 15);
		put("Fire Blast", 16);
		put("Air Wave", 17);
		put("Water Wave", 18);
		put("Earth Wave", 19);
		put("Fire Wave", 20);
		put("Air Surge", 21);
		put("Water Surge", 22);
		put("Earth Surge", 23);
		put("Fire Surge", 24);

		put("Smoke Rush", 14);
		put("Shadow Rush", 15);
		put("Blood Rush", 16);
		put("Ice Rush", 17);
		put("Smoke Burst", 18);
		put("Shadow Burst", 19);
		put("Blood Burst", 21);
		put("Ice Burst", 22);
		put("Smoke Blitz", 23);
		put("Shadow Blitz", 24);
		put("Blood Blitz", 25);
		put("Ice Blitz", 26);
		put("Smoke Barrage", 27);
		put("Shadow Barrage", 28);
		put("Blood Barrage", 29);
		put("Ice Barrage", 30);

		put("Crumble Undead", 15);
		put("Iban Blast", 25);
		put("Saradomin Strike (Charge)", 30);
		put("Claws of Guthix (Charge)", 30);
		put("Flames of Zamorak (Charge)", 30);

		put("Magic Dart", 0);
		put("Trident of the Seas", 0);
		put("Trident of the Swamp", 0);
		put("Sanguinesti Staff", 0);
	}};

	// player stats
	private int attackLevel = 1;
	private int strengthLevel = 1;
	private int rangeLevel = 1;
	private int magicLevel = 1;

	// prayer
	private double[] attackPrayer = {1, 1.05, 1.1, 1.15, 1.15, 1.20};
	private double[] strengthPrayer = {1, 1.05, 1.1, 1.15, 1.18, 1.23};
	private double[] rangePrayerDamage = {1, 1.05, 1.1, 1.15, 1.23};
	private double[] rangePrayerAccuracy = {1, 1.05, 1.1, 1.15, 1.20};
	private double[] magicPrayer = {1, 1.05, 1.1, 1.15, 1.25};
	private int attackPrayerIndex = 0;
	private int strengthPrayerIndex = 0;
	private int rangePrayerIndex = 0;
	private int magicPrayerIndex = 0;

	// boosts
	private double[] potionMultiplier = {1, 1.1, 1.15, 1.16};
	private double[] potionBase = {0, 3, 5, 6};
	private int potionIndex = 0;

	// combat stance
	private ArrayList<String> combatStance =  new ArrayList<>(Arrays.asList("punch/crush/accurate", "kick/crush/aggressive", "block/defensive/crush"));
	private int combatStanceIndex;

	// sets
	private boolean voidSet = false;

	HashMap<Integer, EquipmentSlotItem> getSelectedSet() {
		return equipmentSets.get(selectedSetName);
	}

	ItemStats getTotalBonuses() {
		ItemStats bonuses = new ItemStats();
		for(Map.Entry<Integer, EquipmentSlotItem> entry : currentSet.entrySet()) {
			ItemStats value = entry.getValue().getEquipment();
			bonuses.setAttack_stab(bonuses.getAttack_stab() + value.getAttack_stab());
			bonuses.setAttack_slash(bonuses.getAttack_slash() + value.getAttack_slash());
			bonuses.setAttack_crush(bonuses.getAttack_crush() + value.getAttack_crush());
			bonuses.setAttack_magic(bonuses.getAttack_magic() + value.getAttack_magic());
			bonuses.setAttack_ranged(bonuses.getAttack_ranged() + value.getAttack_ranged());
			bonuses.setMelee_strength(bonuses.getMelee_strength() + value.getMelee_strength());
			bonuses.setMagic_damage(bonuses.getMagic_damage() + value.getMagic_damage());
			bonuses.setRanged_strength(bonuses.getRanged_strength() + value.getRanged_strength());
		}
		return bonuses;
	}

	int getEffectiveAttackLevel() {
		int effectiveLevel = (int) ((int) (potionBase[potionIndex] + (attackLevel * potionMultiplier[potionIndex])) * attackPrayer[attackPrayerIndex]);
		switch (attackType()[2]) {
			case "accurate":
				effectiveLevel += 3;
				break;
			case "controlled":
				effectiveLevel += 1;
				break;
		}
		effectiveLevel = (int) ((effectiveLevel + 8) * voidMelee());

		return effectiveLevel;
	}

	int getEffectiveStrengthLevel() {
		int effectiveLevel = (int) ((int) (potionBase[potionIndex] + (strengthLevel * potionMultiplier[potionIndex])) * strengthPrayer[strengthPrayerIndex]);
		switch (attackType()[2]) {
			case "aggressive":
				effectiveLevel += 3;
				break;
			case "controlled":
				effectiveLevel += 1;
				break;
		}
		effectiveLevel = (int) ((effectiveLevel + 8) * voidMelee());

		return effectiveLevel;
	}

	int getEffectiveMagicLevel() {
		int effectiveLevel = (int) ((int) (potionBase[potionIndex] + (magicLevel * potionMultiplier[potionIndex])) * magicPrayer[magicPrayerIndex]);
		if (currentSet.containsKey(3) &&
				(currentSet.get(3).getName().toLowerCase().contains("trident") || currentSet.get(3).getName().toLowerCase().contains("sanguinesti"))) {
			switch (attackType()[0]) {
				case "accurate":
					effectiveLevel += 3;
					break;
				case "longrange":
					effectiveLevel += 1;
					break;
			}
		}
		effectiveLevel = (int) ((effectiveLevel + 8) * voidMage());

		return effectiveLevel;
	}

	int getVisibleMagicLevel() {
		return (int) (potionBase[potionIndex] + (magicLevel * potionMultiplier[potionIndex]));
	}

	int getEffectiveRangeAttackLevel() {
		int effectiveLevel = (int) ((int) (potionBase[potionIndex] + (rangeLevel * potionMultiplier[potionIndex])) * rangePrayerAccuracy[rangePrayerIndex]);
		if (attackType()[0].equals("accurate")) {
			effectiveLevel += 3;
		}
		effectiveLevel = (int) ((effectiveLevel + 8) * voidRange());

		return effectiveLevel;
	}

	int getEffectiveRangeStrengthLevel() {
		int effectiveLevel = (int) ((int) (potionBase[potionIndex] + (rangeLevel * potionMultiplier[potionIndex])) * rangePrayerDamage[rangePrayerIndex]);
		if (attackType()[0].equals("accurate")) {
			effectiveLevel += 3;
		}
		effectiveLevel = (int) ((effectiveLevel + 8) * voidRange());

		return effectiveLevel;
	}

	double voidMelee() {
		double num = 1;
		if (voidSet) {
			if (currentSet.get(0).getName().toLowerCase().contains("void melee")) {
				num = 1.45;
			}
		}
		return num;
	}

	double voidMage() {
		double num = 1;
		if (voidSet) {
			if (currentSet.get(0).getName().toLowerCase().contains("void mage")) {
				num = 1.45;
			}
		}
		return num;
	}

	double voidRange() {
		double num = 1;
		if (voidSet) {
			if (currentSet.get(0).getName().toLowerCase().contains("void range")) {
				num = 1.45;
			}
		}
		return num;
	}

	double inquisitor() {
		int num = 0;
		double[] inquisitor = {1, 1.005, 1.01, 1.025};
		if (attackType()[1].equals("crush")) {
			if (currentSet.containsKey(0) && currentSet.get(0).getName().toLowerCase().contains("inquisitor")) {
				num++;
			}
			if (currentSet.containsKey(4) && currentSet.get(4).getName().toLowerCase().contains("inquisitor")) {
				num++;
			}
			if (currentSet.containsKey(7) && currentSet.get(7).getName().toLowerCase().contains("inquisitor")) {
				num++;
			}
		}
		return inquisitor[num];
	}

	int getSpellDamage() {
		ArrayList<Integer> spellDamage = new ArrayList<>(spells.values());
		ArrayList<String> spellName = new ArrayList<>(spells.keySet());
		int num = spellDamage.get(spellIndex);
		if (spellName.get(spellIndex).toLowerCase().contains("bolt") &&
				currentSet.containsKey(9) &&
				currentSet.get(9).getName().toLowerCase().equals("chaos gauntlets")) {
			num += 3;
		} else if (currentSet.containsKey(3) && Arrays.asList(42, 43, 44, 45).contains(spellIndex)) {
			if (currentSet.get(3).getName().toLowerCase().contains(("slayer's"))) {
				num = (int) (getVisibleMagicLevel() / 10.0) + 10;
			} else if (currentSet.get(3).getName().toLowerCase().contains(("seas"))) {
				num = (int) (getVisibleMagicLevel() / 3.0) - 5;
			} else if (currentSet.get(3).getName().toLowerCase().contains(("swamp"))) {
				num = (int) (getVisibleMagicLevel() / 3.0) - 2;
			} else if (currentSet.get(3).getName().toLowerCase().contains(("sanguinesti"))) {
				num = (int) (getVisibleMagicLevel() / 3.0) - 1;
			}
		}
		return num;
	}

	String[] attackType() {
			return combatStance.get(combatStanceIndex).split("/");
	}

	double tomeOfFire() {
		ArrayList<String> list = new ArrayList<>(spells.keySet());
		if (list.get(spellIndex).toLowerCase().contains("fire") &&
				currentSet.containsKey(5) &&
				currentSet.get(5).getName().toLowerCase().equals("tome of fire")) {
			return 1.5;
		} else {
			return 1.0;
		}
	}

	int attackSpeed() {
		try {
			if (spellIndex > 0 && spellIndex < 42) {
				if (currentSet.get(3).getName().toLowerCase().contains("harmonised")) {
					return 4;
				} else {
					return 5;
				}
			} else if (attackType()[0].toLowerCase().contains("rapid")) {
				return currentSet.get(3).getWeapon().getAttack_speed() - 1;
			} else {
				return currentSet.get(3).getWeapon().getAttack_speed();
			}
		} catch (Exception e) {
			return 4;
		}
	}

	boolean scythe() {
		return currentSet.containsKey(3) && currentSet.get(3).getName().toLowerCase().contains("scythe of vitur");
	}

	// dharoks

	// berserker necklane

	// full obby set

	// keris
}
