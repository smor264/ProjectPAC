package application;

import javafx.scene.shape.Shape;

public class Character extends LevelObject {

	private double speed = 1;
	private int[] prevPos; // Previous index in the level array
	Main.Direction prevDirection; // Previous direction the character was moving
	public Character(Shape model, double speed) {
		super(model);
		this.speed  = speed;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public int[] getPrevPos() {
		return prevPos;
	}
	
	public void setPrevPos(int x, int y) {
		prevPos = new int[] {x,y};
	}
}
