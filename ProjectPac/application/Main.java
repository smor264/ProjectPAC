package application;


import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class Main extends Application {
	public static int windowWidth = 1280;
	public static int windowHeight = 720;
	public static int[] centre = {windowWidth/2, windowHeight/2};
	public static int levelWidth = 10;
	public static int levelHeight = 10;
	public static int gridSquareSize = 20;
	public static int levelOffsetX = 100;
	public static int levelOffsetY = 100;
	public static enum Direction {
		up,
		down,
		left,
		right,
	}
	/*TEST VARIABLES*/
	/*
	Wall w = new Wall(Wall.WallType.full, Direction.up);
	LevelObject[][] pathTest = {{w,	 w,		w,	 w,	  w},
								{w, null, null, null, w},
								{w, null, 	w,  null, w},
								{w, null, 	w,  null, w},
								{w, null, null, null, w}, 
								{w,	 w,		w,	 w,	  w}};
	AdjacencyMatrix testAdjMatrix = new AdjacencyMatrix(pathTest);
	*/
	/**/
	
	
	
	private LevelObject[][] levelObjectArray = new LevelObject[levelWidth][levelHeight]; //Array storing all objects in the level (walls, pellets, enemies, player)
	private Player player = new Player(new Circle(10, Color.YELLOW), 2);
	private SetArrayList<Direction> directionArray = new SetArrayList<Direction>(); //Stores currently pressed buttons in chronological order (top = newest)
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>(); // Stores all enemies so we can loop through them for AI pathing

	private AdjacencyMatrix adjMatrix;
	private Group currentLevel = new Group(player.getModel());
	private Scene scene = new Scene(currentLevel, windowWidth, windowHeight, Color.GREY); //Scene is where all visible objects are stored to be displayed on the stage (i.e window)
	private Level test = new Level();


	private void initialiseLevel(Level level) {
		int[][] array = level.getArray();
		//System.out.println(array.length);
		//System.out.println(array[0].length);
		boolean playerExists = false;
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				if (array[j][i] == 1) { // Wall
					// Actual code

					ArrayList<Object> wallType = new ArrayList<Object>();
					wallType = determineWallType(array,i,j);

					/*
					Wall wall = new Wall((Wall.WallType)wallType.get(0), (Direction)wallType.get(1));
					levelObjectArray[i][j] = wall;

					wall.moveTo(LevelObject.height*i+levelOffsetY, LevelObject.width*j+levelOffsetX);
					currentLevel.getChildren().add(wall.getModel());
					*/

					// Test code
					//System.out.println(java.util.Arrays.asList(wallType.get(0), wallType.get(1)));
					Wall wall = new Wall((Wall.WallType) wallType.get(0),(Direction) wallType.get(1)); // Make all walls full type
					levelObjectArray[j][i] = wall;
					//Wall wall = new Wall(Wall.WallType.tee, Direction.left);

					//System.out.println("Making a wall at ypos:" + (LevelObject.height*i+levelOffsetY) + ", and xpos:" + (LevelObject.width*j+levelOffsetX));
					wall.moveTo(gridSquareSize*i+levelOffsetY, gridSquareSize*j+levelOffsetX);
					currentLevel.getChildren().add(wall.getModel());

					//System.out.println("Wall constructed and placed");

				}
				else if (array[j][i] == 2) { // player
					if (playerExists){
						throw new UnsupportedOperationException();
					}
					else {
						player.moveTo(gridSquareSize*i + levelOffsetY, gridSquareSize*j + levelOffsetX);
						playerExists = true;
						player.setPrevPos(i,j);
						System.out.println("Moving player to" + (gridSquareSize*i+levelOffsetY) + ", " + (gridSquareSize*j+levelOffsetX));
						levelObjectArray[j][i] = player;
					}
				}
				else if (array[j][i] == 3) { //Enemy
					Enemy enemy = new Enemy(1, Color.RED);
					enemy.moveTo(gridSquareSize*i+levelOffsetY, gridSquareSize*j+levelOffsetX);
					currentLevel.getChildren().add(enemy.getModel());
					enemyList.add(enemy);
					enemy.setPrevPos(i,j);
					levelObjectArray[j][i] = enemy;
				}
				else if(array[j][i] == 4 || array[j][i] == 5 || array[j][i] == 6) {
					PickUp pickUp = new PickUp(array[j][i]);
					pickUp.moveTo(gridSquareSize*i+levelOffsetY, gridSquareSize*j+levelOffsetX);
					currentLevel.getChildren().add(pickUp.getModel());
					levelObjectArray[j][i] = pickUp;
				}
			}
		}
	}



	@Override
	public void start(Stage primaryStage) {
		try {
			//System.out.println(testAdjMatrix.findDijkstraPath(new Integer[] {1,1}, new Integer[] {4,2}));
			initialiseLevel(test);

			adjMatrix = new AdjacencyMatrix(levelObjectArray);

			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
						case UP: { directionArray.append(Direction.up); break;}
						case DOWN: { directionArray.append(Direction.down); break;}
						case LEFT: { directionArray.append(Direction.left); break;}
						case RIGHT: { directionArray.append(Direction.right); break;}
						default: break;
					}
				}

			});

			scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
						case UP: { directionArray.remove(Direction.up); break; }
						case DOWN: { directionArray.remove(Direction.down); break; }
						case LEFT: { directionArray.remove(Direction.left); break; }
						case RIGHT: { directionArray.remove(Direction.right); break; }
						default: break;
					}
				}

			});


			primaryStage.show();
			primaryStage.setScene(scene);


			AnimationTimer timer = new AnimationTimer() {
				@Override
				public void handle(long now) {

					int[] delta = {0,0};
					delta = calculatePlayerMovement();
					player.moveBy(delta[0], delta[1]);
					try {
						for (int i=0; i< enemyList.size(); i++){
							delta = new int[] {0,0};
							delta = calculateEnemyMovement(enemyList.get(i));
							enemyList.get(i).moveBy(delta[0], delta[1]);
						}
					}
					catch(PlayerCaughtException e){
						System.out.println("CAUGHT!");
						this.stop();
					}
				}
			};

			timer.start();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private int[] calculateEnemyMovement(Enemy enemy) throws PlayerCaughtException {
		int[] delta = {0,0};
		
		// Is this enemy colliding with the player?
		if ((Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) < 10) && (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) < 10)) {
			throw new PlayerCaughtException();
		}
		
		// If enemy is aligned with grid, update the grid position
		if ( (enemy.getPosition()[0] % gridSquareSize == 0) && (enemy.getPosition()[1] % gridSquareSize == 0) ) {
			Integer xIndex = (int)(enemy.getPosition()[0] - levelOffsetX) / gridSquareSize;
			Integer yIndex = (int)(enemy.getPosition()[1] - levelOffsetY) / gridSquareSize;
			
			Integer playerXIndex = (int)((player.getPosition()[0] - levelOffsetX) / gridSquareSize);
			Integer playerYIndex = (int)((player.getPosition()[1] - levelOffsetY) / gridSquareSize);
			
			
			levelObjectArray[enemy.getPrevPos()[0]][enemy.getPrevPos()[1]] = null; //clear old player position in collision detection array
			enemy.setNextMoves(adjMatrix.findDijkstraPath(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex})); // set next moves to be the directions from enemy to player
			levelObjectArray[yIndex][xIndex] = enemy; // set new player position in array
			enemy.setPrevPos(xIndex, yIndex);

			//Choose new direction to move in
			//System.out.println("Attempting to move " + enemy.getNextMove());
			switch (enemy.popNextMove()) {
				case up: {
					if (test.getArray()[yIndex-1][xIndex] == 1) {break;} //Is this line ever needed?
					delta[1] = -(int)enemy.getSpeed();
					enemy.prevDirection = Direction.up; break;}
				case down:{
					if (test.getArray()[yIndex+1][xIndex] == 1) {break;}
					delta[1] = (int)enemy.getSpeed();
					enemy.prevDirection = Direction.down; break;}
				case left:{
					if (test.getArray()[yIndex][xIndex-1] == 1) {break;}
					delta[0] = -(int)enemy.getSpeed();
					enemy.prevDirection = Direction.left; break;}
				case right:{
					if (test.getArray()[yIndex][xIndex+1] == 1) {break;}
					delta[0] = (int)enemy.getSpeed();
					enemy.prevDirection = Direction.right; break;}
				default:{break;}

			}
			
		}
		else {
			switch(enemy.prevDirection) {
				case up:{ delta[1] = -(int)enemy.getSpeed(); break;}
				case down:{ delta[1] = (int)enemy.getSpeed(); break;}
				case left:{ delta[0] = -(int)enemy.getSpeed(); break;}
				case right:{ delta[0] = (int)enemy.getSpeed(); break;}
			}
		}

		return delta;
	}

	/*private void calculateCharacterMovement(Character character){
		if ( (character.getPosition()[0] % LevelObject.width == 0) && (character.getPosition()[1] % LevelObject.height == 0) ) {
			int xpos = (int)(character.getPosition()[0] - levelOffsetX) / LevelObject.width;
			int ypos = (int)(character.getPosition()[1] - levelOffsetY) / LevelObject.height;

			levelObjectArray[character.getPrevPos()[0]][character.getPrevPos()[1]] = null; //clear old player position in collision detection array
			levelObjectArray[xpos][ypos] = player; // set new player position in array
			character.setPrevPos(xpos, ypos);
		}
	}*/

	private int[] calculatePlayerMovement(){
		int[] delta = {0,0};
		// If player has aligned with the grid
		if ( (player.getPosition()[0] % gridSquareSize == 0) && (player.getPosition()[1] % gridSquareSize == 0) ) {
			int xIndex = (int)(player.getPosition()[0] - levelOffsetX) / gridSquareSize;
			int yIndex = (int)(player.getPosition()[1] - levelOffsetY) / gridSquareSize;

			levelObjectArray[player.getPrevPos()[0]][player.getPrevPos()[1]] = null; //clear old player position in collision detection array
			if(levelObjectArray[yIndex][xIndex] instanceof PickUp) {
				player.modifyScore(((PickUp)(levelObjectArray[yIndex][xIndex])).getScoreValue());
				currentLevel.getChildren().remove((levelObjectArray[yIndex][xIndex].getModel()));
				System.out.println(player.getScore());
				} // Adds to players score depending on type of pellet eaten

			levelObjectArray[yIndex][xIndex] = player; // set new player position in array
			player.setPrevPos(xIndex, yIndex);

			//Loop through the held movement keys in order of preference
			for (int n = 0; n< Integer.min(directionArray.size(), 2) ; n++) {
				// If movement key held, and not in corner of map
				if((directionArray.getNFromTop(n) == Direction.up) && (test.getArray()[yIndex-1][xIndex] != 1)) {
					delta[1] = -(int)player.getSpeed();
					player.prevDirection = Direction.up;
					break;
				}
				else if(directionArray.getNFromTop(n) == Direction.down  && (test.getArray()[yIndex+1][xIndex] != 1)) {
					delta[1] = (int)player.getSpeed();
					player.prevDirection = Direction.down;
					break;
				}
				else if(directionArray.getNFromTop(n) == Direction.left && (test.getArray()[yIndex][xIndex-1] != 1)) {
					delta[0] = -(int)player.getSpeed();
					player.prevDirection = Direction.left;
					break;
				}
				else if(directionArray.getNFromTop(n) == Direction.right && (test.getArray()[yIndex][xIndex+1] != 1)) {
					delta[0] = (int)player.getSpeed();
					player.prevDirection = Direction.right;
					break;
				}
			}

		}
		// If player not aligned with grid, continue in same direction.
		else {
			if (player.prevDirection == Direction.up) {
				delta[1] = -(int)player.getSpeed();
			}
			else if (player.prevDirection == Direction.down) {
				delta[1] = (int)player.getSpeed();
			}
			else if (player.prevDirection == Direction.left) {
				delta[0] = -(int)player.getSpeed();
			}
			else if (player.prevDirection == Direction.right) {
				delta[0] = (int)player.getSpeed();
			}
		}
		return delta;
	}

	public static void main(String[] args) {
		launch(args);
	}

	private ArrayList<Object> determineWallType(int[][] array, int i, int j) {
		boolean northNeighbour = false, southNeighbour = false, leftNeighbour = false, rightNeighbour = false;
		try {
			if (array[j-1][i] == 1) {
				northNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		try {
			if (array[j+1][i] == 1) {
				southNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		try {
			if (array[j][i-1] == 1) {
				leftNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}

		try {
			if (array[j][i+1] == 1) {
				rightNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}

		int numNeighbours = (northNeighbour ? 1:0) + (southNeighbour ? 1:0) + (leftNeighbour ? 1:0) + (rightNeighbour ? 1:0);
		ArrayList<Object> type = new ArrayList<Object>();

		switch(numNeighbours) {
			case (4):{
				type.add(Wall.WallType.cross);
				type.add(Direction.up);
				return type;
			}
			case (3): {
				type.add(Wall.WallType.tee);
				if (!northNeighbour) {
					type.add(Direction.down);
				}
				else if (!southNeighbour) {
					type.add(Direction.up);
				}
				else if (!leftNeighbour) {
					type.add(Direction.right);
				}
				else {
					type.add(Direction.left);
				}
				return type;
			}
			case (2): {
				if (northNeighbour && southNeighbour) {
					type.add(Wall.WallType.straight);
					type.add(Direction.up);
				}
				else if (leftNeighbour && rightNeighbour) {
					type.add(Wall.WallType.straight);
					type.add(Direction.right);
				}
				else {
					type.add(Wall.WallType.corner);
					if (southNeighbour && rightNeighbour) {
						type.add(Direction.up);
					}
					else if (leftNeighbour && southNeighbour) {
						type.add(Direction.down);
					}
					else if (northNeighbour && leftNeighbour) {
						type.add(Direction.right);
					}
					else {
						type.add(Direction.left);
					}
				}
				return type;
			}
			case (1): {
				type.add(Wall.WallType.end);
				if (northNeighbour) {
					type.add(Direction.up);
				}
				else if (southNeighbour) {
					type.add(Direction.down);
				}
				else if (leftNeighbour) {
					type.add(Direction.left);
				}
				else {
					type.add(Direction.right);
				}
				return type;
			}
			case(0): {
				type.add(Wall.WallType.single);
				type.add(Direction.up);
				return type;
			}
			default: {
				throw new UnsupportedOperationException();
			}
		}
	}
}
