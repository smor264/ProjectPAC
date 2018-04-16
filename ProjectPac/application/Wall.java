package application;

import javafx.scene.shape.Rectangle;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;


public class Wall extends LevelObject{

	public static enum WallType {
		single,
		end,
		straight,
		corner,
		tee,
		cross,
		full,
	}

	WallType walltype;
	Main.Direction orientation;

	public Wall(WallType walltype, Main.Direction orientation, Color colour) {
		this.walltype = walltype;
		this.orientation = orientation;
		switch(walltype) {
			case end: {
				switch (orientation) {
					case up:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double[]{-width/4.0,-width/2.0, width/4.0,-width/2.0, width/4.0,width/4.0, width/4.0,width/2.0, width/4.0,width/4.0, width/2.0,width/4.0, width/4.0,width/4.0, -width/4.0,width/4.0});
						super.model = shape;
						break;
					}
					case down:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double[] {-width/4.0,-width/4.0, width/4.0, -width/4.0, width/4.0, width/2.0, -width/4.0 , width/2.0, width/2.0,width/2.0, -width/4.0, width/2.0});
						super.model = shape;
						break;
					}
					case left:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double[] {-width/2.0,-width/4.0, width/4.0,-width/4.0, width/4.0,width/4.0, width/4.0,width/2.0, width/4.0,width/4.0, width/2.0,width/4.0, width/4.0,width/4.0, -width/2.0,width/4.0});
						super.model = shape;
						break;
					}
					case right:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double[] {-width/4.0,-width/4.0, width/2.0, -width/4.0, width/2.0, width/4.0, -width/4.0, width/4.0, -width/4.0,width/2.0, -width/4.0,width/4.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case straight:{
				switch (orientation) {
					case up:
					case down: {
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/2.0, width/4.0, -width/2.0, width/2.0,-width/2.0, width/4.0,-width/2.0,  width/4.0, width/2.0, -width/4.0, width/2.0});
						super.model = shape;
						break;
					}
					case left:
					case right: {
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/2.0,-width/4.0, width/2.0,-width/4.0, width/2.0,width/4.0, width/2.0,width/2.0, width/2.0,width/4.0, -width/2.0,width/4.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case tee:{
				switch (orientation) {
					case up:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/2.0, width/4.0,-width/2.0, width/4.0,-width/4.0, width/2.0,-width/4.0, width/2.0,width/4.0, width/2.0, width/2.0,width/2.0,width/4.0, -width/2.0, width/4.0, -width/2.0, -width/4.0, -width/4.0, -width/4.0});
						super.model = shape;
						break;
					}
					case down:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/2.0, -width/4.0, width/2.0, -width/4.0, width/2.0, width/4.0, width/4.0, width/4.0, width/4.0, width/2.0, -width/4.0, width/2.0, -width/4.0, width/4.0, -width/2.0, width/4.0});
						super.model = shape;
						break;
					}
					case left:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/4.0, -width/4.0,-width/2.0, width/4.0,-width/2.0, width/4.0,width/2.0, width/2.0,width/2.0, width/4.0,width/2.0, -width/4.0,width/2.0, -width/4.0,width/4.0, -width/2.0,width/4.0, -width/2.0,-width/4.0});
						super.model = shape;
						break;
					}
					case right:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/2.0, width/4.0, -width/2.0, width/4.0, -width/4.0, width/2.0, -width/4.0, width/2.0, width/4.0, width/4.0, width/4.0, width/4.0, width/2.0, -width/4.0, width/2.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case cross:{
				Polygon shape = new Polygon();
				shape.setFill(colour);
				shape.getPoints().addAll(new Double [] {-width/4.0,-width/2.0, width/4.0, -width/2.0, width/4.0, -width/4.0, width/2.0, -width/4.0, width/2.0, width/4.0, width/4.0, width/4.0, width/4.0, width/2.0, -width/4.0, width/2.0, -width/4.0, width/4.0, -width/2.0, width/4.0, -width/2.0, -width/4.0, -width/4.0, -width/4.0});
				super.model = shape;
				break;
			}
			case single:{
				Polygon shape = new Polygon();
				shape.setFill(colour);
				shape.getPoints().addAll(new Double [] {-width/4.0,-width/4.0, width/4.0,-width/4.0, width/4.0,width/4.0, width/2.0,width/2.0, width/4.0,width/4.0, -width/4.0,width/4.0});
				super.model = shape;
				break;
			}

			case corner:{
				switch (orientation) {
					case left:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/2.0, width/4.0,-width/2.0, width/4.0,-width/4.0, width/2.0, -width/4.0, width/2.0, width/4.0, -width/4.0, width/4.0, -width/4.0,width/2.0,-width/4.0,width/4.0});
						super.model = shape;
						break;
					}
					case right:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/4.0, -width/4.0,-width/2.0, width/4.0,-width/2.0, width/2.0,-width/2.0, width/4.0,-width/2.0, width/4.0,width/4.0, width/4.0, width/2.0, width/4.0,width/4.0, -width/2.0,width/4.0, -width/2.0,-width/4.0});
						super.model = shape;
						break;
					}
					case up:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/4.0,-width/4.0, width/2.0,-width/4.0, width/2.0,width/4.0, width/4.0,width/4.0, width/4.0,width/2.0, -width/4.0,width/2.0});
						super.model = shape;
						break;
					}
					case down:{
						Polygon shape = new Polygon();
						shape.setFill(colour);
						shape.getPoints().addAll(new Double [] {-width/2.0,-width/4.0, width/4.0,-width/4.0, width/2.0,-width/4.0, width/4.0,-width/4.0, width/4.0,width/2.0, -width/4.0,width/2.0, -width/4.0,width/4.0, -width/2.0,width/4.0});
						super.model = shape;
						break;
					}
				}
				break;
			}


			case full:{
				Rectangle shape = new Rectangle(20,20, colour);
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
