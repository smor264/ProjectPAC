package application;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
/* Thoughts about random level generation:
 * - Every node must have at least two neighbours (degree two)
 * - No loops of four or less (girth of 5+)
 *
 * */
/*To Do List:
 * Add AI elements to Enemy, e.g intelligence, randomness, unique behaviour
 * Implement pause and end game
 * Make start menu and transition between levels
 * Implement more behaviour types
 * Think about flood-fill-esque algorithm for detecting surrounded wall pieces to make them look filled in rather than cross pieces
 * */

public class Main extends Application {
	public static int windowWidth = 1280;
	public static int windowHeight = 990;
	public static int[] centre = {windowWidth/2, windowHeight/2};
	public static int levelWidth = 27;
	public static int levelHeight = 25;
	public static int gridSquareSize = 38; // ONLY WORKS FOR EVEN NUMBERS
	public static int levelOffsetX = 100;
	public static int levelOffsetY = 60;
	public static PlayerCharacter playerCharacter = PlayerCharacter.PacKid;
	
	/**
	 * A list of all characters that the player can use.
	 * Each PlayerCharacter has a model (Shape) and an ability (Ability)
	 * */
	public static enum PlayerCharacter {
		PacMan (new Circle(gridSquareSize/2,Color.YELLOW), Ability.eatGhosts),
		MsPacMan (new Circle(gridSquareSize/2, Color.LIGHTPINK), Ability.eatGhosts),
		PacKid (new Circle(gridSquareSize/3, Color.GREENYELLOW), Ability.wallJump),
		GlitchTheGhost (null, Ability.eatSameColor),
		SnacTheSnake (new Rectangle(gridSquareSize, gridSquareSize,Color.SEAGREEN), Ability.snake),
		Robot (new Rectangle(gridSquareSize/2, gridSquareSize/2, Color.DARKGREY), Ability.gun);
		
		private final Shape model;
		private final Ability ability;
		
		PlayerCharacter(Shape model, Ability ability){
			this.model = model;
			this.ability = ability;
		}
		private Shape model() {return model;}
	public Text currentScoreText = new Text();
	public AnchorPane HUDBar = new AnchorPane();

	}
	
	/**
	 * A list of all valid directions characters can move
	 * */
	public static enum Direction {
		up,
		down,
		left,
		right,
	}
	
	/**
	 * PlayerCharacter-specific special actions 
	 * */
	public static enum Ability {
		eatGhosts,
		wallJump,
		gun,
		eatSameColor,
		snake,
	}
	
	/**
	 * Special actions usable by any PlayerCharacter
	 * */
	public static enum Boost {
		timeSlow, superTimeStop,
		dash, superDash,
		pelletMagnet, superPelletMagnet,
		invertControls,
		randomTeleport,
	}
	
	/**
	 * A list of colours that are given to enemies based on the order that they are initialised in the Level
	 * */
	public static Color[] enemyColors = {Color.RED, Color.DARKORANGE, Color.DARKMAGENTA, Color.DARKCYAN, Color.GREENYELLOW, Color.SPRINGGREEN};
	
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

	private void println() {
		System.out.println();
	}
	private void println(String str) {
		System.out.println(str); // because I'm tired of writing System.out.println
	}
	private void print(String str) {
		System.out.print(str); // because I'm tired of writing System.out.print
	}
	private int extraLives = 2;
	private LevelObject[][] levelObjectArray = new LevelObject[levelHeight][levelWidth]; //Array storing all objects in the level (walls, pellets, enemies, player)
	private Player player = new Player(playerCharacter.model(), 2);
	private SetArrayList<Direction> directionArray = new SetArrayList<Direction>(); //Stores currently pressed buttons in chronological order (top = newest)
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>(); // Stores all enemies so we can loop through them for AI pathing
	private AnchorPane gameUI = new AnchorPane();
	private AdjacencyMatrix adjMatrix;
	private Group currentLevel = new Group();
	private Scene scene = new Scene(gameUI, windowWidth, windowHeight, Color.GREY); //Scene is where all visible objects are stored to be displayed on the stage (i.e window)
	private Level test = new Level();                                      //  ^  This does nothing now, btw
	private int pelletsRemaining = 0;
	private boolean pausePressed = false;
	private boolean playerCanEatGhosts = false;
	private int playerPowerUpDuration = 10 * 60; // Powerup duration time in ticks
	private int playerPowerUpTimer = 0;// This counts down from playerPowerUpDuration to zero, at which point the powerup expires
	private int ateGhostScore = 200; //Score given for eating a ghost
	
	

	private int convertToIndex(double position, boolean isXCoord) {
		return (int)(position-(isXCoord ? levelOffsetX:levelOffsetY)) / gridSquareSize;
	}
	
	private double convertToPosition(int index, boolean isXCoord) {
		return (index * gridSquareSize) + (isXCoord ? levelOffsetX : levelOffsetY);
	}
	
	private Object[] determineEnemyCharacteristics(int num) {
		// Enemies have their characteristics encoded using prime factorisation. 
		// Every enemy is of the form 2^x x 3^y x 5^z, where the x is the intelligence, y is the behaviour, and z is the algorithm used
		Object[] array = new Object[3];
		
		int twoExponent = 0;
		while(num%2 == 0) {
			twoExponent++;
			num = num/2;
		}
		switch (twoExponent) {
			case 0: {array[0] = Enemy.Intelligence.dumb; break;}
			case 1: {array[0] = Enemy.Intelligence.moderate; break;}
			case 2: {array[0] = Enemy.Intelligence.smart; break;}
			case 3: {array[0] = Enemy.Intelligence.perfect; break;}
		}
		
		int threeExponent = 0;
		while (num%3 == 0) {
			threeExponent++;
			num = num/3;
		}
		switch (threeExponent) {
			case 0: {array[1] = Enemy.Behaviour.hunter; break;}
			case 1: {array[1] = Enemy.Behaviour.ambusher; break;}
			case 2: {array[1] = Enemy.Behaviour.guard; break;}
			case 3: {array[1] = Enemy.Behaviour.patrol; break;}
			case 4: {array[1] = Enemy.Behaviour.scared; break;}
		}
		
		int fiveExponent = 0;
		while (num%5 == 0) {
			fiveExponent++;
			num = num/5;
		}
		switch (fiveExponent) {
			case 0: {array[2] = Enemy.Algorithm.dijkstra; break;}
			case 1: {array[2] = Enemy.Algorithm.euclidean; break;}
			case 2: {array[2] = Enemy.Algorithm.bfs; break;}
			case 3: {array[2] = Enemy.Algorithm.dfs; break;}
		}
		
		return array;
	} 
	
	private void initialiseLevel(Level level) {
		int[][] array = level.getArray();
		enemyList.clear();
		boolean playerExists = false;
		for (int xPos = 0; xPos < array[0].length; xPos++) {
			for (int yPos = 0; yPos < array.length; yPos++) {
				if (array[yPos][xPos] == 1) { // Wall
					Object[] wallType;
					//ArrayList<Object> wallType = new ArrayList<Object>();
					wallType = determineWallType(array,xPos,yPos);
					Wall wall = new Wall( (Wall.WallType)wallType[0], (Direction)wallType[1]);

					placeLevelObject(wall, xPos, yPos);
				}
				else if (array[yPos][xPos] == 2) { // player
					if (playerExists){
						throw new UnsupportedOperationException();
					}
					else {
						player.setPrevPos(xPos,yPos);
						player.setStartPosition(new int[] {xPos,yPos});
						playerExists = true;

						placeLevelObject(player, xPos, yPos);
					}
				}
				else if (array[yPos][xPos] < 0) { //Enemy
					Object[] characteristics = determineEnemyCharacteristics(-array[yPos][xPos]);
					Enemy enemy = new Enemy(2, enemyColors[enemyList.size()], (Enemy.Intelligence)characteristics[0], (Enemy.Behaviour)characteristics[1], (Enemy.Algorithm)characteristics[2]);
					enemyList.add(enemy);
					
					enemy.setPrevPos(xPos, yPos);
					enemy.setStartPosition(new int[] {xPos, yPos});
					
					placeLevelObject(enemy, xPos, yPos);
				}
				else if(array[yPos][xPos] == 4 || array[yPos][xPos] == 5 || array[yPos][xPos] == 6) {
					PickUp pickUp = new PickUp(array[yPos][xPos]);
					pelletsRemaining++;
					placeLevelObject(pickUp, xPos, yPos);
				}
			}
			resetPlayerPowerUpState();
		}
		player.getModel().toFront(); // Draw player and enemies over top of pellets, etc.
		for (Enemy enemy: enemyList) {
			enemy.getModel().toFront();
		}
	}
	
	private void restartLevel() {
		int playerStartXPos = player.getStartPosition()[0];
		int playerStartYPos = player.getStartPosition()[1];

		player.moveTo(convertToPosition(playerStartXPos, true), convertToPosition(playerStartYPos, false));
		
		levelObjectArray[player.getPrevPos()[1]][player.getPrevPos()[0]] = null;
		levelObjectArray[playerStartYPos][playerStartXPos] = player;
		
		for (Enemy enemy : enemyList) {
			enemy.moveTo(convertToPosition(enemy.getStartPosition()[0], true), convertToPosition(enemy.getStartPosition()[1], false));	
		}
		resetPlayerPowerUpState();
	}

	private void placeLevelObject(LevelObject obj, int x, int y) { // Places objects (wall, pickups, player, enemies) in the level
		obj.moveTo(gridSquareSize*x + levelOffsetX, gridSquareSize*y + levelOffsetY);
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

		} catch(IOException e) {
			e.printStackTrace();
		}

	}
	
	private void resetPlayerPowerUpState() {
		playerCanEatGhosts = false; 
		playerPowerUpTimer = 0;
		for (Enemy enemy : enemyList) {
			enemy.resetColor();
			enemy.setSpeed(2);
			
			int[] delta = {0,0};
			if (((int)enemy.getPosition()[0] & 1) != 0) {
				// If horizontal position is odd
				delta[0] = 1;
				
			}
			if (((int)enemy.getPosition()[1] & 1) != 0) {
				// If position is odd
				delta[1] = 1;
			}
			enemy.moveBy(delta[0], delta[1]);
			
		}
	}

	@FXML
	//Prints when currentScoreText changes
	private void initialize(){
		currentScoreText.textProperty().addListener((orbservable, oldValue, newValue) -> {
		System.out.println("changed from " + oldValue + " to " + newValue);
		});
	}


	@Override
	public void start(Stage primaryStage) {
		try {
			if ((gridSquareSize %2) == 0) {} else { throw new ArithmeticException("gridSquareSize can only be even"); }
			initialiseLevel(test);
			initialize();

				if(player != null) {
				currentScoreText.setText(player.getScoreString());
					//currentScoreText.setStyle("-fx-font-size: 4em;");
					}
						else { currentScoreText.setText("");
					}


			
			adjMatrix = new AdjacencyMatrix(levelObjectArray);
			
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
						case UP: { directionArray.append(Direction.up); break;}
						case DOWN: { directionArray.append(Direction.down); break;}
						case LEFT: { directionArray.append(Direction.left); break;}
						case RIGHT: { directionArray.append(Direction.right); break;}
						case P: { pausePressed = !pausePressed; 
							if (pausePressed) {println("PAUSED!");} 
							else { println("UNPAUSED!"); }; 
							break;
						}
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
					while (pausePressed) {
						return;
					}
					
					int[] delta = {0,0};
					
					try { delta = calculatePlayerMovement(); }
					catch(LevelCompleteException e1) {
						println("LEVEL COMPLETE!");
						this.stop();
						try {
							TimeUnit.SECONDS.sleep(1);
							currentLevel.getChildren().clear();
							initialiseLevel(test);
							this.start();
							return;
						}
						catch(InterruptedException e2){
							Thread.currentThread().interrupt(); // I'm sure this does something, but right now it's just to stop the compiler complaining.
						}
					}
					
					if (playerPowerUpTimer == 0) { 
						resetPlayerPowerUpState();
					}
					else { 
						if ((playerPowerUpTimer < (2*60)) && (playerPowerUpTimer % 20 == 0)) {
							for (Enemy enemy :enemyList) {
								enemy.setColor(Color.WHITE);
							}
						}
						else if ((playerPowerUpTimer < (2*60)) && ((playerPowerUpTimer+10) % 20 == 0)) {
							for (Enemy enemy :enemyList) {
								enemy.setColor(Color.DODGERBLUE);
							}
						}
						playerPowerUpTimer--; 
					}
					
					player.moveBy(delta[0], delta[1]);
					
					try {
						for (int i=0; i< enemyList.size(); i++){
							delta = new int[] {0,0};
							delta = calculateEnemyMovement(enemyList.get(i));
							enemyList.get(i).moveBy(delta[0], delta[1]);
						}
					}
					catch(PlayerCaughtException e1){
						println("CAUGHT!");
						//this.stop();
						try {
							TimeUnit.SECONDS.sleep(1);
							extraLives--;
							
							if (extraLives < 0) {
								println("GAME OVER!");
								player.setScore(0);
								this.stop();
								return;
							}
							else if (extraLives == 0) {
								print("Careful! ");
							}
							print("You have " + extraLives + " extra lives remaining");
							println();
							restartLevel();
							this.start();
							return;

						} 
						catch (InterruptedException e2){
							Thread.currentThread().interrupt(); // I'm sure this does something, but right now it's just to stop the compiler complaining.
						}
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
		if ((Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) < gridSquareSize/2) && (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) < gridSquareSize/2)) {
			if (playerCanEatGhosts == true) {
				player.modifyScore(ateGhostScore);
				enemy.moveTo(convertToPosition(enemy.getStartPosition()[0],true), convertToPosition(enemy.getStartPosition()[1],false));
				println("Score: " + player.getScore());
			}
			else { throw new PlayerCaughtException(); }
		}

		// If enemy is aligned with grid, update the grid position
		if ( ((enemy.getPosition()[0] - levelOffsetX) % gridSquareSize == 0) && ((enemy.getPosition()[1]- levelOffsetY) % gridSquareSize == 0) ) {
			int xIndex = convertToIndex(enemy.getPosition()[0], true); //(int)(enemy.getPosition()[0] - levelOffsetX) / gridSquareSize;
			int yIndex = convertToIndex(enemy.getPosition()[1], false);//(int)(enemy.getPosition()[1] - levelOffsetY) / gridSquareSize;

			int playerXIndex = convertToIndex(player.getPosition()[0], true);// (int)((player.getPosition()[0] - levelOffsetX) / gridSquareSize);
			int playerYIndex = convertToIndex(player.getPosition()[1], false);// (int)((player.getPosition()[1] - levelOffsetY) / gridSquareSize);
			
			//Enemies aren't stored in levelObjectArray because they would overwrite pellets as they move, plus they don't need to be.
			
			
			//The beginning of more AI decisions goes here
			if (playerCanEatGhosts) {
				//Take the direction that maximises euclidean distance to the player
				enemy.setNextMove(adjMatrix.findEuclideanDirection(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}, false));
			}
			else {
				switch(enemy.getBehaviour()) {
					case hunter: {
						chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						break;
					}
					case ambusher: {
						switch (enemy.getAmbusherState()) {
							case reposition1:{							
								/* If it's now time to ambush, pick a random spot on the map to move to, from there we can try to cut the player off */
								//println("Choosing a random position to move to...");
								Random rand = new Random();
								int randXIndex; 
								int randYIndex;
								/*Check to see if this move is valid*/
								boolean validMove = true;
								do {
									validMove = true;
									randXIndex = rand.nextInt(levelWidth);
									randYIndex = rand.nextInt(levelHeight);
									try {
										if (levelObjectArray[randYIndex][randXIndex] instanceof Wall) {
											validMove = false;
										}
									}
									catch (Exception e) { validMove = false; }
								} while(!validMove);
								
								chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {randYIndex, randXIndex});
								enemy.manageAmbusherFSM();
								break;
							}
							case reposition2:{

								enemy.manageAmbusherFSM();
								break;
							}
							case ambush:{
								//If far away, try to cut the player off
								//use player direction to aim ahead of the player
								//println("Time to ambush!");
								int[] del = {0,0};
								if (player.getPrevDirection() != null) {
									switch (player.getPrevDirection()) {
										case up:    {del[1] = -3; break;}
										case down:  {del[1] = 3; break;}
										case left:  {del[0] = -3; break;}
										case right: {del[0] = 3; break;}
										default:    {break;}
									}
								}
								if (playerYIndex + del[1] > levelHeight || playerYIndex+del[1] < 0) {
									del[1] = 0;
								}
								if (playerXIndex + del[0] > levelWidth || playerXIndex+del[0] < 0) {
									del[0] =  0;
								}
								try {
									if (levelObjectArray[playerYIndex+del[1]][playerXIndex + del[0]] instanceof Wall) {
										break;
									}
								} catch(ArrayIndexOutOfBoundsException e){break;}
								chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex+del[1], playerXIndex + del[0]});
								
								enemy.manageAmbusherFSM();
								break;
							}
							case chase:{
								//println("Chasing the player...");
								chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
								enemy.manageAmbusherFSM();
								break;
							}
							default: {break;}
							
						}
						break;
					}
					case guard: {
						int guardRadius = 10;
						int guardYIndex = 5;
						int guardXIndex = 6;
						if (AdjacencyMatrix.calcDistance(new Integer[] {guardYIndex, guardXIndex}, new Integer[] {playerYIndex, playerXIndex}) < guardRadius) {
							// Is the player near my guard point? Chase them!
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						}
						else if (AdjacencyMatrix.calcDistance(new Integer[] {yIndex, xIndex}, new Integer[] {guardYIndex, guardXIndex}) > guardRadius) {
							// Am I far from my guard point? Move closer
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {guardYIndex, guardXIndex});
						}
						else {
							//I am near my guard point and the player is not. Do a random move?
							if (enemy.getPathLength() >= 1) {
								break;
							}
							Random random = new Random();
							
							int randomXCoord;
							int randomYCoord;
							boolean validMove = true;
							do {
								validMove = true;
								randomXCoord = random.nextInt(2*guardRadius) - guardRadius; // random index from (-guardRadius) to (guardRadius)
								randomYCoord = random.nextInt(2*guardRadius) - guardRadius;
								try{ 
									if (levelObjectArray[guardYIndex - randomYCoord][guardXIndex - randomXCoord] instanceof Wall) {
										validMove = false;
										//println((guardXIndex - randomXCoord) +", " + (guardYIndex - randomYCoord) + " is a wall, retrying...");
									}
								} 
								catch(ArrayIndexOutOfBoundsException e) {
									validMove = false;
									//println((guardXIndex - randomXCoord) +", " + (guardYIndex - randomYCoord) + " is outside the level bounds, retrying...");
								}
								
							} while (!validMove);
							//println("Looks like I'm gonna check out " + (guardXIndex - randomXCoord) + ", " + (guardYIndex - randomYCoord));
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {guardYIndex - randomYCoord, guardXIndex - randomXCoord});
						}
						break;
					}
					case indecisive: {break;}
					case patrol: {
						int aggroRadius = 4;
						if (AdjacencyMatrix.calcDistance(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}) < aggroRadius) {
							//If close to the player, chase
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						}
						else {
							//move between points
						}
						break;
					}
					case scared: { 
						int scaredRadius = 1;
						if (AdjacencyMatrix.calcDistance(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}) > scaredRadius && !enemy.isScared()) {
							//If the player is far away
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						}
						else {
							/* Choose the direction that maximises euclidean distance to the player */
							
							//This function acts as hysteresis, to keep the ghost scared for a few turns
							enemy.manageScared();
							
							enemy.setNextMove(adjMatrix.findEuclideanDirection(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}, false));
						}
						break;
					}
					default: { throw new IllegalArgumentException("Invalid behaviour specified");} 
				}
			}
			

			enemy.setPrevPos(xIndex, yIndex);

			//Choose new direction to move in
			//System.out.println("Attempting to move " + enemy.getNextMove());
			Direction next = enemy.popNextMove();
			if (next == null){
				return delta;
			}
			
			switch (next) {
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
					enemy.setPrevDirection(Direction.up); break;}
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
					enemy.setPrevDirection(Direction.down); break;}
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
					enemy.setPrevDirection(Direction.left); break;}
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
					enemy.setPrevDirection(Direction.right); break;}
				default:{ throw new IllegalArgumentException("No move to make!");}

			}

		}
		else { // Otherwise continue moving until you're aligned with the grid
			switch(enemy.getPrevDirection()) {
				case up:{ delta[1] = -(int)enemy.getSpeed(); break;}
				case down:{ delta[1] = (int)enemy.getSpeed(); break;}
				case left:{ delta[0] = -(int)enemy.getSpeed(); break;}
				case right:{ delta[0] = (int)enemy.getSpeed(); break;}
				default: { throw new IllegalArgumentException("prevDirection is undefined");}
			}
		}

		return delta;
	}

	private void chooseMoveFromAlgorithm(Enemy enemy, Integer[] source, Integer[] destination) {
		
		switch (enemy.getAlgorithm()) {
			case bfs:{
				// set next moves to be the directions from enemy to player
				enemy.setNextMoves(adjMatrix.findBFSPath(source, destination)); 
				break;
			}
			case dfs:{
				// Since DFS's paths are so windy, we actually need to let them complete before repathing
				if (enemy.getPathLength() == 0) {
					enemy.setNextMoves(adjMatrix.findDFSPath(source, destination));
				}
				break;
			}
			case dijkstra:{
				//System.out.println(targetXIndex + ", " + targetYIndex);
				enemy.setNextMoves(adjMatrix.findDijkstraPath(source, destination)); 
				break;
			}
	
			case euclidean:{
				enemy.setNextMove(adjMatrix.findEuclideanDirection(source, destination, true));
				break;
			}
			default:{throw new IllegalArgumentException("Invalid algorithm");}
		}
	}
	
	private int[] calculatePlayerMovement() throws LevelCompleteException{
		int[] delta = {0,0};
		// If player has aligned with the grid
		
		if ( ((player.getPosition()[0] - levelOffsetX) % gridSquareSize == 0) && ((player.getPosition()[1] - levelOffsetY) % gridSquareSize == 0) ) {
			int xIndex = convertToIndex(player.getPosition()[0], true);
			int yIndex = convertToIndex(player.getPosition()[1], false);

			levelObjectArray[player.getPrevPos()[1]][player.getPrevPos()[0]] = null; //clear old player position in collision detection array

			if(levelObjectArray[yIndex][xIndex] instanceof PickUp) {
				player.modifyScore(((PickUp)(levelObjectArray[yIndex][xIndex])).getScoreValue());
				currentLevel.getChildren().remove((levelObjectArray[yIndex][xIndex].getModel()));
				initialize();
				currentScoreText.setText(player.getScoreString());
				HUDBar.getChildren().remove(currentScoreText);
				HUDBar.getChildren().add(currentScoreText);
				System.out.println("Score: " + player.getScore());
				pelletsRemaining--;
				
				if (pelletsRemaining == 0) {
					throw new LevelCompleteException();
				}
				
				if (((PickUp)(levelObjectArray[yIndex][xIndex])).getPickUpType() == PickUp.PickUpType.powerPellet) {
					playerCanEatGhosts = true;
					playerPowerUpTimer = playerPowerUpDuration;
					for (Enemy enemy : enemyList) {
						enemy.setColor(Color.DODGERBLUE);
						enemy.setSpeed(1);
					}
				}
			} // Adds to players score depending on type of pellet eaten

			levelObjectArray[yIndex][xIndex] = player; // set new player position in array
			player.setPrevPos(xIndex, yIndex);

			//Loop through the held movement keys in order of preference
			for (int n = 0; n< Integer.min(directionArray.size(), 2) ; n++) {
				// If movement key held, and not in corner of map
				if((directionArray.getNFromTop(n) == Direction.up) ) {
					// If regular move...
					if ((yIndex != 0) && !(levelObjectArray[yIndex-1][xIndex] instanceof Wall)) {
						delta[1] = -(int)player.getSpeed();
						player.setPrevDirection(Direction.up);
						break;
					} // If wrapping around screen...
					else if ((yIndex == 0) && !(levelObjectArray[levelObjectArray.length-1][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), (levelObjectArray.length-1)*gridSquareSize + levelOffsetY);
					}
				}
				else if(directionArray.getNFromTop(n) == Direction.down) {
					if ((yIndex != levelObjectArray.length - 1) && (test.getArray()[yIndex+1][xIndex] != 1)) {
						delta[1] = (int)player.getSpeed();
						player.setPrevDirection(Direction.down);
						break;
					}
					else if ((yIndex == levelObjectArray.length - 1) && !(levelObjectArray[0][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), levelOffsetY);
					}
				}
				else if(directionArray.getNFromTop(n) == Direction.left) {
					if ((xIndex != 0) && (test.getArray()[yIndex][xIndex-1] != 1)) {
						delta[0] = -(int)player.getSpeed();
						player.setPrevDirection(Direction.left);
						break;
					}
					else if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof Wall)) {
						player.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
					}
				}
				else if(directionArray.getNFromTop(n) == Direction.right) {
					if ((xIndex != levelObjectArray[0].length - 1) && (test.getArray()[yIndex][xIndex+1] != 1)) {
						delta[0] = (int)player.getSpeed();
						player.setPrevDirection(Direction.right);
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
