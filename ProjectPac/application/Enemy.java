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
	private int isScaredTimer = 0; // Is only used for the scared behaviour. Used to give some hysteresis
	private int chaseTimer = 0; // Is used to make ambushers break away after pursuing for a while
	private AmbusherState ambusherState;
	private int ambushTimer = 0;

	public static enum Intelligence {
		RANDOM, 	// makes correct choice 25% of the time
		DUMB,		// makes correct choice 50% of the time
		MODERATE, 	// makes correct choice 65% of the time
		SMART,		// makes correct choice 85% of the time
		PERFECT,	// makes correct choice 100% of the time
	}

	public static enum Behaviour {
		HUNTER, // always chases the player
		AMBUSHER, //tries to end up in front of the player
		GUARD, // guards a specific tile, pursues player if within x blocks
		/*NOT FULLY IMPLEMENTED*/ patrol, // Ran out of time :( moves between an array of tiles, chases the player if they get too close
		SCARED,	// moves randomly(?), but runs away if too close to the player
	}
	public static enum Algorithm {
		EUCLIDEAN, // moves in the direction that reduces the euclidean distance to the player the most, can run into dead ends
		BFS, // Uses breadth-first search to find 'a' path
		DFS, // Uses depth first search to find a not great path
		DIJKSTRA, // Uses dijkstra's to calculate shortest path
	}

	public static enum AmbusherState {
		RETREAT, // selecting a random position on the map
		REPOSITION, // moving to that position
		AMBUSH, //aiming ahead of the player
		CHASE, // aiming at the player
	}

	public Enemy(Shape model, double speed, Intelligence intelligence, Behaviour behaviour, Algorithm algorithm) { // If we have an enemy model already
		super(model, speed);
		nextMoves = new ArrayList<Main.Direction>();
		this.regularColor = (Color) model.getFill();
		this.intelligence = intelligence;
		this.behaviour = behaviour;
		this.algorithm = algorithm;

		if (behaviour == Behaviour.AMBUSHER) {
			ambusherState = AmbusherState.RETREAT;
		}
	}

	public Enemy(double speed, Color color) { // Otherwise make it a triangle
		super(new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0), speed);
		nextMoves = new ArrayList<Main.Direction>();
		model.setFill(color);
		
		
		intelligence = Intelligence.MODERATE;
		behaviour = Behaviour.HUNTER;
		algorithm = Algorithm.DIJKSTRA;
	}

	public Enemy(double speed, Color color, Intelligence intelligence, Behaviour behaviour, Algorithm algorithm) { // Otherwise make it a triangle
		super(new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0), speed);
		nextMoves = new ArrayList<Main.Direction>();

		this.regularColor = color;
		this.intelligence = intelligence;
		this.behaviour = behaviour;
		this.algorithm = algorithm;

		if (behaviour == Behaviour.AMBUSHER) {
			ambusherState = AmbusherState.RETREAT;
		}
	}

	public Main.Direction popNextMove() {
		if (nextMoves.isEmpty()) {
			return null;
		}
		Main.Direction rightMove = nextMoves.remove(0);
		double lowerBound;

		switch(intelligence) {
			case RANDOM:{lowerBound = 0.25; break;}
			case DUMB:{lowerBound = 0.50; break;}
			case MODERATE:{lowerBound = 0.65; break;}
			case SMART:{lowerBound = 0.85; break;}
			case PERFECT:{return rightMove;}
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
			case AMBUSH:{
				if (nextMoves.size() < 6 || ambushTimer > 30) {
					//If close to the player, stop trying to cut them off and just chase
					System.out.println("Chasing the player!");
					ambushTimer = 0;
					ambusherState = AmbusherState.CHASE;
				}
				else {
					ambushTimer++;
				}
				break;
			}
			case CHASE:{
				if (chaseTimer >= 10) {
					//signal to pick a random spot on the map to move to so you might be able to ambush
					System.out.println("Choosing a random position to move to...");
					ambusherState = AmbusherState.RETREAT;
					chaseTimer = 0;
				}
				else {
					//Just keep chasing
					chaseTimer++;
				}
				break;
			}
			case RETREAT:{
				/*We've chosen a random position to move to in the datapath section*/
				System.out.println("Moving to selected random location...");
				ambusherState = AmbusherState.REPOSITION;
				break;
			}
			case REPOSITION:{
				if (nextMoves.size() == 0){
					System.out.println("Time to ambush!");
					ambusherState = AmbusherState.AMBUSH;
				}
			}
			default:{break;}

		}

	}


	public AmbusherState getAmbusherState() {
		return ambusherState;
	}

}
