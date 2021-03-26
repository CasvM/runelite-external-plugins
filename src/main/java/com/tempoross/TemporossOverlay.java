package com.tempoross;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

class TemporossOverlay extends Overlay
{
	private static final int MAX_DISTANCE = 2400;

	private final Client client;
	private final TemporossPlugin plugin;
	private final TemporossConfig config;

	@Inject
	private TemporossOverlay(Client client, TemporossPlugin plugin, TemporossConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();

		Instant now = Instant.now();
		if (config.highlightFires())
		{
			plugin.getGameObjects().values().forEach((drawObject) ->
			{
				GameObject object = drawObject.getGameObject();
				Tile tile = drawObject.getTile();

				if (tile.getPlane() == client.getPlane()
					&& tile.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE)
				{
					Polygon poly = object.getCanvasTilePoly();

					if (poly != null)
					{
						OverlayUtil.renderPolygon(graphics, poly, drawObject.getColor());
					}
				}

				if (config.useFireTimer() && drawObject.getDuration() > 0 && drawObject.getGameObject().getCanvasLocation() != null)
				{
					//modulo as the fire spreads every 24 seconds
					float percent = ((now.toEpochMilli() - drawObject.getStartTime().toEpochMilli()) % drawObject.getDuration()) / (float) drawObject.getDuration();
					ProgressPieComponent ppc = new ProgressPieComponent();
					ppc.setBorderColor(drawObject.getColor());
					ppc.setFill(drawObject.getColor());
					ppc.setProgress(percent);
					ppc.setDiameter(20);
					ppc.setPosition(drawObject.getGameObject().getCanvasLocation());
					ppc.render(graphics);
				}
			});
		}

		if (config.highlightDoubleSpot())
		{
			plugin.getNpcs().forEach((npc, startTime) ->
			{
				NPCComposition npcComposition = npc.getComposition();
				int size = npcComposition.getSize();
				LocalPoint lp = npc.getLocalLocation();

				Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, lp, size);
				if (tilePoly != null && lp.distanceTo(playerLocation) < MAX_DISTANCE)
				{
					OverlayUtil.renderPolygon(graphics, tilePoly, config.doubleSpotColor());
				}

				if (config.useDoubleSpotTimer())
				{
					//testing shows a time between 20 and 27 seconds. even though it isn't fully accurate, it is still better than nothing
					float percent = (now.toEpochMilli() - startTime.toEpochMilli()) / (float) 24000;
					ProgressPieComponent ppc = new ProgressPieComponent();
					ppc.setBorderColor(config.doubleSpotColor());
					ppc.setFill(config.doubleSpotColor());
					ppc.setProgress(percent);
					ppc.setDiameter(20);
					ppc.setPosition(Perspective.localToCanvas(client, lp, client.getPlane()));
					ppc.render(graphics);
				}
			});
		}

		return null;
	}

}
