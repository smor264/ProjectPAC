package application;

import javafx.scene.shape.Shape;

public class Character extends LevelObject {

	protected double speed = 1;
	private int[] prevPos; // Previous index in the level array
	protected Main.Direction prevDirection; // Previous direction the character was moving
	private int[] startPosition = new int[2];
	public Character(Shape model, double speed) {
		super(model);
		this.speed  = speed;
	}
	
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	public int[] getPrevIndex() {
		return prevPos;
	}
	
	public void setPrevIndex(int x, int y) {
		prevPos = new int[] {x,y};
	}
	
	public Main.Direction getPrevDirection(){
		return prevDirection;
	}
	
	public void setPrevDirection(Main.Direction dir){
		prevDirection = dir;
	}
	public int[] getStartPosition() {
		return startPosition;
	}
	public void setStartIndex(int[] pos) {
		startPosition = pos;
	}
}
