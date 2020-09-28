package com.dpscalc;

import lombok.extern.slf4j.Slf4j;
import com.dpscalc.beans.EquipmentSlot;
import com.dpscalc.beans.EquipmentSlotItem;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DpsCalcPlayerItemPanel extends JPanel {
	private static final Dimension ICON_SIZE = new Dimension(32, 32);

	DpsCalcPlayerItemPanel(DpsCalcPlayerPanel panel, ImageIcon icon, String name, EquipmentSlot slot, EquipmentSlotItem equipmentSlotItem) {
		BorderLayout layout = new BorderLayout();
		layout.setHgap(5);
		setLayout(layout);
		if (equipmentSlotItem != null) {
			String tooltip = "<html>" +
					"Stab: " + equipmentSlotItem.getEquipment().getAttack_stab() + "<br>" +
					"Slash: " + equipmentSlotItem.getEquipment().getAttack_slash() + "<br>" +
					"Crush: " + equipmentSlotItem.getEquipment().getAttack_crush() + "<br>" +
					"Magic: " + equipmentSlotItem.getEquipment().getAttack_magic() + "<br>" +
					"Range: " + equipmentSlotItem.getEquipment().getAttack_ranged() + "<br><br>" +
					"Melee Strength: " + equipmentSlotItem.getEquipment().getMelee_strength() + "<br>" +
					"Magic Damage: " + equipmentSlotItem.getEquipment().getMagic_damage() + "<br>" +
					"Ranged Strength: " + equipmentSlotItem.getEquipment().getRanged_strength() +
					"</html>";
			setToolTipText(tooltip);
		}
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		Color background = getBackground();
		List<JPanel> panels = new ArrayList<>();
		panels.add(this);

		MouseAdapter itemPanelMouseListener = new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				for (JPanel panel : panels)
				{
					matchComponentBackground(panel, ColorScheme.DARK_GRAY_HOVER_COLOR);
				}
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				for (JPanel panel : panels)
				{
					matchComponentBackground(panel, background);
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (equipmentSlotItem == null) {
					panel.clearEquipmentSlot(slot);
				} else {
					panel.setEquipmentSlot(icon, slot, equipmentSlotItem);
				}
			}
		};

		addMouseListener(itemPanelMouseListener);

		setBorder(new EmptyBorder(5, 5, 5, 0));

		JLabel itemIcon = new JLabel();
		itemIcon.setPreferredSize(ICON_SIZE);
		if (icon != null)
		{
			itemIcon.setIcon(icon);
		}
		add(itemIcon, BorderLayout.LINE_START);

		JPanel rightPanel = new JPanel(new GridLayout(1, 1));
		panels.add(rightPanel);
		rightPanel.setBackground(background);

		// Item name
		JLabel itemName = new JLabel();
		itemName.setForeground(Color.WHITE);
		itemName.setBackground((Color.WHITE));
		itemName.setMaximumSize(new Dimension(0, 0));
		itemName.setPreferredSize(new Dimension(0, 0));
		itemName.setText(name);
		add(itemName, BorderLayout.CENTER);

		revalidate();
		repaint();
	}

	private void matchComponentBackground(JPanel panel, Color color)
	{
		panel.setBackground(color);
		for (Component c : panel.getComponents())
		{
			c.setBackground(color);
		}
	}
}