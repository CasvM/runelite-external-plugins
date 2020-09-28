package com.dpscalc.beans;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Stances implements Serializable {
	private String combat_style;
	private String attack_type;
	private String attack_style;
	private String experience;
	private String boosts;
}
