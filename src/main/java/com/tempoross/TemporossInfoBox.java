package com.tempoross;

import java.awt.Color;
import java.awt.image.BufferedImage;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class TemporossInfoBox extends InfoBox
{
	@Setter
	@Getter
	private int rewardCount;

	@Setter
	private String alternateText;

	@Setter
	private String toolTipText;

	public TemporossInfoBox(BufferedImage image, Plugin plugin, int rewardCount)
	{
		super(image, plugin);
		this.rewardCount = rewardCount;
	}

	@Override
	public String getText()
	{
		if (alternateText != null)
		{
			return alternateText;
		}
		return Integer.toString(rewardCount);
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		return toolTipText;
	}

}
