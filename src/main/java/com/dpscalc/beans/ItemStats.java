package com.dpscalc.beans;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ItemStats implements Serializable {
	private int attack_stab;
	private int attack_slash;
	private int attack_crush;
	private int attack_magic;
	private int attack_ranged;
	private int defence_stab;
	private int defence_slash;
	private int defence_crush;
	private int defence_magic;
	private int defence_ranged;
	private int melee_strength;
	private int ranged_strength;
	private int magic_damage;
	private int prayer;
	private String slot;
	private ItemRequirements requirements;
}
