package application;

import java.util.LinkedList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Enemy extends Character{
	
	private LinkedList<Main.Direction> nextMoves;
	
	public Enemy(Shape model, double speed) {
		// TODO Auto-generated constructor stub
		super(model, speed);
		nextMoves = new LinkedList<Main.Direction>();
	}
	
	public Enemy(double speed, Color color) {
		// TODO Auto-generated constructor stub
		//Polygon model = new Polygon(0.0,10.0, 10.0,10.0, -10.0,10.0); 
		super(new Polygon(0.0,-10.0, 10.0,10.0, -10.0,10.0), speed);
		nextMoves = new LinkedList<Main.Direction>();
		model.setFill(color);
	}
	
	public LinkedList<Main.Direction> getNextMoves() {
		return nextMoves;
	}
	
	public void setNextMoves(LinkedList<Main.Direction> moves){
		nextMoves = moves;
	}

}
