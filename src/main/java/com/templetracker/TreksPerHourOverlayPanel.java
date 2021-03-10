package com.templetracker;

import com.templetracker.constructors.TempleTracker;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TreksPerHourOverlayPanel extends OverlayPanel
{

	private ArrayList<TempleTracker> TTList = new ArrayList<>();
	private long meanTime;
	private int meanPoints;

	public static final String TREKS_RESET = "Reset";

	@Inject
	private TreksPerHourOverlayPanel(TempleTrackerPlugin plugin)
	{
		super(plugin);

		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);

		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, TREKS_RESET,"Treks per hour"));
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

	public void reset() {
		TTList = new ArrayList<>();
		meanTime = 0;
		meanPoints = 0;
	}

	public void addTrek(TempleTracker tracker) {
		TTList.add(tracker);
		ArrayList<TempleTracker> tempTTList = TTList.stream()
			.filter(TT -> (TT.getRoute() == tracker.getRoute() && TT.getEndTime() > 0))
			.collect(Collectors.toCollection(ArrayList::new));

		if (tempTTList.size() > 1)
		{
			long tempAddTimes = 0;
			int tempAddPoints = tempTTList.get(0).getPoints();
			for (int i = 1; i < tempTTList.size(); i++)
			{
				tempAddTimes += (tempTTList.get(i).getEndTime() - tempTTList.get(i - 1).getEndTime());
				tempAddPoints += tempTTList.get(i).getPoints();
			}

			meanTime = tempAddTimes / (tempTTList.size() - 1);
			meanPoints = tempAddPoints / tempTTList.size();
		}
		else if (tempTTList.size() == 1 && TTList.size() >= 2) {
			System.out.println(TTList.toString());
			meanTime = TTList.get(TTList.size() - 1).getEndTime() - TTList.get(TTList.size() - 2).getEndTime();
			meanPoints = TTList.get(TTList.size() - 1).getPoints();
		}
		else {
			meanTime = 0;
		}

	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (meanTime > 0)
		{
			panelComponent.getChildren().clear();

			panelComponent.getChildren().add(TitleComponent.builder().text("Treks/Hour").build());
			addSpacer(3);

			addSplitPanel("Route " + TTList.get(TTList.size() - 1).getRoute() + ":",
				String.format("%.1f",(double)3600000/(double)meanTime));
			addSpacer(2);

			addSplitPanel("Avg Points:", Integer.toString(meanPoints));

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
