package application;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Enemy extends Character {
	
	private ArrayList<Main.Direction> nextMoves;
	
	public Enemy(Shape model, double speed) {
		// TODO Auto-generated constructor stub
		super(model, speed);
		nextMoves = new ArrayList<Main.Direction>();
	}
	
	public Enemy(double speed, Color color) {
		// TODO Auto-generated constructor stub
		//Polygon model = new Polygon(0.0,10.0, 10.0,10.0, -10.0,10.0); 
		super(new Polygon(0.0,-10.0, 10.0,10.0, -10.0,10.0), speed);
		nextMoves = new ArrayList<Main.Direction>();
		model.setFill(color);
	}
	
	public Main.Direction popNextMove() {
		return nextMoves.remove(0);
	}
	
	public Main.Direction getNextMove() {
		return nextMoves.get(0);
	}
	
	public void setNextMoves(ArrayList<Main.Direction> moves){
		nextMoves = moves;
	}

}
