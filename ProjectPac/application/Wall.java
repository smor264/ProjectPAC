package application;

import javafx.scene.shape.Rectangle;

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

}
