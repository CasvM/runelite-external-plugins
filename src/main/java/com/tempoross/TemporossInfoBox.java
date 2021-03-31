package com.tempoross;

import lombok.Setter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class TemporossInfoBox extends InfoBox
{
	@Setter
	private String text;

	public TemporossInfoBox(BufferedImage image, Plugin plugin)
	{
		super(image, plugin);
	}

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}
}
