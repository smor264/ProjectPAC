package application;

import java.util.ArrayList;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Enemy extends Character {
	
	private ArrayList<Main.Direction> nextMoves;
	
	private Intelligence intelligence;
	private Behaviour behaviour;
	private Algorithm algorithm;
	
	public static enum Intelligence {
		random, 	// makes correct choice 25% of the time
		dumb,		// makes correct choice 50% of the time
		moderate, 	// makes correct choice 65% of the time
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
	
	public Enemy(Shape model, double speed) { // If we have an enemy model already
		super(model, speed);
		nextMoves = new ArrayList<Main.Direction>();
		intelligence = Intelligence.moderate;
		behaviour = Behaviour.hunter;
		algorithm = Algorithm.dijkstra;
	}
	
	public Enemy(double speed, Color color) { // Otherwise make it a triangle
		super(new Polygon(0.0,-10.0, 10.0,10.0, -10.0,10.0), speed);
		nextMoves = new ArrayList<Main.Direction>();
		model.setFill(color);
		
		intelligence = Intelligence.dumb;
		behaviour = Behaviour.hunter;
		algorithm = Algorithm.dijkstra;
	}
	
	public Enemy(double speed, Color color, Intelligence intelligence, Behaviour behaviour, Algorithm algorithm) { // Otherwise make it a triangle
		super(new Polygon(0.0,-10.0, 10.0,10.0, -10.0,10.0), speed);
		nextMoves = new ArrayList<Main.Direction>();
		model.setFill(color);
		
		this.intelligence = intelligence;
		this.behaviour = behaviour;
		this.algorithm = algorithm;
	}
	
	public Main.Direction popNextMove() {
		Main.Direction rightMove = nextMoves.remove(0);
		double lowerBound;

		switch(intelligence) {
			case random:{lowerBound = 0.25; break;}
			case dumb:{lowerBound = 0.50; break;}
			case moderate:{lowerBound = 0.65; break;}
			case smart:{lowerBound = 0.85; break;}
			case perfect:{return rightMove;}
			default:{throw new IllegalArgumentException("Invalid intelligence value");}
		}
		
		double randomVal = Math.random(); // between 0 -> 0.99999...
		
		if (randomVal < lowerBound) { // Pass test. Move correctly
			return rightMove;
		}
		else { //Fail, move in previously moved direction
			return prevDirection;
		}
	}
	
	public Intelligence getIntelligence() {
		return intelligence;
	}
	
	public Behaviour getBehaviour() {
		return behaviour;
	}
	
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	
	public Integer checkPathLength() {
		return nextMoves.size();
	}
	
	public void setNextMoves(ArrayList<Main.Direction> moves){
		nextMoves = moves;
	}
	public void setNextMove(Main.Direction move){
		nextMoves.clear();
		nextMoves.add(move);
	}

}
