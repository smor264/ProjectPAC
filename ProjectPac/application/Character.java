package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class Character extends LevelObject {

	protected double speed = 2;
	private int[] prevPos; // Previous index in the level array
	protected Main.Direction prevDirection; // Previous direction the character was moving
	private int[] startPosition = new int[2];
	private double defaultSpeed;
	protected Color regularColor;
	private Shape mouth = new Polygon(-18.0,-18.0, 18.0,-18.0, (18),(0), 0.0,0.0, (18),(0), 18.0,18.0, -18.0,18.0 );
	
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
			case up:{ model.setRotate(270); mouth.setRotate(270); break;}
			case down:{ model.setRotate(90); mouth.setRotate(90); break;}
			case left:{ model.setRotate(180); mouth.setRotate(180); break;}
			case right:{ model.setRotate(0); mouth.setRotate(0); break;}
		}
	}
	public void manageAnimation(int animationFrame) {
		if (model instanceof Rectangle) {
			return;
		}
		double xPos;
		double yPos;
		double rotate = model.getRotate();
		if (animationFrame <= 18) {
			xPos = 4*((5.3/18.0) * animationFrame + 12.7);
			yPos = 4*((5.3/18.0) * animationFrame - 5.3);
		}
		else {
			xPos = 4*((-5.3/18.0) * animationFrame + 23.3);
			yPos = 4*((-5.3 / 18.0) * animationFrame + 5.3);
		}
		mouth = new Polygon(-18.0,-18.0, 18.0,-18.0, (xPos),(yPos), 0.0,0.0, (xPos),(-yPos), 18.0,18.0, -18.0,18.0 );
		Shape newModel = Shape.intersect(new Circle(width/2), mouth);
		this.setModel(newModel);
		model.setFill(regularColor);
		model.setRotate(rotate);
	}
}
