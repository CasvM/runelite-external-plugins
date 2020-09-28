package com.dpscalc.beans;

import lombok.Getter;

@Getter
public class Monster {
	int id;
	String name;
	boolean incomplete;
	boolean members;
	String release_date;
	int combat_level;
	int size;
	int hitpoints;
	int max_hit;
	String[] attack_type;
	int attack_speed;
	boolean aggressive;
	boolean poisonous;
	boolean immune_poison;
	boolean immune_venom;
	String[] attributes;
	String[] category;
	boolean slayer_monster;
	int slayer_level;
	double slayer_xp;
	String[] slayer_masters;
	boolean duplicate;
	String examine;
	String icon;
	String wiki_name;
	String wiki_url;
	int attack_level;
	int strength_level;
	int defence_level;
	int magic_level;
	int ranged_level;
	int attack_stab;
	int attack_slash;
	int attack_crush;
	int attack_magic;
	int attack_ranged;
	int defence_stab;
	int defence_slash;
	int defence_crush;
	int defence_magic;
	int defence_ranged;
	int attack_accuracy;
	int melee_strength;
	int ranged_strength;
	int magic_damage;
}
