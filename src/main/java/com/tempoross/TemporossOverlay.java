package com.tempoross;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.time.Instant;
import java.util.Set;

import static com.tempoross.TimerSwaps.*;

class TemporossOverlay extends Overlay
{
	private static final int MAX_DISTANCE = 3000;
	private static final int PIE_DIAMETER = 20;
	private static final float DOUBLE_SPOT_MOVE_MILLIS = 24000f;
	private static final int FIRE_ID = 37582;

	private final Set<Integer> FIRE_GAMEOBJECTS = ImmutableSet.of(
			FIRE_ID, NullObjectID.NULL_41006, NullObjectID.NULL_41007);

	private final Set<Integer> TETHER_GAMEOBJECTS = ImmutableSet.of(NullObjectID.NULL_41352,
			NullObjectID.NULL_41353, NullObjectID.NULL_41354, NullObjectID.NULL_41355, ObjectID.DAMAGED_MAST_40996,
			ObjectID.DAMAGED_MAST_40997, ObjectID.DAMAGED_TOTEM_POLE, ObjectID.DAMAGED_TOTEM_POLE_41011);

	private final Client client;
	private final TemporossPlugin plugin;
	private final TemporossConfig config;
	private final TextComponent textComponent = new TextComponent();

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
			if (FIRE_GAMEOBJECTS.contains(drawObject.getGameObject().getId()) && config.highlightFires() != TimerModes.OFF)
			{
				if (drawObject.getDuration() > 0 &&
						drawObject.getGameObject().getCanvasLocation() != null &&
						tile.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE && (config.highlightFires() == TimerModes.SECONDS || config.highlightFires() == TimerModes.TICKS))
				{
					long waveTimerMillis = (drawObject.getStartTime().toEpochMilli() + drawObject.getDuration()) - now.toEpochMilli();
					//modulo to recalculate fires timer after they spread
					waveTimerMillis = (((waveTimerMillis % drawObject.getDuration()) + drawObject.getDuration()) % drawObject.getDuration());

					renderTextElement(drawObject, waveTimerMillis, graphics, config.highlightFires());
				}
				else if (drawObject.getDuration() > 0 &&
						drawObject.getGameObject().getCanvasLocation() != null &&
						tile.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE && config.highlightFires() == TimerModes.PIE)
				{
					renderPieElement(drawObject, now, graphics);
				}
			}
			else if (TETHER_GAMEOBJECTS.contains(drawObject.getGameObject().getId()) && config.useWaveTimer() != TimerModes.OFF) //Wave and is not OFF
			{
				if (drawObject.getDuration() > 0 &&
						drawObject.getGameObject().getCanvasLocation() != null &&
						tile.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE && (config.useWaveTimer() == TimerModes.SECONDS || config.useWaveTimer() == TimerModes.TICKS))
				{
					long waveTimerMillis = (drawObject.getStartTime().toEpochMilli() + drawObject.getDuration()) - now.toEpochMilli();

					renderTextElement(drawObject, waveTimerMillis, graphics, config.useWaveTimer());
				}
				else if (drawObject.getDuration() > 0 &&
						drawObject.getGameObject().getCanvasLocation() != null &&
						tile.getLocalLocation().distanceTo(playerLocation) < MAX_DISTANCE && config.useWaveTimer() == TimerModes.PIE)
				{
					renderPieElement(drawObject, now, graphics);
				}
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

	private void renderTextElement(DrawObject drawObject, long timerMillis, Graphics2D graphics, TimerModes timerMode)
	{
		final String timerText;
		if (timerMode == TimerModes.SECONDS)
		{
			timerText = String.format("%.1f", timerMillis / 1000f);
		}
		else // TICKS
		{
			timerText = String.format("%d", timerMillis / 600);
		}
		textComponent.setText(timerText);
		textComponent.setColor(drawObject.getColor());
		textComponent.setPosition(new java.awt.Point(drawObject.getGameObject().getCanvasLocation().getX(), drawObject.getGameObject().getCanvasLocation().getY()));
		textComponent.render(graphics);
	}

	private void renderPieElement(DrawObject drawObject, Instant now, Graphics2D graphics)
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
}
