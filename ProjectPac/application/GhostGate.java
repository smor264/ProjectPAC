package application;

import application.Main.Direction;
import javafx.scene.paint.Color;
/*Ghost Gate is a Wall that ghosts can move through, but the player can't. */
public class GhostGate extends Wall {

	public GhostGate(WallType walltype, Direction orientation) {
		super(walltype, orientation, Color.DIMGREY);
	}

}
