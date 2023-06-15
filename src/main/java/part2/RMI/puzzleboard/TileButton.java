package part2.RMI.puzzleboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TileButton extends JButton {

	private boolean selected;

	public TileButton(final Tile tile) {
		super(new ImageIcon(tile.getImage()));
		selected = false;

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				selected = !selected;
				if(selected) {
					setBorder(BorderFactory.createLineBorder(Color.yellow, 3));
				} else {
					setBorder(BorderFactory.createLineBorder(Color.gray));
				}
			}
		});
	}

}
