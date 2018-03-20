package application;

import javafx.scene.shape.Shape;

public class Character extends LevelObject {

	private double speed = 1;

	public Character(Shape model, double speed) {
		super(model);
		this.speed  = speed;
	}
	
	public double getSpeed() {
		return speed;
	}

}
