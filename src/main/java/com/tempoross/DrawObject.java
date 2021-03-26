package com.tempoross;

import java.awt.Color;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;


@Setter
@Getter
@AllArgsConstructor
class DrawObject
{
	private final Tile tile;
	private final GameObject gameObject;
	private Instant startTime;
	private final int duration;
	private Color color;
}
