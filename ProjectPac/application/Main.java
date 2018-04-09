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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
 * Make start menu and transition between levels
 * Implement more behaviour types
 * Think about flood-fill-esque algorithm for detecting surrounded wall pieces to make them look filled in rather than cross pieces
 * */

public class Main extends Application {
	//Unchanged
	public final static int windowWidth = 1280;
	public final static int windowHeight = 990;
	public final static int[] centre = {windowWidth/2, windowHeight/2};
	public final static int levelWidth = 27;
	public final static int levelHeight = 25;
	public final static int gridSquareSize = 36; // ONLY WORKS FOR EVEN NUMBERS
	public final static int levelOffsetX = 100;
	public final static int levelOffsetY = 100;

	//Managed Variables and Objects
	private int extraLives = 2;
	private LevelObject[][] levelObjectArray = new LevelObject[levelHeight][levelWidth]; //Array storing all objects in the level (walls, pellets, enemies, player)
	private Player player = new Player(playerCharacter.model(), 2, playerCharacter.ability());
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>(); // Stores all enemies so we can loop through them for AI pathing
	private AdjacencyMatrix adjMatrix; // Pathfinding array

	private int pelletsRemaining = 0;
	private boolean pausePressed = false;
	private boolean playerCanEatGhosts = false;
	private int playerPowerUpDuration = 10 * 60; // Powerup duration time in ticks
	private int playerPowerUpTimer = 0;// This counts down from playerPowerUpDuration to zero, at which point the powerup expires
	private int ateGhostScore = 200; //Score given for eating a ghost
	private double currentGameTick = 0;
	private double maxTime = 120 * 60;

	Laser laserFactory = new Laser();

	//Scenes and Panes
	private AnchorPane gameUI = new AnchorPane();
	private AnchorPane launchScreen = new AnchorPane();
	private Scene launchScene = new Scene(launchScreen, windowWidth, windowHeight);
	private Group currentLevel = new Group();
	private Scene gameScene = new Scene(gameUI, windowWidth, windowHeight, Color.GREY); //Scene is where all visible objects are stored to be displayed on the stage (i.e window)
	private Level level1 = new Level("level1");
	private Level levelTarget = new Level("target");
	private Level levelCastle = new Level("castle");



	//Overlays
	private Rectangle pauseScreen = new Rectangle(0,0, (double) windowWidth,(double) windowHeight); //Pause Overlay
	private Text pauseText = new Text("Paused");
	private StackPane pauseOverlay = new StackPane(pauseScreen, pauseText);
	private Rectangle startScreen = new Rectangle(0,0, (double) windowWidth, (double) windowHeight);
	private Text startText = new Text();
	private StackPane startOverlay = new StackPane(startScreen, startText);

	private AnimationTimer gameLoop;

	private ProgressBar timeBar = new ProgressBar();


	//FXML
	//Game FXML
	public FXMLController controller = new FXMLController();
	public Text currentScoreText = (Text) gameScene.lookup("#currentScoreText");
    public AnchorPane HUDBar = (AnchorPane) gameScene.lookup("#HUDBar");

    //Start Screen FXML
    public Button playButton = (Button) launchScene.lookup("#playButton");
    public Text currentAbility = (Text) gameScene.lookup("#currentAbility");
    public Text currentBoost = (Text) gameScene.lookup("#currentBoost");

	public static PlayerCharacter playerCharacter = PlayerCharacter.Robot;

	/**
	 * A list of all characters that the player can use.
	 * Each PlayerCharacter has a model (Shape) and an ability (Ability)
	 * */
	public static enum PlayerCharacter {
		PacMan (new Circle(gridSquareSize/2,Color.YELLOW), Player.Ability.eatGhosts),
		MsPacMan (new Circle(gridSquareSize/2, Color.LIGHTPINK), Player.Ability.eatGhosts),
		PacKid (new Circle(gridSquareSize/3, Color.GREENYELLOW), Player.Ability.wallJump),
		GlitchTheGhost (null, Player.Ability.eatSameColor),
		SnacTheSnake (new Rectangle(gridSquareSize, gridSquareSize,Color.SEAGREEN), Player.Ability.snake),
		Robot (new Rectangle(gridSquareSize/2, gridSquareSize/2, Color.DARKGREY), Player.Ability.gun);

		private final Shape model;
		private final Player.Ability ability;

		PlayerCharacter(Shape model, Player.Ability ability){
			this.model = model;
			this.ability = ability;
		}
		public Shape model() {return model;}
		public Player.Ability ability() {return ability;}
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
						player.setPrevIndex(xPos,yPos);
						player.setStartIndex(new int[] {xPos,yPos});
						playerExists = true;

						placeLevelObject(player, xPos, yPos);
					}
				}
				else if (array[yPos][xPos] < 0) { //Enemy
					Object[] characteristics = determineEnemyCharacteristics(-array[yPos][xPos]);
					Enemy enemy = new Enemy(2, enemyColors[enemyList.size()], (Enemy.Intelligence)characteristics[0], (Enemy.Behaviour)characteristics[1], (Enemy.Algorithm)characteristics[2]);
					enemyList.add(enemy);

					enemy.setPrevIndex(xPos, yPos);
					enemy.setStartIndex(new int[] {xPos, yPos});

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
		adjMatrix = new AdjacencyMatrix(levelObjectArray);
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


	/**
	 * Initializes the root game layout
	 */
	public void initRootGameLayout() {
		try {
			//Load root layout from XML file
			AnchorPane gameUI = (AnchorPane) FXMLLoader.load(getClass().getResource("GameUI.fxml"));
			gameUI.getChildren().add(currentLevel);
			gameScene.setRoot(gameUI);

		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initializes the root launch layout
	 * @throws IOException
	 */
	private void initRootLaunchLayout() {
		try {
		AnchorPane launchScreen = (AnchorPane) FXMLLoader.load(getClass().getResource("StartScreen.fxml"));
		launchScene.setRoot(launchScreen);
		//FXMLLoader loader = new FXMLLoader(getClass().getResource("StartScreen.fxml"));
		//loader.setController(controller);
		//launchScreen = loader.load();

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

	/**
	 * Loads the next level, clears previous level
	 * @param primaryStage
	 * @param newLevel
	 * @return
	 */
	private boolean loadNewLevel(Stage primaryStage,Level newLevel) {
		println("Hello from load new level");
		levelObjectArray = new LevelObject[levelHeight][levelWidth];
		currentLevel.getChildren().clear();
		initialiseLevel(levelCastle);
		currentGameTick = 0;
		currentLevel.getChildren().add(timeBar);
		return true;
	}

	/**
	 * Stops the gameLoop and handles game over UI
	 */
	private void gameOver() {
		println("GAME OVER!");
		player.setScore(0);
		gameLoop.stop();
	}
	private boolean showOverlay(StackPane overlay) {
		return currentLevel.getChildren().add(overlay);
	}
	private boolean hideOverlay(StackPane overlay) {
		return currentLevel.getChildren().removeAll(overlay);
	}

	private void initialiseOverlays(){
		pauseScreen.setFill(Color.BLACK);
		pauseScreen.setOpacity(0.8);
		pauseText.setFill(Color.WHITE);
		pauseText.setStyle("-fx-font: 24 arial;");

		startScreen.setFill(Color.BLACK);
		startScreen.setOpacity(0.8);
		startText.setFill(Color.WHITE);
		startText.setStyle("-fx-font: 24 arial;");
	}

	/**
	 * Loads , displays, and runs the game itself
	 * @param primaryStage
	 */
	private void game(Stage primaryStage) {
		try {
		initRootGameLayout();
		initialiseLevel(level1);
		initialiseOverlays();
		primaryStage.setScene(gameScene);
		primaryStage.show();

		println("Hello from game");
		//initialiseLevel(level);
		//initialiseOverlays();

		//Binds the variables to their FXML counter parts
		HUDBar = (AnchorPane) gameScene.lookup("#HUDBar");
		currentScoreText = (Text) gameScene.lookup("#currentScoreText");
		currentAbility = (Text) gameScene.lookup("#currentAbility");
		currentBoost = (Text) gameScene.lookup("#currentBoost");

		if ((gridSquareSize %2) == 0) {} else { throw new ArithmeticException("gridSquareSize can only be even"); }


		if(player != null && player.getScoreString() != null && currentScoreText != null) {
			currentScoreText.setText(player.getScoreString());
		}
		else {
			currentScoreText.setText("--");
		}
		if (player != null) {
			currentAbility.setText(playerCharacter.ability().text());
			currentBoost.setText("--");
		}


		//ProgressBar timeBar = new ProgressBar();
		currentLevel.getChildren().add(timeBar);

		adjMatrix = new AdjacencyMatrix(levelObjectArray);

		gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
					case W:
					case UP: { player.getHeldButtons().append(Direction.up); break;}

					case S:
					case DOWN: { player.getHeldButtons().append(Direction.down); break;}

					case A:
					case LEFT: { player.getHeldButtons().append(Direction.left); break;}

					case D:
					case RIGHT: { player.getHeldButtons().append(Direction.right); break;}

					case V:{ usePlayerAbility(); break; }

					case N:{
						loadNewLevel(primaryStage, levelTarget);
						break;
						}

					case PAGE_DOWN:{ currentGameTick = maxTime; break;}

					case ESCAPE:{ primaryStage.close(); break;}
					case P: { pausePressed = !pausePressed;
						if (pausePressed) {
							println("PAUSED!");
							showOverlay(pauseOverlay);
							}
						else {
							println("UNPAUSED!");
							hideOverlay(pauseOverlay);
						}
						break;
					}
					default: break;
				}
			}

		});

		gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
					case W:
					case UP: { player.getHeldButtons().remove(Direction.up); break; }

					case S:
					case DOWN: { player.getHeldButtons().remove(Direction.down); break; }

					case A:
					case LEFT: { player.getHeldButtons().remove(Direction.left); break; }

					case D:
					case RIGHT: { player.getHeldButtons().remove(Direction.right); break; }

					default: break;
				}
			}

		});


		//primaryStage.show();
		//primaryStage.setScene(gameScene);


		gameLoop = new AnimationTimer() {
			@Override
			public void handle(long now) {
				while (pausePressed) {
					return;
				}

				int[] delta = {0,0};
				//println("Hello from game loop");

				try {
					timeBar.setProgress(currentGameTick/(maxTime + 240));
					if(currentGameTick >= (maxTime + 240)) {
						print("Time's Up!");
						throw(new TimeOutException());}

					else {
						currentGameTick++;
					}

					/* Display the starting screen*/
					if (currentGameTick - 1 <= 240) {
						switch( (int)currentGameTick - 1 ) {
							case 0: {startText.setText("3!"); showOverlay(startOverlay); break;}
							case 60: {startText.setText("2!"); break;}
							case 120: {startText.setText("1!"); break;}
							case 180: {startText.setText("Go!"); break;}
							case 240: {hideOverlay(startOverlay); break;}
							default: {break;}
						}
						return;
					}

					try { delta = calculatePlayerMovement(); }
					catch(LevelCompleteException e1) {
						println("LEVEL COMPLETE!");
						this.stop();
						try {
							TimeUnit.SECONDS.sleep(1);
							currentLevel.getChildren().clear();
							initialiseLevel(level1);
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
					if (laserFactory.getAnimationTick() != null) {
						laserFactory.createNextLaserFrame();
					}

				} catch (TimeOutException e) {
					e.printStackTrace();
					gameOver();

				}

			}
		};

		gameLoop.start();

	} catch(Exception e) {
		e.printStackTrace();
	}
}

	protected void usePlayerAbility() {
		switch (player.getAbility()) {
			case eatGhosts:
			case eatSameColor:
			case snake:{return;}

			case gun:{
				if (player.getAbilityCharges() == 0) {
					return;
				}
				else {
					fireLaser();
				}

				break;
			}
			case wallJump:{
				if (player.getAbilityCharges() == 0) {
					return;
				}
				else {
					wallJump();
				}

				break;
			}
		}


	}
	private void wallJump() {
		// TODO Auto-generated method stub

	}
	private void fireLaser() {
		if (player.getAbilityCharges() <= 0) {
			// play error sound
			return;
		}
		Double width;
		Double height;
		Double xPos;
		Double yPos;
		boolean isHorizontal = true;

		if (player.getHeldButtons().isEmpty()) {
			switch (player.getPrevDirection()) {
			case up:
			case down: {isHorizontal = false; break;}

			case left:
			case right: {isHorizontal = true; break;}
			default: {break;}
			}
		}
		else {
			switch(player.getHeldButtons().getTop()) {
				case up:
				case down:{isHorizontal = false; break;}

				case left:
				case right: {isHorizontal = true; break;}
				default: {break;}
			}
		}

		if (isHorizontal) {
			height = 1.5*gridSquareSize;
			xPos = 0.0;
			yPos = player.getPosition()[1] - height + 4;

		}
		else {
			width = 1.5*gridSquareSize;
			xPos = player.getPosition()[0] - width + 4;
			yPos = 0.0;
		}
		if (laserFactory.createNewLaser(xPos, yPos, isHorizontal)) {
			currentLevel.getChildren().add(laserFactory.getLaserGroup());
			player.decrementAbilityCharges();
			for (Enemy enemy : enemyList) {
				if (isHorizontal) {
					if (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) <= gridSquareSize * 1.5) {
						println(enemy.getPosition()[1] - player.getPosition()[1] +", " + gridSquareSize*1.5);
						enemyKilled(enemy);
					}
				}
				else {
					if (Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) <= gridSquareSize * 1.5) {
						enemyKilled(enemy);
					}
				}
			}

		}
		else {
			//play error sound
		}

	}
	@Override
	public void start(Stage primaryStage) {
		try {

			initRootLaunchLayout();
			primaryStage.setScene(launchScene);
			primaryStage.show();

			playButton = (Button) launchScene.lookup("#playButton");
			playButton.setDefaultButton(true);
			playButton.setOnAction(e -> game(primaryStage));

		} catch(Exception e) {
			e.printStackTrace();
		}


	}



	private void enemyKilled(Enemy enemy) {
		player.modifyScore(ateGhostScore);
		enemy.moveTo(convertToPosition(enemy.getStartPosition()[0],true), convertToPosition(enemy.getStartPosition()[1],false));
	}

	private int[] calculateEnemyMovement(Enemy enemy) throws PlayerCaughtException {
		int[] delta = {0,0};

		// Is this enemy colliding with the player?
		if ((Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) < gridSquareSize/2) && (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) < gridSquareSize/2)) {
			if (playerCanEatGhosts == true) {
				enemyKilled(enemy);
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


			enemy.setPrevIndex(xIndex, yIndex);

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
				if (player == null) {
					print("Player is null");
				}
				if (currentScoreText == null) {
					print("currentScoreText is null");
				}
				currentScoreText.setText(player.getScoreString());

				System.out.println("Score: " + player.getScore());
				pelletsRemaining--;

				// Is the level complete?
				if (pelletsRemaining == 0) {
					throw new LevelCompleteException();
				}

				//Is this pickup a power pellet?
				if (((PickUp)(levelObjectArray[yIndex][xIndex])).getPickUpType() == PickUp.PickUpType.powerPellet) {
					//What ability does the player have?
					switch (player.getAbility()) {
						case eatGhosts:{
							playerCanEatGhosts = true;
							playerPowerUpTimer = playerPowerUpDuration;
							for (Enemy enemy : enemyList) {
								enemy.setColor(Color.DODGERBLUE);
								enemy.setSpeed(1);
							}
						}

						case wallJump:
						case gun: {player.incrementAbilityCharges(); break;}

						case snake:{ break;}
						case eatSameColor:{ break;}

					}

				}
			} // Adds to players score depending on type of pellet eaten

			levelObjectArray[yIndex][xIndex] = player; // set new player position in array
			player.setPrevIndex(xIndex, yIndex);

			//Loop through the held movement keys in order of preference
			for (int n = 0; n< Integer.min(player.getHeldButtons().size(), 2) ; n++) {
				// If movement key held, and not in corner of map
				if((player.getHeldButtons().getNFromTop(n) == Direction.up) ) {
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
				else if(player.getHeldButtons().getNFromTop(n) == Direction.down) {
					if ((yIndex != levelObjectArray.length - 1) && !(levelObjectArray[yIndex+1][xIndex] instanceof Wall)) {
						delta[1] = (int)player.getSpeed();
						player.setPrevDirection(Direction.down);
						break;
					}
					else if ((yIndex == levelObjectArray.length - 1) && !(levelObjectArray[0][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), levelOffsetY);
					}
				}
				else if(player.getHeldButtons().getNFromTop(n) == Direction.left) {
					if ((xIndex != 0) && !(levelObjectArray[yIndex][xIndex-1] instanceof Wall)) {
						delta[0] = -(int)player.getSpeed();
						player.setPrevDirection(Direction.left);
						break;
					}
					else if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof Wall)) {
						player.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
					}
				}
				else if(player.getHeldButtons().getNFromTop(n) == Direction.right) {
					if ((xIndex != levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][xIndex+1] instanceof Wall)) {
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
