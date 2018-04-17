package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public abstract class Character extends LevelObject {

	protected double speed = 2;
	private int[] prevPos; // Previous index in the level array
	protected Main.Direction prevDirection; // Previous direction the character was moving
	private int[] startPosition = new int[2];
	private double defaultSpeed;
	protected Color regularColor;
	
	public Character(Shape model, double speed) {
		super(model);
		this.speed  = speed;
		this.defaultSpeed = speed;
		regularColor = (Color) model.getFill();
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
	public int[] getStartIndex() {
		return startPosition;
	}
	public void setStartIndex(int[] pos) {
		startPosition = pos;
	}
	public void setTempSpeed(int n){
		speed = n;
	}
	public void resetSpeed(){
		speed = defaultSpeed;
	}
	public void resetColor() {
		model.setFill(regularColor);
	}
	public Color getRegularColor(){
		return regularColor;
	}
	public void pointModel(Main.Direction dir) {
		switch(dir) {
			case UP:{ model.setRotate(270);break;}
			case DOWN:{ model.setRotate(90);break;}
			case LEFT:{ model.setRotate(180); break;}
			case RIGHT:{ model.setRotate(0); break;}
		}
	}
	
}
