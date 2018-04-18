package application;

import javafx.scene.shape.Rectangle;
import application.Main.Direction;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/** Parent class of GhostGate and SolidWall.
 * Can have many shapes, depending on the connectivity with neighbours.
 * */
public abstract class Wall extends LevelObject{

	public static enum WallType {
		SINGLE,
		END,
		STRAIGHT,
		CORNER,
		TEE,
		CROSS,
		SOLID,
	}

	WallType walltype;
	Main.Direction orientation;

	public Wall(WallType walltype, Main.Direction orientation, Color colour) {
		this.walltype = walltype;
		this.orientation = orientation;
		double w = this.width;
		switch(walltype) {
			case END: {
				Polygon shape = new Polygon();
				shape.setFill(colour);
				switch (orientation) {
					case UP:{
						shape.getPoints().addAll(new Double[]{-w/4.0,-w/2.0, w/4.0,-w/2.0, w/4.0,w/4.0, w/4.0,w/2.0, w/4.0,w/4.0, w/2.0,w/4.0, w/4.0,w/4.0, -w/4.0,w/4.0});
						super.model = shape;
						break;
					}
					case DOWN:{
						shape.getPoints().addAll(new Double[] {-w/4.0,-w/4.0, w/4.0, -w/4.0, w/4.0, w/2.0, -w/4.0 , w/2.0, w/2.0,w/2.0, -w/4.0, w/2.0});
						super.model = shape;
						break;
					}
					case LEFT:{
						shape.getPoints().addAll(new Double[] {-w/2.0,-w/4.0, w/4.0,-w/4.0, w/4.0,w/4.0, w/4.0,w/2.0, w/4.0,w/4.0, w/2.0,w/4.0, w/4.0,w/4.0, -w/2.0,w/4.0});
						super.model = shape;
						break;
					}
					case RIGHT:{

						shape.getPoints().addAll(new Double[] {-w/4.0,-w/4.0, w/2.0, -w/4.0, w/2.0, w/4.0, -w/4.0, w/4.0, -w/4.0,w/2.0, -w/4.0,w/4.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case STRAIGHT:{
				switch (orientation) {
					case UP:
					case DOWN: {
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/2.0, w/4.0, -w/2.0, w/2.0,-w/2.0, w/4.0,-w/2.0,  w/4.0, w/2.0, -w/4.0, w/2.0});
						super.model = shape;
						break;
					}
					case LEFT:
					case RIGHT: {
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/2.0,-w/4.0, w/2.0,-w/4.0, w/2.0,w/4.0, w/2.0,w/2.0, w/2.0,w/4.0, -w/2.0,w/4.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case TEE:{
				switch (orientation) {
					case UP:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/2.0, w/4.0,-w/2.0, w/4.0,-w/4.0, w/2.0,-w/4.0, w/2.0,w/4.0, w/2.0, w/2.0,w/2.0,w/4.0, -w/2.0, w/4.0, -w/2.0, -w/4.0, -w/4.0, -w/4.0});
						super.model = shape;
						break;
					}
					case DOWN:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/2.0, -w/4.0, w/2.0, -w/4.0, w/2.0, w/4.0, w/4.0, w/4.0, w/4.0, w/2.0, -w/4.0, w/2.0, -w/4.0, w/4.0, -w/2.0, w/4.0});
						super.model = shape;
						break;
					}
					case LEFT:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/4.0, -w/4.0,-w/2.0, w/4.0,-w/2.0, w/4.0,w/2.0, w/2.0,w/2.0, w/4.0,w/2.0, -w/4.0,w/2.0, -w/4.0,w/4.0, -w/2.0,w/4.0, -w/2.0,-w/4.0});
						super.model = shape;
						break;
					}
					case RIGHT:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/2.0, w/4.0, -w/2.0, w/4.0, -w/4.0, w/2.0, -w/4.0, w/2.0, w/4.0, w/4.0, w/4.0, w/4.0, w/2.0, -w/4.0, w/2.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case CROSS:{
				Polygon shape = new Polygon();
				shape.setFill(colour);
				shape.getPoints().addAll(new Double [] {-w/4.0,-w/2.0, w/4.0, -w/2.0, w/4.0, -w/4.0, w/2.0, -w/4.0, w/2.0, w/4.0, w/4.0, w/4.0, w/4.0, w/2.0, -w/4.0, w/2.0, -w/4.0, w/4.0, -w/2.0, w/4.0, -w/2.0, -w/4.0, -w/4.0, -w/4.0});
				super.model = shape;
				break;
			}
			case SINGLE:{
				Polygon shape = new Polygon();
				shape.setFill(colour);
				shape.getPoints().addAll(new Double [] {-w/4.0,-w/4.0, w/4.0,-w/4.0, w/4.0,w/4.0, w/2.0,w/2.0, w/4.0,w/4.0, -w/4.0,w/4.0});
				super.model = shape;
				break;
			}

			case CORNER:{
				switch (orientation) {
					case LEFT:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/2.0, w/4.0,-w/2.0, w/4.0,-w/4.0, w/2.0, -w/4.0, w/2.0, w/4.0, -w/4.0, w/4.0, -w/4.0,w/2.0,-w/4.0,w/4.0});
						super.model = shape;
						break;
					}
					case RIGHT:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/4.0, -w/4.0,-w/2.0, w/4.0,-w/2.0, w/2.0,-w/2.0, w/4.0,-w/2.0, w/4.0,w/4.0, w/4.0, w/2.0, w/4.0,w/4.0, -w/2.0,w/4.0, -w/2.0,-w/4.0});
						super.model = shape;
						break;
					}
					case UP:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/4.0,-w/4.0, w/2.0,-w/4.0, w/2.0,w/4.0, w/4.0,w/4.0, w/4.0,w/2.0, -w/4.0,w/2.0});
						super.model = shape;
						break;
					}
					case DOWN:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-w/2.0,-w/4.0, w/4.0,-w/4.0, w/2.0,-w/4.0, w/4.0,-w/4.0, w/4.0,w/2.0, -w/4.0,w/2.0, -w/4.0,w/4.0, -w/2.0,w/4.0});
						super.model = shape;
						break;
					}
				}
				break;
			}


			case SOLID:{
				Rectangle shape = new Rectangle(w,w, colour);
				super.model = shape;
				break;
			}
			default:{return;}
		}
		model.layoutXProperty().bind(container.layoutXProperty());
		model.layoutYProperty().bind(container.layoutYProperty());
		model.translateXProperty().bind(container.translateXProperty());
		model.translateYProperty().bind(container.translateYProperty());
		width = container.getBoundsInLocal().getWidth();
		height = container.getBoundsInLocal().getHeight();
		container.getChildren().add(model);
	}
	
	
	public static Object[] determineWallType(int[][] array, int i, int j) {
		boolean northNeighbour = false, southNeighbour = false, leftNeighbour = false, rightNeighbour = false;
		try {
			if (array[j-1][i] == 1) {
				northNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		try {
			if (array[j+1][i] == 1) {
				southNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		try {
			if (array[j][i-1] == 1) {
				leftNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}

		try {
			if (array[j][i+1] == 1) {
				rightNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}

		int numNeighbours = (northNeighbour ? 1:0) + (southNeighbour ? 1:0) + (leftNeighbour ? 1:0) + (rightNeighbour ? 1:0);
		Object[] type = new Object[2];

		switch(numNeighbours) {
			case (4):{
				type[0] = Wall.WallType.CROSS;
				type[1] = Direction.UP;
				break;
			}
			case (3): {
				type[0] = Wall.WallType.TEE;
				if (!northNeighbour) {
					type[1] = Direction.DOWN;}
				else if (!southNeighbour) {
					type[1] = Direction.UP;
				}
				else if (!leftNeighbour) {
					type[1] = Direction.RIGHT;
				}
				else {
					type[1] = Direction.LEFT;
				}
				break;
			}
			case (2): {
				if (northNeighbour && southNeighbour) {
					type[0] = Wall.WallType.STRAIGHT;
					type[1] = Direction.UP;
				}
				else if (leftNeighbour && rightNeighbour) {
					type[0] = Wall.WallType.STRAIGHT;
					type[1] = Direction.RIGHT;
				}
				else {
					type[0] = Wall.WallType.CORNER;
					if (southNeighbour && rightNeighbour) {
						type[1] = Direction.UP;
					}
					else if (leftNeighbour && southNeighbour) {
						type[1] = Direction.DOWN;
					}
					else if (northNeighbour && leftNeighbour) {
						type[1] = Direction.RIGHT;
					}
					else {
						type[1] = Direction.LEFT;
					}
				}
				break;
			}
			case (1): {
				type[0] = Wall.WallType.END;
				if (northNeighbour) {
					type[1] = Direction.UP;
				}
				else if (southNeighbour) {
					type[1] = Direction.DOWN;
				}
				else if (leftNeighbour) {
					type[1] = Direction.LEFT;
				}
				else {
					type[1] = Direction.RIGHT;
				}
				break;
			}
			case(0): {
				type[0] = Wall.WallType.SINGLE;
				type[1] = Direction.UP;
				break;
			}
			default: {throw new UnsupportedOperationException();}
		}
		return type;
	}

}
