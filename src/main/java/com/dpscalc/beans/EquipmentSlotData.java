package com.dpscalc.beans;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class EquipmentSlotData implements Serializable {
	private EquipmentSlotItem[] equipmentSlotItems;
}
