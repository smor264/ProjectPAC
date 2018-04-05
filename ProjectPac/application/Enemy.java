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
	private Color regularColor;
	private int isScaredTimer = 0; // Is only used for the scared behaviour. Used to give some hysteresis
	private int chaseTimer = 0; // Is used to make ambushers break away after pursuing for a while
	private AmbusherState ambusherState;
	private int ambushTimer = 0;
	
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
	
	public static enum AmbusherState {
		reposition1, // selecting a random position on the map
		reposition2, // moving to that position
		ambush, //aiming ahead of the player
		chase, // aiming at the player
	}
	
	public Enemy(Shape model, double speed) { // If we have an enemy model already
		super(model, speed);
		nextMoves = new ArrayList<Main.Direction>();
		intelligence = Intelligence.moderate;
		behaviour = Behaviour.hunter;
		algorithm = Algorithm.dijkstra;
		regularColor = (Color)model.getFill();
	}
	
	public Enemy(double speed, Color color) { // Otherwise make it a triangle
		super(new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0), speed);
		nextMoves = new ArrayList<Main.Direction>();
		model.setFill(color);
		
		intelligence = Intelligence.moderate;
		behaviour = Behaviour.hunter;
		algorithm = Algorithm.dijkstra;
		regularColor = color;
	}
	
	public Enemy(double speed, Color color, Intelligence intelligence, Behaviour behaviour, Algorithm algorithm) { // Otherwise make it a triangle
		super(new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0), speed);
		nextMoves = new ArrayList<Main.Direction>();
		model.setFill(color);
		
		this.intelligence = intelligence;
		this.behaviour = behaviour;
		this.algorithm = algorithm;
		regularColor = color;
		if (behaviour == Behaviour.ambusher) {
			ambusherState = AmbusherState.reposition1;
		}
	}
	
	public Main.Direction popNextMove() {
		if (nextMoves.isEmpty()) {
			return null;
		}
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
	
	public Integer getPathLength() {
		return nextMoves.size();
	}
	
	public void setNextMoves(ArrayList<Main.Direction> moves){
		nextMoves = moves;
	}
	
	public void setNextMove(Main.Direction move){
		nextMoves.clear();
		nextMoves.add(move);
	}
	
	public void setColor(Color color) {
		model.setFill(color);
	}
	
	public void resetColor() {
		model.setFill(regularColor);
	}
	public boolean isScared() {
		return (isScaredTimer == 0) ? false : true;
	}
	public void manageScared() {
		if (isScaredTimer == 0) {
			isScaredTimer = 7;
		}
		else {
			isScaredTimer--;
		}
	}
	
	/**
	 * Updates the ambusher FSM. Reposition -> Ambush -> Chase -> Reposition ...
	 * */
	public void manageAmbusherFSM() {
		switch(ambusherState) {
			case ambush:{
				if (nextMoves.size() < 6 || ambushTimer > 30) {
					//If close to the player, stop trying to cut them off and just chase
					System.out.println("Chasing the player!");
					ambushTimer = 0;
					ambusherState = AmbusherState.chase;
				}
				else {
					ambushTimer++;
				}
				break;
			}
			case chase:{
				if (chaseTimer >= 10) {
					//signal to pick a random spot on the map to move to so you might be able to ambush
					System.out.println("Choosing a random position to move to...");
					ambusherState = AmbusherState.reposition1;
					chaseTimer = 0;
				}
				else {
					//Just keep chasing
					chaseTimer++;
				}
				break;
			}
			case reposition1:{
				/*We've chosen a random position to move to in the datapath section*/
				System.out.println("Moving to selected random location...");
				ambusherState = AmbusherState.reposition2;
				break;
			}
			case reposition2:{
				if (nextMoves.size() == 0){
					System.out.println("Time to ambush!");
					ambusherState = AmbusherState.ambush;
				}
			}
			default:{break;}
		
		}
		
	}
	

	public AmbusherState getAmbusherState() {
		return ambusherState;
	}
	
}
