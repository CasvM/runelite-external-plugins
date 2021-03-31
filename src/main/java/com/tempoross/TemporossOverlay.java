package com.tempoross;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.time.Instant;

class TemporossOverlay extends Overlay
{
	private static final int MAX_DISTANCE = 3000;
	private static final int PIE_DIAMETER = 20;
	private static final float DOUBLE_SPOT_MOVE_MILLIS = 24000f;

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
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return null;
		}
		LocalPoint playerLocation = localPlayer.getLocalLocation();
		Instant now = Instant.now();

		highlightGameObjects(graphics, playerLocation, now);
		highlightNpcs(graphics, playerLocation, now);


		return null;
	}

	private void highlightGameObjects(Graphics2D graphics, LocalPoint playerLocation, Instant now)
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

			if (drawObject.getDuration() > 0 &&
				drawObject.getGameObject().getCanvasLocation() != null &&
				tile.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE)
			{
				//modulo as the fire spreads every 24 seconds
				float percent = ((now.toEpochMilli() - drawObject.getStartTime().toEpochMilli()) % drawObject.getDuration()) / (float) drawObject.getDuration();
				ProgressPieComponent ppc = new ProgressPieComponent();
				ppc.setBorderColor(drawObject.getColor());
				ppc.setFill(drawObject.getColor());
				ppc.setProgress(percent);
				ppc.setDiameter(PIE_DIAMETER);
				ppc.setPosition(drawObject.getGameObject().getCanvasLocation());
				ppc.render(graphics);
			}
		});
	}

	private void highlightNpcs(Graphics2D graphics, LocalPoint playerLocation, Instant now)
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

			if (lp.distanceTo(playerLocation) < MAX_DISTANCE)
			{
				//testing shows a time between 20 and 27 seconds. even though it isn't fully accurate, it is still better than nothing
				float percent = (now.toEpochMilli() - startTime.toEpochMilli()) / DOUBLE_SPOT_MOVE_MILLIS;
				ProgressPieComponent ppc = new ProgressPieComponent();
				ppc.setBorderColor(config.doubleSpotColor());
				ppc.setFill(config.doubleSpotColor());
				ppc.setProgress(percent);
				ppc.setDiameter(PIE_DIAMETER);
				ppc.setPosition(Perspective.localToCanvas(client, lp, client.getPlane()));
				ppc.render(graphics);
			}
		});
	}
}
