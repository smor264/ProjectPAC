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

	public Wall(WallType walltype, Main.Direction orientation) {
		this.walltype = walltype;
		this.orientation = orientation;
		switch(walltype) {
			case end: {
				switch (orientation) {
					case up:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double[]{-5.0,-10.0, 5.0,-10.0, 5.0,5.0, 5.0,10.0, 5.0,5.0, 10.0,5.0, 5.0,5.0, -5.0,5.0});
						super.model = shape;
						break;
					}
					case down:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double[] {-5.0,-5.0, 5.0, -5.0, 5.0, 10.0, -5.0 , 10.0, 10.0,10.0, -5.0, 10.0});
						super.model = shape;
						break;
					}
					case left:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double[] {-10.0,-5.0, 5.0,-5.0, 5.0,5.0, 5.0,10.0, 5.0,5.0, 10.0,5.0, 5.0,5.0, -10.0,5.0});
						super.model = shape;
						break;
					}
					case right:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double[] {-5.0,-5.0, 10.0, -5.0, 10.0, 5.0, -5.0, 5.0, -5.0,10.0, -5.0,5.0});
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
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-10.0, 5.0, -10.0, 10.0,-10.0, 5.0,-10.0,  5.0, 10.0, -5.0, 10.0});
						super.model = shape;
						break;
					}
					case left:
					case right: {
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-10.0,-5.0, 10.0,-5.0, 10.0,5.0, 10.0,10.0, 10.0,5.0, -10.0,5.0});
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
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-10.0, 5.0,-10.0, 5.0,-5.0, 10.0,-5.0, 10.0,5.0, 10.0, 10.0,10.0,5.0, -10.0, 5.0, -10.0, -5.0, -5.0, -5.0});
						super.model = shape;
						break;
					}
					case down:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-10.0, -5.0, 10.0, -5.0, 10.0, 5.0, 5.0, 5.0, 5.0, 10.0, -5.0, 10.0, -5.0, 5.0, -10.0, 5.0});
						super.model = shape;
						break;
					}
					case left:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-5.0, -5.0,-10.0, 5.0,-10.0, 5.0,10.0, 10.0,10.0, 5.0,10.0, -5.0,10.0, -5.0,5.0, -10.0,5.0, -10.0,-5.0});
						super.model = shape;
						break;
					}
					case right:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-10.0, 5.0, -10.0, 5.0, -5.0, 10.0, -5.0, 10.0, 5.0, 5.0, 5.0, 5.0, 10.0, -5.0, 10.0});
						super.model = shape;
						break;
					}
				}
				break;
			}
			case cross:{
				Polygon shape = new Polygon();
				shape.setFill(Color.BLUE);
				shape.getPoints().addAll(new Double [] {-5.0,-10.0, 5.0, -10.0, 5.0, -5.0, 10.0, -5.0, 10.0, 5.0, 5.0, 5.0, 5.0, 10.0, -5.0, 10.0, -5.0, 5.0, -10.0, 5.0, -10.0, -5.0, -5.0, -5.0});
				super.model = shape;
				break;
			}
			case single:{
				Polygon shape = new Polygon();
				shape.setFill(Color.BLUE);
				shape.getPoints().addAll(new Double [] {-5.0,-5.0, 5.0,-5.0, 5.0,5.0, 10.0,10.0, 5.0,5.0, -5.0,5.0});
				super.model = shape;
				break;
			}

			case corner:{
				switch (orientation) {
					case left:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-10.0, 5.0,-10.0, 5.0,-5.0, 10.0, -5.0, 10.0, 5.0, -5.0, 5.0, -5.0,10.0,-5.0,5.0});
						super.model = shape;
						break;
					}
					case right:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-5.0, -5.0,-10.0, 5.0,-10.0, 10.0,-10.0, 5.0,-10.0, 5.0,5.0, 5.0, 10.0, 5.0,5.0, -10.0,5.0, -10.0,-5.0});
						super.model = shape;
						break;
					}
					case up:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-5.0,-5.0, 10.0,-5.0, 10.0,5.0, 5.0,5.0, 5.0,10.0, -5.0,10.0});
						super.model = shape;
						break;
					}
					case down:{
						Polygon shape = new Polygon();
						shape.setFill(Color.BLUE);
						shape.getPoints().addAll(new Double [] {-10.0,-5.0, 5.0,-5.0, 10.0,-5.0, 5.0,-5.0, 5.0,10.0, -5.0,10.0, -5.0,5.0, -10.0,5.0});
						super.model = shape;
						break;
					}
				}
				break;
			}


			case full:{
				Rectangle shape = new Rectangle(20,20, Color.BLUE);
				super.model = shape;
				break;
			}
			default:{return;}
		}
	}

}
