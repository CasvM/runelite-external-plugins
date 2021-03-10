package com.templetracker.overlay;

import com.templetracker.constructors.Encounter;
import com.templetracker.constructors.TempleTracker;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TempleTrackerOverlayPanel extends OverlayPanel
{
	private final TempleTrackerPlugin plugin;

	@Inject
	private TempleTrackerOverlayPanel(TempleTrackerPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;

		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	private void addSplitPanel(String first, String second)
	{
		LineComponent splitComponent = LineComponent.builder().left(first).right(second).build();
		panelComponent.getChildren().add(splitComponent);
	}

	private void addSpacer(int height)
	{
		LayoutableRenderableEntity spacer = new LayoutableRenderableEntity()
		{
			@Override
			public Rectangle getBounds()
			{
				return new Rectangle(5,height);
			}

			@Override
			public void setPreferredLocation(Point position)
			{

			}

			@Override
			public void setPreferredSize(Dimension dimension)
			{
			}

			@Override
			public Dimension render(Graphics2D graphics)
			{
				return new Dimension(5,height);
			}
		};

		panelComponent.getChildren().add(spacer);

	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		TempleTracker tracker = plugin.getTracker();

		if (tracker.getRoute() > 0) {
			panelComponent.getChildren().add(TitleComponent.builder().text("Route " + tracker.getRoute()).build());
			addSpacer(3);
		}

		addSplitPanel("Current Points:", Integer.toString(Math.max(tracker.getPoints(), 0)));
		addSpacer(3);

		long endTime = tracker.getEndTime() > 0 ? tracker.getEndTime() : System.currentTimeMillis();
		addSplitPanel("Duration:", millisToMinuteString(endTime - tracker.getStartTime()));

		if (tracker.getLatestEncounter() != null || tracker.getEncounterList().size() > 0) {
			addSpacer(3);
			panelComponent.getChildren().add(LineComponent.builder().left("Encounters:").build());
			addSpacer(1);
			for (Encounter encounter : tracker.getEncounterList()) {
				addSplitPanel((encounter.getName().getName()), millisToMinuteString(encounter.getEndTime() - encounter.getStartTime()));
			}

			if (tracker.getLatestEncounter() != null) {
				endTime = tracker.getLatestEncounter().getEndTime() > 0 ? tracker.getLatestEncounter().getEndTime() : System.currentTimeMillis();
				addSplitPanel(tracker.getLatestEncounter().getName().getName(), millisToMinuteString(endTime - tracker.getLatestEncounter().getStartTime()));
			}
		}

		return super.render(graphics);
	}

	private static String millisToMinuteString(long time)
	{
		long minutes = (time / 1000) / 60;
		long seconds = (time / 1000) % 60;

		return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
	}

}
