package com.dpscalc.beans;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class EquipmentSlotItem implements Serializable {
	private int id;
	private String name;
	private boolean incomplete;
	private boolean members;
	private boolean tradeable;
	private boolean tradeable_on_ge;
	private boolean stackable;
	private int stacked;
	private boolean notes;
	private boolean noteable;
	private int linked_id_item;
	private int linked_id_noted;
	private int linked_id_placeholder;
	private boolean placeholder;
	private boolean equipable;
	private boolean equipable_by_player;
	private boolean equipable_weapon;
	private int cost;
	private int lowalch;
	private int highalch;
	private double weight;
	private int buy_limit;
	private boolean quest_item;
	private String release_date;
	private boolean duplicate;
	private String examine;
	private String icon;
	private String wiki_name;
	private String wiki_url;
	private String wiki_exchange;
	private ItemStats equipment;
	private Weapon weapon;
}
