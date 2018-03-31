package application;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Enemy extends Character {
	
	private ArrayList<Main.Direction> nextMoves;
	
	private Intelligence intLevel;
	private Behaviour behaviourType;
	private Algorithm algorithm;
	
	public static enum Intelligence {
		random, 	// makes correct choice 25% of the time
		dumb,		// makes correct choice 40% of the time
		moderate, 	// makes correct choice 60% of the time
		smart,		// makes correct choice 85% of the time
		perfect,	// makes correct choice 100% of the time
	}
	
	public static enum Behaviour {
		indecisive, // type varies between other options e.g hunter -> ambusher -> scared -> etc
		hunter, // always chases the player
		ambusher, //tries to end up in front of the player
		guard, // guards a specific tile, pursues player if within x blocks
		patrol, // moves between a set of tiles, chases the player if they get too close
		scared,	// moves randomly(?), but runs away if too close to the player	
	}
	public static enum Algorithm {
		euclidean, // moves in the direction that reduces the euclidean distance to the player the most, can run into dead ends
		bfs, // Uses breadth-first search to find 'a' path
		dfs, // Uses depth first search to find a not great path
		dijkstra, // Uses dijkstra's to calculate shortest path
	}
	
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
