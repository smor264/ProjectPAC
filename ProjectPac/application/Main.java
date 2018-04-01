package application;


import java.io.IOException;
import java.util.ArrayList;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
/* Thoughts about random level generation:
 * - Every node must have at least two neighbours (degree two)
 * - No loops of four or less (girth of 5+)
 *
 * */
/*To Do List:
 * Add AI elements to Enemy, e.g intelligence, randomness, unique behaviour
 * Implement pause and end game
 * Make start menu and transition between levels
 * 
 * 
 * */

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
	private AnchorPane gameUI = new AnchorPane();
	private AdjacencyMatrix adjMatrix;
	private Group currentLevel = new Group();
	private Scene scene = new Scene(gameUI, windowWidth, windowHeight, Color.GREY); //Scene is where all visible objects are stored to be displayed on the stage (i.e window)
	private Level test = new Level();                                      //  ^  This does nothing now, btw


	private int convertToIndex(double position, boolean isXCoord) {
		return (int)(position-(isXCoord ? levelOffsetX:levelOffsetY)) / gridSquareSize;
	}
	private double convertToPosition(int index, boolean isXCoord) {
		return (index * gridSquareSize) + (isXCoord ? levelOffsetX : levelOffsetY);
	}

	private void initialiseLevel(Level level) {
		int[][] array = level.getArray();
		boolean playerExists = false;
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				if (array[j][i] == 1) { // Wall
					Object[] wallType;
					//ArrayList<Object> wallType = new ArrayList<Object>();
					wallType = determineWallType(array,i,j);
					Wall wall = new Wall( (Wall.WallType)wallType[0], (Direction)wallType[1]);

					placeLevelObject(wall, i, j);
				}
				else if (array[j][i] == 2) { // player
					if (playerExists){
						throw new UnsupportedOperationException();
					}
					else {
						player.setPrevPos(j,i);
						playerExists = true;

						placeLevelObject(player, i, j);
					}
				}
				else if (array[j][i] == 3) { //Enemy
					Enemy enemy = new Enemy(1, Color.RED);
					enemyList.add(enemy);
					enemy.setPrevPos(j,i);

					placeLevelObject(enemy, i, j);
				}
				else if(array[j][i] == 4 || array[j][i] == 5 || array[j][i] == 6) {
					PickUp pickUp = new PickUp(array[j][i]);

					placeLevelObject(pickUp, i, j);
				}
			}
		}
		player.getModel().toFront(); // Draw player and enemies over top of pellets, etc.
		for (Enemy enemy: enemyList) {
			enemy.getModel().toFront();
		}
	}

	private void placeLevelObject(LevelObject obj, int x, int y) { // Places objects (wall, pickups, player, enemies) in the level
		obj.moveTo(gridSquareSize*x + levelOffsetY, gridSquareSize*y + levelOffsetX);
		levelObjectArray[y][x] = obj;
		currentLevel.getChildren().add(obj.getModel());
	}


	//Initializes the root layout
	public void initRootLayout() {
		try {
			//Load root layout from XML file
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("GameUI.fxml"));
			gameUI = loader.load();

			//Show the scene containing the root layout
			//primaryStage.setScene(scene);
			//primaryStage.show();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void start(Stage primaryStage) {
		try {
			//System.out.println(testAdjMatrix.findDijkstraPath(new Integer[] {1,1}, new Integer[] {4,2}));
			initialiseLevel(test);
			//testAdjMatrix.findDijkstraPath(new Integer[] {1,1}, new Integer[] {4,2});
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

			initRootLayout();
			gameUI.getChildren().add(currentLevel);
			scene.setRoot(gameUI);

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
	
	//private boolean flag = false;
	
	private int[] calculateEnemyMovement(Enemy enemy) throws PlayerCaughtException {
		int[] delta = {0,0};

		// Is this enemy colliding with the player?
		if ((Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) < 10) && (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) < 10)) {
			throw new PlayerCaughtException();
		}

		// If enemy is aligned with grid, update the grid position
		if ( (enemy.getPosition()[0] % gridSquareSize == 0) && (enemy.getPosition()[1] % gridSquareSize == 0) ) {
			Integer xIndex = convertToIndex(enemy.getPosition()[0], true); //(int)(enemy.getPosition()[0] - levelOffsetX) / gridSquareSize;
			Integer yIndex = convertToIndex(enemy.getPosition()[1], false);//(int)(enemy.getPosition()[1] - levelOffsetY) / gridSquareSize;

			Integer playerXIndex = convertToIndex(player.getPosition()[0], true);// (int)((player.getPosition()[0] - levelOffsetX) / gridSquareSize);
			Integer playerYIndex = convertToIndex(player.getPosition()[1], false);// (int)((player.getPosition()[1] - levelOffsetY) / gridSquareSize);

			//Enemies aren't stored in levelObjectArray because they would overwrite pellets as they move, plus they don't need to be.
			
			
			//The beginning of more AI decisions goes here
			switch (enemy.getAlgorithm()) {
				case bfs:{
					// set next moves to be the directions from enemy to player
					enemy.setNextMoves(adjMatrix.findBFSPath(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex})); 
					break;
				}
				case dfs:{
					// Since DFS's paths are so windy, we actually need to let them complete before repathing
					if (enemy.checkPathLength() == 0) {
						enemy.setNextMoves(adjMatrix.findDFSPath(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}));
					}
					break;
				}
				case dijkstra:{
					enemy.setNextMoves(adjMatrix.findDijkstraPath(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex})); 
					break;
				}
	
				case euclidean:{
					enemy.setNextMove(adjMatrix.findEuclideanDirection(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}));
					break;
				}
				default:{
					break;
				}

			}

			
			enemy.setNextMoves(adjMatrix.findDijkstraPath(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}));
			
			
			
			
			enemy.setPrevPos(yIndex, xIndex);

			//Choose new direction to move in
			//System.out.println("Attempting to move " + enemy.getNextMove());
			switch (enemy.popNextMove()) {
				case up: {
					//If wrapping around level...
					if ((yIndex == 0) && !(levelObjectArray[levelObjectArray.length-1][xIndex] instanceof Wall)){
						enemy.moveTo(convertToPosition(xIndex, true), convertToPosition(levelObjectArray.length-1, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex-1][xIndex] instanceof Wall) {break;}

					//Otherwise, regular move...
					delta[1] = -(int)enemy.getSpeed();
					enemy.prevDirection = Direction.up; break;}
				case down:{
					//If wrapping around level...
					if ((yIndex == levelObjectArray.length - 1) && !(levelObjectArray[0][xIndex] instanceof Wall)){
						enemy.moveTo(convertToPosition(xIndex, true), convertToPosition(0, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex+1][xIndex] instanceof Wall) {break;}

					//Otherwise, regular move...
					delta[1] = (int)enemy.getSpeed();
					enemy.prevDirection = Direction.down; break;}
				case left:{
					//If wrapping around level...
					if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof Wall)) {
						enemy.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex][xIndex-1] instanceof Wall) {break;}

					//Otherwise, regular move...
					delta[0] = -(int)enemy.getSpeed();
					enemy.prevDirection = Direction.left; break;}
				case right:{
					//If wrapping around level...
					if ((xIndex == levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][0] instanceof Wall)) {
						enemy.moveTo(convertToPosition(0, true), convertToPosition(yIndex, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex][xIndex+1] instanceof Wall) {break;}

					//Otherwise, regular move...
					delta[0] = (int)enemy.getSpeed();
					enemy.prevDirection = Direction.right; break;}
				default:{break;}

			}

		}
		else { // Otherwise continue moving until you're aligned with the grid
			switch(enemy.prevDirection) {
				case up:{ delta[1] = -(int)enemy.getSpeed(); break;}
				case down:{ delta[1] = (int)enemy.getSpeed(); break;}
				case left:{ delta[0] = -(int)enemy.getSpeed(); break;}
				case right:{ delta[0] = (int)enemy.getSpeed(); break;}
			}
		}

		return delta;
	}

	private int[] calculatePlayerMovement(){
		int[] delta = {0,0};
		// If player has aligned with the grid
		if ( (player.getPosition()[0] % gridSquareSize == 0) && (player.getPosition()[1] % gridSquareSize == 0) ) {
			int xIndex = convertToIndex(player.getPosition()[0], true);
			int yIndex = convertToIndex(player.getPosition()[1], false);

			levelObjectArray[player.getPrevPos()[0]][player.getPrevPos()[1]] = null; //clear old player position in collision detection array

			if(levelObjectArray[yIndex][xIndex] instanceof PickUp) {
				player.modifyScore(((PickUp)(levelObjectArray[yIndex][xIndex])).getScoreValue());
				currentLevel.getChildren().remove((levelObjectArray[yIndex][xIndex].getModel()));
				System.out.println(player.getScore());
			} // Adds to players score depending on type of pellet eaten

			levelObjectArray[yIndex][xIndex] = player; // set new player position in array
			player.setPrevPos(yIndex, xIndex);

			//Loop through the held movement keys in order of preference
			for (int n = 0; n< Integer.min(directionArray.size(), 2) ; n++) {
				// If movement key held, and not in corner of map
				if((directionArray.getNFromTop(n) == Direction.up) ) {
					// If regular move...
					if ((yIndex != 0) && !(levelObjectArray[yIndex-1][xIndex] instanceof Wall)) {
						delta[1] = -(int)player.getSpeed();
						player.prevDirection = Direction.up;
						break;
					} // If wrapping around screen...
					else if ((yIndex == 0) && !(levelObjectArray[levelObjectArray.length-1][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), (levelObjectArray.length-1)*gridSquareSize + levelOffsetY);
					}
				}
				else if(directionArray.getNFromTop(n) == Direction.down) {
					if ((yIndex != levelObjectArray.length - 1) && (test.getArray()[yIndex+1][xIndex] != 1)) {
						delta[1] = (int)player.getSpeed();
						player.prevDirection = Direction.down;
						break;
					}
					else if ((yIndex == levelObjectArray.length - 1) && !(levelObjectArray[0][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), levelOffsetY);
					}
				}
				else if(directionArray.getNFromTop(n) == Direction.left) {
					if ((xIndex != 0) && (test.getArray()[yIndex][xIndex-1] != 1)) {
						delta[0] = -(int)player.getSpeed();
						player.prevDirection = Direction.left;
						break;
					}
					else if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof Wall)) {
						player.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
					}
				}
				else if(directionArray.getNFromTop(n) == Direction.right) {
					if ((xIndex != levelObjectArray[0].length - 1) && (test.getArray()[yIndex][xIndex+1] != 1)) {
						delta[0] = (int)player.getSpeed();
						player.prevDirection = Direction.right;
						break;
					}
					else if ((xIndex == levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][0] instanceof Wall)) {
						player.moveTo(convertToPosition(0, true), convertToPosition(yIndex, false));
					}
				}
			}

		}
		else { // If player not aligned with grid, continue in same direction.
			switch (player.getPrevDirection()) {
				case up: {delta[1] = -(int)player.getSpeed(); break;}
				case down: {delta[1] = (int)player.getSpeed(); break;}
				case left: {delta[0] = -(int)player.getSpeed(); break;}
				case right: {delta[0] = (int)player.getSpeed(); break;}
				default: {break;}
			}
		}
		return delta;
	}

	public static void main(String[] args) {
		launch(args);
	}

	private Object[] determineWallType(int[][] array, int i, int j) {
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
		Object[] type = new Object[2];

		switch(numNeighbours) {
			case (4):{
				type[0] = Wall.WallType.cross;
				type[1] = Direction.up;
				return type;
			}
			case (3): {
				type[0] = Wall.WallType.tee;
				if (!northNeighbour) {
					type[1] = Direction.down;}
				else if (!southNeighbour) {
					type[1] = Direction.up;
				}
				else if (!leftNeighbour) {
					type[1] = Direction.right;
				}
				else {
					type[1] = Direction.left;
				}
				return type;
			}
			case (2): {
				if (northNeighbour && southNeighbour) {
					type[0] = Wall.WallType.straight;
					type[1] = Direction.up;
				}
				else if (leftNeighbour && rightNeighbour) {
					type[0] = Wall.WallType.straight;
					type[1] = Direction.right;
				}
				else {
					type[0] = Wall.WallType.corner;
					if (southNeighbour && rightNeighbour) {
						type[1] = Direction.up;
					}
					else if (leftNeighbour && southNeighbour) {
						type[1] = Direction.down;
					}
					else if (northNeighbour && leftNeighbour) {
						type[1] = Direction.right;
					}
					else {
						type[1] = Direction.left;
					}
				}
				return type;
			}
			case (1): {
				type[0] = Wall.WallType.end;
				if (northNeighbour) {
					type[1] = Direction.up;
				}
				else if (southNeighbour) {
					type[1] = Direction.down;
				}
				else if (leftNeighbour) {
					type[1] = Direction.left;
				}
				else {
					type[1] = Direction.right;
				}
				return type;
			}
			case(0): {
				type[0] = Wall.WallType.single;
				type[1] = Direction.up;
				return type;
			}
			default: {throw new UnsupportedOperationException();}
		}
	}


}
