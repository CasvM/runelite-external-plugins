package com.dpscalc;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Target implements Serializable {
	// target stats
	private int hpLevel = 1;
	private int attackLevel = 0;
	private int strengthLevel = 0;
	private int defenseLevel = 0;
	private int rangeLevel = 0;
	private int magicLevel = 0;

	// target defense bonuses
	private int stabDefenseBonus = 0;
	private int slashDefenseBonus = 0;
	private int crushDefenseBonus = 0;
	private int rangedDefenseBonus = 0;
	private int magicDefenseBonus = 0;

	// damage & accuracy multipiers
	private boolean undead = false;
	private boolean slayerTask = false;
	private double demon = 1.0;
	private double dragon = 1.0;
	private double kalphite = 1.0;
	private double[] salve = {1.0, 1.0, 1.0};
	private double[] slayerBonus = {1.0, 1.0, 1.0};

	// size
	private int size = 1;

	// stat drains
	private int dwh = 0;
	private int bgs = 0;
	private int arclight = 0;

	private int monsterIndex = 0;
}
