package application;

import application.Main.Direction;
import javafx.scene.paint.Color;
/*A SolidWall is a wallpiece that neither the ghosts not the player can normally move through.*/
public class SolidWall extends Wall {

	public SolidWall(WallType walltype, Direction orientation, Color colour) {
		super(walltype, orientation, colour);
	}

}
