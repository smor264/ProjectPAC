package application;

import javafx.scene.shape.Rectangle;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;


public class Wall extends LevelObject{
	
	public static enum WallType {
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
						shape.setFill(Color.BLUE);;
						shape.getPoints().addAll(new Double[]{-5.0,-10.0, 5.0,-10.0, 5.0,5.0, -5.0,5.0});
						super.model = shape;
						break;
					}
					case down:{
						
						break;
					}
					case left:{
						
						break;
					}
					case right:{
						
						break;
					}
				}
			}
			case straight:{
				switch (orientation) {
					case up:
					case down: {
						break;
					}
					case left:
					case right: {
						
						break;
					}
				}
			}
			case tee:{
				switch (orientation) {
					case up:{
						
						break;
					}
					case down:{
						
						break;
					}
					case left:{
						
						break;
					}
					case right:{
						
						break;
					}
				}
			}
			case cross:{
				break;
			}
			case full:{
				Rectangle shape = new Rectangle(20,20, Color.BLUE);
				super.model = shape;
				System.out.println("Make a square!");
				break;
			}
			default:{return;}
		}
	}

}
