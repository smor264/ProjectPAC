package application;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import application.Player.Ability;
import application.Player.Boost;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
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
	public final static int levelOffsetX = 154+36;
	public final static int levelOffsetY = 100;

	//Managed Variables and Objects
	private int extraLives = 2;
	private LevelObject[][] levelObjectArray = new LevelObject[levelHeight][levelWidth]; //Array storing all objects in the level (walls, pellets, enemies, player)
	private Player player; //= new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>(); // Stores all enemies so we can loop through them for AI pathing
	private AdjacencyMatrix adjMatrix; // Pathfinding array
	private ArrayList<Player> playerList = new ArrayList<Player>();

	private int pelletsRemaining = 0;
	private boolean pausePressed = false;
	//private boolean playerCanEatGhosts = false;
	private int playerPowerUpDuration = 10 * 60; // Powerup duration time in ticks
	private int playerPowerUpTimer = 0;// This counts down from playerPowerUpDuration to zero, at which point the powerup expires
	private int ateGhostScore = 200; //Score given for eating a ghost
	private double currentGameTick = 0;
	private double maxTime = 120 * 60;

	private boolean playerIsWallJumping = false; // This should go in player eventually
	boolean isBoostActive = false;
	int boostDuration;
	boolean waitingForGridAlignment = false; // used for dash and super dash boosts
	public int changeColor = 0;

	int pelletPickupSize = 0; // goes in player eventually

	Laser laserFactory = new Laser();

	ArrayList<SnakePiece> snakePieces = new ArrayList<SnakePiece>(); // stores snake pieces if the player is snake
	Random rand = new Random();

	//Scenes and Panes
	private AnchorPane gameUI = new AnchorPane();
	private AnchorPane launchScreen = new AnchorPane();
	private Scene launchScene = new Scene(launchScreen, windowWidth, windowHeight);
	private Group currentLevel = new Group();
	private Scene gameScene = new Scene(gameUI, windowWidth, windowHeight, Color.GREY); //Scene is where all visible objects are stored to be displayed on the stage (i.e window)

	//Levels
	private String loadedLevelName;
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

    //Post level elements
    public Text postLevelTitle = new Text();

    public Button castleSelect = new Button("Castle");
    public Button targetSelect = new Button("Target");
    public Button givenBoostButton = new Button();
    public Button randomBoostButton = new Button("Random Boost");

	private HBox postLevelTitles = new HBox(25);
	private HBox postLevelElements = new HBox(25);
	private VBox postLevelScreen = new VBox(25);
	private Rectangle postLevelBackground = new Rectangle(800, 400);
    private StackPane postLevelOverlay = new StackPane(postLevelBackground, postLevelScreen);
    private GridPane worldMap = new GridPane();

	//FXML
	//Game FXML
	public FXMLController controller = new FXMLController();
	public Text currentScoreText = (Text) gameScene.lookup("#currentScoreText");
    public AnchorPane HUDBar = (AnchorPane) gameScene.lookup("#HUDBar");

    //Start Screen FXML
    public Button playButton;
    public Button exitButton;
    public Button loadSaveButton;
    public Button twoPlayerButton;
    public Button threePlayerButton;
    public Text currentAbility;
    public Text currentBoost;
    public Text currentSaveFileName;

    private File saveFile;



	private static Shape glitchTheGhostModel = new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0);
	private static Shape ghostModel = new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0);

	public  PlayerCharacter playerCharacter = PlayerCharacter.SnacTheSnake;

	public static enum GameMode {
		SinglePlayer,
		TwoPlayer,
		ThreePlayer;
	}

	public GameMode currentGameMode;


	/**
	 * A list of all characters that the player can use.
	 * Each PlayerCharacter has a model (Shape) and an ability (Ability)
	 * */
	public static enum PlayerCharacter {
		PacMan (new Circle(gridSquareSize/2,Color.YELLOW), Player.Ability.eatGhosts, 2),
		MsPacMan (new Circle(gridSquareSize/2, Color.LIGHTPINK), Player.Ability.eatGhosts, 2),
		PacKid (new Circle(gridSquareSize/3, Color.GREENYELLOW), Player.Ability.wallJump, 2),
		GlitchTheGhost (glitchTheGhostModel, Player.Ability.eatSameColor, 2),
		SnacTheSnake (new Rectangle(gridSquareSize, gridSquareSize,Color.SEAGREEN), Player.Ability.snake, 3),
		Robot (new Rectangle(gridSquareSize/2, gridSquareSize/2, Color.DARKGREY), Player.Ability.gun, 2);

		private final Shape model;
		private final Player.Ability ability;
		private final int speed;

		PlayerCharacter(Shape model, Player.Ability ability, int speed){
			this.model = model;
			this.ability = ability;
			this.speed = speed;
		}
		public Shape model() {return model;}
		public Player.Ability ability() {return ability;}
		public int speed() {return speed;}
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
	 * A list of colours that are given to enemies based on the order that they are initialised in the Level
	 * */
	public static Color[] enemyColors = {Color.RED, Color.DARKORANGE, Color.DARKMAGENTA, Color.DARKCYAN, Color.GREENYELLOW, Color.SPRINGGREEN};

	/*A Note about Positions and Indexes. Positions are in terms of onscreen pixel position, e.g (500px, 750px)
	 * Whereas Indexes are in terms of the level grid e.g (12, 23)*/

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
		Rectangle background = new Rectangle(windowWidth, windowHeight);
		background.setFill(level.getBackground());
		currentLevel.getChildren().add(background);
		background.toBack();
		background.setTranslateY(60);
		pelletsRemaining = 0;
		loadedLevelName = level.getLevelName();

		for (int xPos = 0; xPos < array[0].length; xPos++) {
			for (int yPos = 0; yPos < array.length; yPos++) {
				if (array[yPos][xPos] == 1) { // Wall
					Object[] wallType;
					//ArrayList<Object> wallType = new ArrayList<Object>();
					wallType = determineWallType(array,xPos,yPos);
					Wall wall = new Wall( (Wall.WallType)wallType[0], (Direction)wallType[1], level.getWallColor());

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
						playerList.add(player);

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


		if(currentGameMode != GameMode.SinglePlayer) {
			for(int i = 0; i < currentGameMode.ordinal(); i++) {
				Player playerGhost = new Player(new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0), playerCharacter.speed(), true,enemyColors[i]);
				playerList.add(playerGhost);
				playerGhost.moveTo(convertToPosition(enemyList.get(0).getStartIndex()[0],true), convertToPosition(enemyList.get(0).getStartIndex()[1],false));
				playerGhost.setStartIndex(enemyList.get(0).getStartIndex());
				currentLevel.getChildren().remove(enemyList.get(0).getModel());
				enemyList.remove(0);
				currentLevel.getChildren().add(playerGhost.getModel());
			}
		}

	}

	private void restartLevel() {
		int playerStartXPos = player.getStartIndex()[0];
		int playerStartYPos = player.getStartIndex()[1];

		player.moveTo(convertToPosition(playerStartXPos, true), convertToPosition(playerStartYPos, false));

		levelObjectArray[player.getPrevIndex()[1]][player.getPrevIndex()[0]] = null;
		levelObjectArray[playerStartYPos][playerStartXPos] = player;

		for (Enemy enemy : enemyList) {
			enemy.moveTo(convertToPosition(enemy.getStartIndex()[0], true), convertToPosition(enemy.getStartIndex()[1], false));
		}
		for (Player player : playerList) {
			player.moveTo(convertToPosition(player.getStartIndex()[0], true), convertToPosition(player.getStartIndex()[1], false));
		}
		resetPlayerPowerUpState();
		disableBoost();
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
	private void initRootLaunchLayout(Stage primaryStage) {
		try {
		AnchorPane launchScreen = (AnchorPane) FXMLLoader.load(getClass().getResource("StartScreen.fxml"));
		launchScene.setRoot(launchScreen);
		playButton = (Button) launchScene.lookup("#playButton");
		exitButton = (Button) launchScene.lookup("#exitButton");
		loadSaveButton = (Button) launchScene.lookup("#loadSaveFile");
		currentSaveFileName = (Text) launchScene.lookup("#currentSaveFileName");
		Button twoPlayerButton = (Button) launchScene.lookup("#twoPlayerButton");
		Button threePlayerButton = (Button) launchScene.lookup("#threePlayerButton");

		twoPlayerButton.setOnAction(e -> {
			currentGameMode = GameMode.TwoPlayer;
			player = new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());

			println("TwoPlayer Selected!");
			game(primaryStage);
		});

		threePlayerButton.setOnAction(e -> {
			currentGameMode = GameMode.ThreePlayer;
			player = new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());
			println("ThreePlayer Selected");
			game(primaryStage);
		});

		FileChooser fileChooser = new FileChooser();

		loadSaveButton.setOnAction(e -> {
			configureFileChooser(fileChooser);
			saveFile = fileChooser.showOpenDialog(primaryStage);
			currentSaveFileName.setText(saveFile.getName());
		});

		exitButton.setOnAction(e -> {primaryStage.close();});
		playButton.setDefaultButton(true);
		playButton.setOnAction(e -> {
			player = new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());
			game(primaryStage);
			});


		} catch(IOException e) {
			e.printStackTrace();
		}

	}


	private void configureFileChooser(FileChooser fileChooser) {
		fileChooser.setTitle("Choose your save file");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
	}

	private void resetPlayerPowerUpState() {
		playerPowerUpTimer = 0;
		playerIsWallJumping = false;
		for (Enemy enemy : enemyList) {
			enemy.resetColor();
			//println(Boolean.toString(isBoostActive));
			if (!isBoostActive) {
				enemy.resetSpeed();
			}

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

		for (int i = 1; i < playerList.size(); i++) {
			playerList.get(i).resetColor();
			//println(Boolean.toString(isBoostActive));
			if (!isBoostActive) {
				playerList.get(i).resetSpeed();
			}

			int[] delta = {0,0};
			if (((int)playerList.get(i).getPosition()[0] & 1) != 0) {
				// If horizontal position is odd
				delta[0] = 1;

			}
			if (((int)playerList.get(i).getPosition()[1] & 1) != 0) {
				// If position is odd
				delta[1] = 1;
			}
			playerList.get(i).moveBy(delta[0], delta[1]);
		}

		for (SnakePiece snakePiece : snakePieces) {
			currentLevel.getChildren().remove(snakePiece.getModel());
		}
		snakePieces.clear();
		player.resetSpeed();
	}

	/**
	 * Loads the next level, clears previous level
	 * @param primaryStage
	 * @param newLevel
	 * @return
	 */
	private boolean loadNewLevel(Stage primaryStage, Level newLevel) {
		hidePostLevelScreen();
		disableBoost();
		resetPlayerPowerUpState();
		levelObjectArray = new LevelObject[levelHeight][levelWidth];
		currentLevel.getChildren().clear();
		initialiseLevel(newLevel);
		currentGameTick = 0;
		currentLevel.getChildren().add(timeBar);
		currentBoost.setText(player.getBoost().toString());
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

	private boolean showPostLevelScreen() {
		int randBoostIndex = rand.nextInt(5) * 2;
		givenBoostButton.setText(Player.Boost.values()[randBoostIndex].text());
		givenBoostButton.setOnAction(e -> {player.setBoost(Player.Boost.values()[randBoostIndex]);} );

		return currentLevel.getChildren().add(postLevelOverlay);
	}

	private boolean hidePostLevelScreen() {
		return currentLevel.getChildren().removeAll(postLevelOverlay);
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
		initialiseLevel(level1);
		initRootGameLayout();
		initialiseOverlays();
		primaryStage.show();
		primaryStage.setScene(gameScene);
		//Binds the variables to their FXML counter parts
		HUDBar = (AnchorPane) gameScene.lookup("#HUDBar");
		currentScoreText = (Text) gameScene.lookup("#currentScoreText");
		currentAbility = (Text) gameScene.lookup("#currentAbility");
		currentBoost = (Text) gameScene.lookup("#currentBoost");


		if ((gridSquareSize %2) == 0) {} else { throw new ArithmeticException("gridSquareSize can only be even"); }

		if(player != null && player.getScoreString() != null && currentScoreText != null) {
			currentScoreText.setText(player.getScoreString());
			currentBoost.setText(player.getBoost().text());
		}
		else {
			currentScoreText.setText("--");
		}
		if (player != null) {
			currentAbility.setText(playerCharacter.ability().text());
			//currentBoost.setText("--");
		}
		currentLevel.getChildren().add(timeBar);
		gameScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				boolean inverted = player.getControlsInverted();
				switch(currentGameMode) {
				case SinglePlayer: {
					switch (event.getCode()) {
					case W:
					case UP: {
						player.getHeldButtons().append( (inverted ? Direction.down: Direction.up) ); break;
					}

					case S:
					case DOWN: { player.getHeldButtons().append( (inverted ? Direction.up: Direction.down) ); break;}

					case A:
					case LEFT: { player.getHeldButtons().append(inverted ? Direction.right : Direction.left); break;}

					case D:
					case RIGHT: { player.getHeldButtons().append(inverted ? Direction.left : Direction.right); break;}

					case C :{  if (currentGameTick >= 240) {usePlayerBoost();} break;}
					case V:{ if (currentGameTick >= 240) {usePlayerAbility(false);} break; }

					case N:{
						showPostLevelScreen();
						switch(loadedLevelName){
						case "level1": { targetSelect.setDisable(false); break;}
						case "target": {castleSelect.setDisable(false); break;}
						case "castle": {break;}
						default: throw new IllegalArgumentException("invalid level name");
						}

						gameLoop.stop();
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
					break;
				}
					case TwoPlayer: {
						switch (event.getCode()) {
						case W: {playerList.get(1).getHeldButtons().append(Direction.up);break;}
						case UP: {player.getHeldButtons().append( (inverted ? Direction.down: Direction.up) ); break;
						}

						case S: {playerList.get(1).getHeldButtons().append(Direction.down);break;}
						case DOWN: { player.getHeldButtons().append( (inverted ? Direction.up: Direction.down) ); break;}

						case A: {playerList.get(1).getHeldButtons().append(Direction.left);break;}
						case LEFT: { player.getHeldButtons().append(inverted ? Direction.right : Direction.left); break;}

						case D: {playerList.get(1).getHeldButtons().append(Direction.right);break;}
						case RIGHT: { player.getHeldButtons().append(inverted ? Direction.left : Direction.right); break;}
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
						}
						break;}
					case ThreePlayer: {
						switch (event.getCode()) {
						case W: {playerList.get(1).getHeldButtons().append(Direction.up);break;}
						case UP: {player.getHeldButtons().append( (inverted ? Direction.down: Direction.up) ); break;}
						case I: {playerList.get(2).getHeldButtons().append(Direction.up);break;}

						case S: {playerList.get(1).getHeldButtons().append(Direction.down);break;}
						case DOWN: { player.getHeldButtons().append( (inverted ? Direction.up: Direction.down) ); break;}
						case K: {playerList.get(2).getHeldButtons().append(Direction.down);break;}

						case A: {playerList.get(1).getHeldButtons().append(Direction.left);break;}
						case LEFT: { player.getHeldButtons().append(inverted ? Direction.right : Direction.left); break;}
						case J: {playerList.get(2).getHeldButtons().append(Direction.left);break;}

						case D: {playerList.get(1).getHeldButtons().append(Direction.right);break;}
						case RIGHT: { player.getHeldButtons().append(inverted ? Direction.left : Direction.right); break;}
						case L: {playerList.get(2).getHeldButtons().append(Direction.right);break;}
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
						}
					}

				default:
					break;
				}


			}
		});

		gameScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				boolean inverted = player.getControlsInverted();
				switch (currentGameMode) {
					case SinglePlayer:
						switch (event.getCode()) {
						case W:
						case UP: { player.getHeldButtons().remove((inverted ? Direction.down: Direction.up)); break; }

						case S:
						case DOWN: { player.getHeldButtons().remove((inverted ? Direction.up: Direction.down)); break; }

						case A:
						case LEFT: { player.getHeldButtons().remove((inverted ? Direction.right: Direction.left)); break; }

						case D:
						case RIGHT: { player.getHeldButtons().remove((inverted ? Direction.left: Direction.right)); break; }

						default: break;
						}
						break;
					case TwoPlayer:
						switch (event.getCode()) {
						case W: { playerList.get(1).getHeldButtons().remove(Direction.up); break; }
						case UP: { player.getHeldButtons().remove((inverted ? Direction.down: Direction.up)); break; }

						case S: { playerList.get(1).getHeldButtons().remove(Direction.down); break; }
						case DOWN: { player.getHeldButtons().remove((inverted ? Direction.up: Direction.down)); break; }

						case A: { playerList.get(1).getHeldButtons().remove(Direction.left); break; }
						case LEFT: { player.getHeldButtons().remove((inverted ? Direction.right: Direction.left)); break; }

						case D: { playerList.get(1).getHeldButtons().remove(Direction.right); break; }
						case RIGHT: { player.getHeldButtons().remove((inverted ? Direction.left: Direction.right)); break; }

						default: break;
						}
						break;

					case ThreePlayer:
						switch (event.getCode()) {
						case W: { playerList.get(1).getHeldButtons().remove(Direction.up); break; }
						case UP: { player.getHeldButtons().remove((inverted ? Direction.down: Direction.up)); break; }
						case I: { playerList.get(2).getHeldButtons().remove(Direction.up); break; }

						case S: { playerList.get(1).getHeldButtons().remove(Direction.down); break; }
						case DOWN: { player.getHeldButtons().remove((inverted ? Direction.up: Direction.down)); break; }
						case K: { playerList.get(2).getHeldButtons().remove(Direction.down); break; }

						case A: { playerList.get(1).getHeldButtons().remove(Direction.left); break; }
						case LEFT: { player.getHeldButtons().remove((inverted ? Direction.right: Direction.left)); break; }
						case J: { playerList.get(2).getHeldButtons().remove(Direction.left); break; }

						case D: { playerList.get(1).getHeldButtons().remove(Direction.right); break; }
						case RIGHT: { player.getHeldButtons().remove((inverted ? Direction.left: Direction.right)); break; }
						case L: { playerList.get(2).getHeldButtons().remove(Direction.right); break; }

						default: break;
						}
						break;

			}

			}});
			gameLoop = new AnimationTimer() {
				@Override
				public void handle(long now) {
					while (pausePressed) {
						return;
					}

					try {
						if ( !manageTime() ) {
							return;
						}
						if (pelletsRemaining == 0) {
							throw new LevelCompleteException();
						}
						int[] delta = {0,0};

						if (player.getAbility() == Player.Ability.eatSameColor) {
							if(currentGameTick%(2*60) == 0) {
								println("Time to change!");
								if(changeColor < enemyColors.length-1) { changeColor++; println("Colour Change!" + Integer.toString(changeColor));}
								else {changeColor = 0;}
								player.model.setFill(enemyColors[changeColor]);
							}
						}

						for(int i = 0; i < playerList.size(); i++) {
							delta = new int[] {0,0};
							delta = calculatePlayerMovement(playerList.get(i));
							playerList.get(i).moveBy(delta[0], delta[1]);
						}

						if (player.getAbility() == Player.Ability.snake) {
							manageSnake();
						}
						else if (player.getAbility() == Player.Ability.eatGhosts && player.isAbilityActive()) {
							manageEatGhosts();
						}


						for (int i=0; i< enemyList.size(); i++){
							delta = new int[] {0,0};
							delta = calculateEnemyMovement(enemyList.get(i));
							enemyList.get(i).moveBy(delta[0], delta[1]);
						}

						if (isBoostActive){
							if (waitingForGridAlignment && isGridAligned(player)) {
								if (player.getBoost() == Player.Boost.dash) {
									player.setTempSpeed(4);
								}
								else if (player.getBoost() == Player.Boost.superDash) {
									player.setTempSpeed(6);
								}
								waitingForGridAlignment = false;
							}
							else {
								boostDuration--;
								if (boostDuration == 0) {
									isBoostActive = false;
									disableBoost();
								}
							}
						}

						if (laserFactory.getAnimationTick() != null) {
							laserFactory.createNextLaserFrame();
						}
					}
					catch (GameFinishedException exception) {
						if (exception instanceof LossException) {
							try {
								if (extraLives < 0) {
									gameOver();
								}
								else {
									playerCaught();
								}
							}
							catch (InterruptedException e2){
								Thread.currentThread().interrupt(); // I'm sure this does something, but right now it's just to stop the compiler complaining.
							}
						}
						else if (exception instanceof WinException) {
							println("LEVEL COMPLETE!");
							this.stop();
							try {
								TimeUnit.SECONDS.sleep(1);

								switch(loadedLevelName){
									case "level1": { targetSelect.setDisable(false); break;}
									case "Target" : {castleSelect.setDisable(false); break;}
									case "Castle": {break;}
									default: throw new IllegalArgumentException("invalid level name");
								}

								//loadNewLevel(primaryStage, levelTarget);
								//this.start();
								showPostLevelScreen();
								return;
							}
							catch(InterruptedException e2){
								Thread.currentThread().interrupt(); // I'm sure this does something, but right now it's just to stop the compiler complaining.
							}
						}
						else {
							exception.printStackTrace();
						}
					}
				}
			};

			gameLoop.start();

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Circle createShield(Color color) {
		Circle shield = new Circle(gridSquareSize/1.5, color);
		shield.relocate(player.getPosition()[0] - shield.getRadius() - gridSquareSize/2, player.getPosition()[1] - shield.getRadius() - gridSquareSize/2 );
		shield.setOpacity(0.5);

		shield.layoutXProperty().bind(player.getModel().layoutXProperty());
		shield.layoutYProperty().bind(player.getModel().layoutYProperty());
		shield.translateXProperty().bind(player.getModel().translateXProperty());
		shield.translateYProperty().bind(player.getModel().translateYProperty());
		currentLevel.getChildren().add(shield);
		return shield;
	}
	private void deleteShield(){
		currentLevel.getChildren().remove(player.getShield());
		player.clearShield();
	}

	private void invertHeldKeys(){
		SetArrayList<Direction> heldKeys = player.getHeldButtons();
		SetArrayList<Direction> newHeldKeys = new SetArrayList<Direction>();
		for (int i = 0; i < heldKeys.size(); i++){
			Direction direction = heldKeys.getNFromTop(heldKeys.size() - (i + 1));
			Direction newDirection;
			switch (direction){
				case up:{newDirection = Direction.down; break;}
				case down:{newDirection = Direction.up; break;}
				case left:{newDirection = Direction.right; break;}
				case right:{newDirection = Direction.left; break;}
				default:{newDirection = Direction.up; break;}
			}
			newHeldKeys.append(newDirection);
		}
		player.setHeldButtons(newHeldKeys);
	}

	private void usePlayerBoost(){
		if (player.getBoostCharges() <= 0){
			return;
		}
		else {
			switch (player.getBoost()){
				case timeSlow:{slowTime(false); break;}
				case superTimeSlow:{slowTime(true); break;}

				/*We may end up misaligned if we change speed whilst not aligned with the grid, so set a flag and do it when we are aligned.*/
				case dash:
				case superDash:{ waitingForGridAlignment = true; break;}

				case pelletMagnet:{ pelletPickupSize = 2; break;}
				case superPelletMagnet:{ pelletPickupSize = 3; break;}

				case invisibility:
				case superInvisibility: {player.setInvisible(true); break;}

				case shield: {player.setShield(createShield(Color.BLUE)); break;}
				case superShield: {player.setShield(createShield(Color.BLUE)); break;}

				case invertControls:{
					player.setControlsInverted(true);
					invertHeldKeys();
					break;
				}
				case randomTeleport:{
					Integer[] pos;
					double minDist;
					double dist;

					do {
						minDist = Double.POSITIVE_INFINITY;
						pos = findRandomValidIndexes();
						for (Enemy enemy : enemyList){
							Integer[] enemyIndex = {convertToIndex(enemy.getPosition()[0], true), convertToIndex(enemy.getPosition()[1], false)};
							dist = AdjacencyMatrix.calcDistance(enemyIndex, pos);
							if (dist < minDist) {
								minDist = dist;
							}
						}
					} while (minDist < 4);
					player.moveTo(convertToPosition(pos[0], true), convertToPosition(pos[1], false));
					break;
				}
				case random: {
					Random rand = new Random();
					int val = rand.nextInt(Player.Boost.values().length - 1);
					player.setBoost(Player.Boost.values()[val]);
					currentBoost.setText(Player.Boost.values()[val].text());
					println("You got... " + Player.Boost.values()[val].text() + "!");

					//If it is a debuff, use it immediately
					if (val == 10 || val == 11){
						println("Bad Luck!");
						usePlayerBoost();
					}
					player.incrementBoostCharges();
					break;
				}
			}
			if (player.getBoost().duration() != null){
				boostDuration = 60 * player.getBoost().duration();
				isBoostActive = true;
			}
			player.decrementBoostCharges();
		}
	}

	private Integer[] findRandomValidIndexes(){
		Random rand = new Random();
		int randYIndex;
		int randXIndex;
		boolean validMove;

		do {
			validMove = true;
			randYIndex = rand.nextInt(levelHeight);
			randXIndex = rand.nextInt(levelWidth);
			try {
				if (levelObjectArray[randYIndex][randXIndex] instanceof Wall) {
					validMove = false;
				}
			}
			catch (ArrayIndexOutOfBoundsException e) {validMove = false;}
		} while ( validMove == false );
		return new Integer[] {randXIndex, randYIndex};
	}

	private void disableBoost(){
		switch(player.getBoost()){
			case timeSlow:
			case superTimeSlow:{
				for (Enemy enemy : enemyList){
					enemy.resetSpeed();
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

				break;
			}

			case dash:
			case superDash:{player.resetSpeed(); break;}

			case pelletMagnet:
			case superPelletMagnet:{pelletPickupSize = 0; break;}

			case invisibility:
			case superInvisibility: {player.setInvisible(false); break;}

			case shield:
			case superShield:  {deleteShield(); break;}

			case randomTeleport:{ break; }
			case invertControls:{
				invertHeldKeys();
				player.setControlsInverted(false);
				break;
			}
			default: {break;}
		}
	}

	private void slowTime(boolean isSuper) {
		for (Enemy enemy: enemyList){
			enemy.setTempSpeed(1);
		}
		isBoostActive = true;
		boostDuration = (isSuper ? Player.Boost.superTimeSlow : Player.Boost.timeSlow).duration();
	}

	private void playerCaught() throws InterruptedException {
		println("CAUGHT!");
		println("You have " + extraLives + " extra lives remaining");
		currentGameTick = 0;
		TimeUnit.SECONDS.sleep(1);
		extraLives--;

		restartLevel();
	}

	/**
	 * Manages the timer, and time bar, timeouts, etc.
	 * Returns true if the game should continue, false if it should be paused (i.e ready screen)
	 * */
	private boolean manageTime() throws TimeOutException {
		timeBar.setProgress(currentGameTick/(maxTime + 240));
		if(currentGameTick >= (maxTime + 240)) {
			print("Time's Up!");
			throw new TimeOutException();
		}
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
			return false;
		}
		return true;
	}

	private void manageEatGhosts() {
		if (playerPowerUpTimer == 0 && player.isAbilityActive()) {
			resetPlayerPowerUpState();
			player.setAbilityActive(false);
		}
		else {
			if ((playerPowerUpTimer < (2*60)) && (playerPowerUpTimer % 20 == 0)) {
				for (Enemy enemy :enemyList) {
					enemy.setColor(Color.WHITE);
				}
				for(int i = 1; i < playerList.size(); i++) {
					playerList.get(i).getModel().setFill(Color.WHITE);
				}
			}
			else if ((playerPowerUpTimer < (2*60)) && ((playerPowerUpTimer+10) % 20 == 0)) {
				for (Enemy enemy :enemyList) {
					enemy.setColor(Color.DODGERBLUE);
				}
				for(int i = 1; i < playerList.size(); i++) {
					playerList.get(i).getModel().setFill(Color.DODGERBLUE);
				}
			}
			playerPowerUpTimer--;
		}
	}

	private void manageSnake() throws PlayerCaughtException {
		for (int i = 0; i < snakePieces.size(); i++) {
			SnakePiece snakePiece = snakePieces.get(i);

			if (i == 0) {
				snakePiece.enqueueMove(player.getPrevDirection());
			}
			else {
				snakePiece.enqueueMove(snakePieces.get(i-1).getPrevDirection());
			}

			Direction move = snakePiece.dequeueMove();
			if (move != null) {
				switch(move) {
					case up:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[1] == 0) {
								//Wrap around
								snakePiece.moveTo(snakePiece.getPosition()[0], convertToPosition(levelObjectArray.length-1, false));
							}
						}
						snakePiece.moveBy(0, (int)-snakePiece.getSpeed());
						break;
					}
					case down:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[1] == levelObjectArray.length - 1) {
								//Wrap around
								snakePiece.moveTo(snakePiece.getPosition()[0], convertToPosition(0, false) );
							}
						}
						snakePiece.moveBy(0, (int)snakePiece.getSpeed());
						break;
					}
					case left:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[0] == 0) {
								//Wrap around
								snakePiece.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), snakePiece.getPosition()[1]);
							}
						}
						snakePiece.moveBy((int)-snakePiece.getSpeed(), 0);
						break;
					}
					case right:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[0] == levelObjectArray[0].length - 1) {
								//Wrap around
								snakePiece.moveTo(convertToPosition(0, true), snakePiece.getPosition()[1]);
							}
						}
						snakePiece.moveBy((int)snakePiece.getSpeed(), 0);
						break;
					}
				}
			}
			snakePiece.setPrevDirection(move);
			snakePiece.setPrevIndex(convertToIndex(snakePiece.getPosition()[0], true), convertToIndex(snakePiece.getPosition()[1], false));
			if (snakePieces.size() > 3) {
				if (Math.abs(snakePiece.getPosition()[0] - player.getPosition()[0]) < 10 && Math.abs(snakePiece.getPosition()[1] - player.getPosition()[1]) < 10) {
					throw new PlayerCaughtException();
				}
			}
		}
	}

	private boolean isGridAligned(Character character) {
		if ( ((character.getPosition()[0] - levelOffsetX) % gridSquareSize == 0) && ((character.getPosition()[1] - levelOffsetY) % gridSquareSize == 0) ) {
			return true;
		}
		else {
			return false;
		}
	}

	private void usePlayerAbility(boolean fromPickup) {
		if (fromPickup == false) {
			switch (player.getAbility()) {
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
				default: {break;}
			}
		}
		else {
			switch (player.getAbility()) {
				case eatGhosts: {
					player.setAbilityActive(true);
					playerPowerUpTimer = playerPowerUpDuration;
					for (Enemy enemy : enemyList) {
						enemy.setColor(Color.DODGERBLUE);
						enemy.setSpeed(1);
					}

					for (int i = 1; i < playerList.size(); i++) {
						playerList.get(i).getModel().setFill(Color.DODGERBLUE);
						playerList.get(i).setSpeed(1);
					}
				}
				case gun:
				case wallJump: {player.incrementAbilityCharges(); break;}

				default: {break;}
			}
		}


	}

	private void wallJump() {
		if (player.getAbilityCharges() <= 0) {
			// play error sound
			return;
		}
		if ( isGridAligned(player) ) {
			try {
				int xIndex = convertToIndex(player.getPosition()[0], true);
				int yIndex = convertToIndex(player.getPosition()[1], false);
				int[] delta = {0,0};

				switch(player.getHeldButtons().getTop()) {
					case up:{delta[1] = -2; break;}
					case down:{delta[1] = 2; break;}
					case left:{delta[0] = -2; break;}
					case right:{delta[0] = 2; break;}
				}

				if(levelObjectArray[yIndex + delta[1]][xIndex + delta[0]] instanceof Wall) {
					return;
				}
				else {
					playerIsWallJumping = true;
					player.setPrevDirection(player.getHeldButtons().getTop());
					player.decrementAbilityCharges();
				}

			}
			catch (ArrayIndexOutOfBoundsException e)  {

			}
		}


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

	private void initPostLevel(Stage primaryStage) {

		castleSelect.setDisable(true);
		targetSelect.setDisable(true);


		castleSelect.setOnAction( e -> {loadNewLevel(primaryStage, levelCastle); gameLoop.start();} );
		targetSelect.setOnAction( e -> {loadNewLevel(primaryStage, levelTarget); gameLoop.start();} );

		randomBoostButton.setOnAction(e -> {player.setBoost(Player.Boost.random);} );

		postLevelOverlay.relocate((windowWidth/2)-400, (windowHeight/2)-200);
		postLevelBackground.setArcHeight(100);
		postLevelBackground.setArcWidth(100);
		postLevelBackground.setFill(Color.AQUA);
		postLevelBackground.setOpacity(0.5);

		postLevelTitle.setTranslateX(50);
		postLevelTitle.setTranslateY(50);
		postLevelTitle.setText("Pick a boost!");
		postLevelTitle.setStyle("-fx-font-size: 24; -fx-font-family: System;");

		postLevelElements.setTranslateX(50);
		postLevelElements.setTranslateY(50);

		worldMap.setTranslateX(100);
		worldMap.getColumnConstraints().add(new ColumnConstraints(75));
		worldMap.getRowConstraints().add(new RowConstraints(75));
		worldMap.add(targetSelect, 0, 1);
		worldMap.add(castleSelect, 1, 1);

		givenBoostButton.setText("Pellet Magnet");

		postLevelScreen.getChildren().addAll(postLevelTitles, postLevelElements);
		postLevelTitles.getChildren().addAll(postLevelTitle);
		postLevelElements.getChildren().addAll(givenBoostButton,randomBoostButton,worldMap);


	}

	@Override
	public void start(Stage primaryStage) {
		try {

			initRootLaunchLayout(primaryStage);
			initPostLevel(primaryStage);
			primaryStage.setScene(launchScene);
			primaryStage.show();

			glitchTheGhostModel.setRotate(180);
			glitchTheGhostModel.setFill(Color.RED);

			timeBar.setLayoutY(50);
			timeBar.setLayoutX(588);
			timeBar.setScaleX(10);

			Text currentCharacter = (Text) launchScene.lookup("#currentCharacter");
			StackPane pacmanSelect = (StackPane) launchScene.lookup("#pacmanSelect");
			StackPane msPacmanSelect = (StackPane) launchScene.lookup("#msPacmanSelect");
			StackPane packidSelect = (StackPane) launchScene.lookup("#packidSelect");
			StackPane robotSelect = (StackPane) launchScene.lookup("#robotSelect");
			StackPane snacSelect = (StackPane) launchScene.lookup("#snacSelect");
			StackPane glitchSelect = (StackPane) launchScene.lookup("#glitchSelect");


			pacmanSelect.setStyle("-fx-border-color: black");
			pacmanSelect.getChildren().add(PlayerCharacter.PacMan.model());

			msPacmanSelect.getChildren().add(PlayerCharacter.MsPacMan.model());
			msPacmanSelect.setStyle("-fx-border-color: black");

			packidSelect.getChildren().add(PlayerCharacter.PacKid.model());
			packidSelect.setStyle("-fx-border-color: black");

			robotSelect.getChildren().add(PlayerCharacter.Robot.model());
			robotSelect.setStyle("-fx-border-color: black");

			snacSelect.getChildren().add(PlayerCharacter.SnacTheSnake.model());
			snacSelect.setStyle("-fx-border-color: black");

			glitchSelect.getChildren().add(PlayerCharacter.GlitchTheGhost.model());
			glitchSelect.setStyle("-fx-border-color: black");

			currentCharacter.setText("Pacman");
			playerCharacter = PlayerCharacter.PacMan;

			pacmanSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					currentCharacter.setText("Pacman");
					playerCharacter = PlayerCharacter.PacMan;
				}
			});

			msPacmanSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					currentCharacter.setText("msPacman");
					playerCharacter = PlayerCharacter.MsPacMan;
				}
			});
			packidSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					currentCharacter.setText("Packid");
					playerCharacter = PlayerCharacter.PacKid;
				}
			});
			robotSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					currentCharacter.setText("Robot");
					playerCharacter = PlayerCharacter.Robot;

				}
			});
			snacSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					currentCharacter.setText("Snac The Snake");
					playerCharacter = PlayerCharacter.SnacTheSnake;
				}
			});
			glitchSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					currentCharacter.setText("Glitch");
					playerCharacter = PlayerCharacter.GlitchTheGhost;
				}
			});

		} catch(Exception e) {
			e.printStackTrace();
		}


	}

	private void enemyKilled(Character enemy) {
		player.modifyScore(ateGhostScore);
		enemy.moveTo(convertToPosition(enemy.getStartIndex()[0],true), convertToPosition(enemy.getStartIndex()[1],false));
	}

	private int[] calculateEnemyMovement(Enemy enemy) throws PlayerCaughtException {
		int[] delta = {0,0};

		// Is this enemy colliding with the player?
		if ((Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) < gridSquareSize/2) && (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) < gridSquareSize/2)) {
			if ((player.isAbilityActive() && player.getAbility() == Player.Ability.eatGhosts) || (player.getAbility() == Player.Ability.eatSameColor && enemy.model.getFill() == player.model.getFill()) ) {
				enemyKilled(enemy);
			}
			else if (player.getShield() != null){
				enemyKilled(enemy);
				deleteShield();
			}
			else { throw new PlayerCaughtException(); }
		}
		if (player.getAbility() == Player.Ability.snake) {
			for (SnakePiece snakePiece : snakePieces) {
				if ((Math.abs(enemy.getPosition()[0] - snakePiece.getPosition()[0]) < gridSquareSize/2) && (Math.abs(enemy.getPosition()[1] - snakePiece.getPosition()[1]) < gridSquareSize/2)) {
					throw new PlayerCaughtException();
				}
			}
		}

		// If enemy is aligned with grid, update the grid position
		if ( isGridAligned(enemy) ) {
			int xIndex = convertToIndex(enemy.getPosition()[0], true); //(int)(enemy.getPosition()[0] - levelOffsetX) / gridSquareSize;
			int yIndex = convertToIndex(enemy.getPosition()[1], false);//(int)(enemy.getPosition()[1] - levelOffsetY) / gridSquareSize;

			int playerXIndex = convertToIndex(player.getPosition()[0], true);// (int)((player.getPosition()[0] - levelOffsetX) / gridSquareSize);
			int playerYIndex = convertToIndex(player.getPosition()[1], false);// (int)((player.getPosition()[1] - levelOffsetY) / gridSquareSize);

			//Enemies aren't stored in levelObjectArray because they would overwrite pellets as they move, plus they don't need to be.

			//The beginning of more AI decisions goes here
			if (player.getAbility() == Player.Ability.eatGhosts && player.isAbilityActive() ) {
				//Take the direction that maximises euclidean distance to the player
				enemy.setNextMove(adjMatrix.findEuclideanDirection(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}, false));
			}
			else if (player.getInvisible()){
				if (enemy.getPathLength() == 0){
					Random rand = new Random();
					int searchRadius = 5;
					println("I can't see anything!");
					int randYIndex = rand.nextInt(searchRadius) - searchRadius / 2;
					int randXIndex = rand.nextInt(searchRadius) - searchRadius / 2;
					boolean validMove = false;

					while ( validMove == false ){
						validMove = true;
						randYIndex = rand.nextInt(searchRadius) - searchRadius / 2;
						randXIndex = rand.nextInt(searchRadius) - searchRadius / 2;
						try {
							if (levelObjectArray[yIndex + randYIndex][xIndex + randXIndex] instanceof Wall) {
								validMove = false;
							}
						}
						catch (ArrayIndexOutOfBoundsException e) {validMove = false;}
					}

					println("I guess I'll move to " + (xIndex + randXIndex) + ", " + (yIndex + randYIndex));

					chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {yIndex + randYIndex, xIndex + randXIndex});
					println("My path is now " + enemy.getPathLength() + " long.");
				}
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
		try {
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
					println("euc");
					break;
				}
				default:{throw new IllegalArgumentException("Invalid algorithm");}
			}
		}
		catch(NullPointerException e) {
			enemy.setNextMove(enemy.getPrevDirection());
			e.printStackTrace();
		}
	}

	private int[] calculatePlayerMovement(Player player) throws LevelCompleteException, PlayerCaughtException{
		int[] delta = {0,0};

		// Is this playerGhost colliding with the player?
		if(player != playerList.get(0)) {
			if ((Math.abs(player.getPosition()[0] - playerList.get(0).getPosition()[0]) < gridSquareSize/2) && (Math.abs(player.getPosition()[1] - playerList.get(0).getPosition()[1]) < gridSquareSize/2)) {
				if ((playerList.get(0).isAbilityActive() && playerList.get(0).getAbility() == Player.Ability.eatGhosts) || (playerList.get(0).getAbility() == Player.Ability.eatSameColor && player.model.getFill() == player.model.getFill()) ) {
					enemyKilled(player);
				}
				else if (player.getShield() != null){
					enemyKilled(player);
					deleteShield();
				}
				else { throw new PlayerCaughtException(); }
			}
		}

		// If player has aligned with the grid
		if ( isGridAligned(player) ) {
			int xIndex = convertToIndex(player.getPosition()[0], true);
			int yIndex = convertToIndex(player.getPosition()[1], false);

			if (playerIsWallJumping) {
				if (levelObjectArray[yIndex][xIndex] instanceof Wall) {
					playerIsWallJumping = false;
				}

				switch (player.getPrevDirection()) {
					case up: {delta[1] = -(int)player.getSpeed(); break;}
					case down: {delta[1] = (int)player.getSpeed(); break;}
					case left: {delta[0] = -(int)player.getSpeed(); break;}
					case right: {delta[0] = (int)player.getSpeed(); break;}
					default: {break;}
				}

			}

			//This bit is for the snake player, who needs to stop all his other pieces to stop moving if he stops
			if (player.getAbility() == Player.Ability.snake) {
				if (player.getHeldButtons().isEmpty()) {
					player.setPrevDirection(null);
				}
				else {
					boolean validMoveExists = false;
					for (int i = 0; i < player.getHeldButtons().size(); i++) {
						boolean valid = true;
						switch(player.getHeldButtons().getNFromTop(i)) {
							case up:{
								try {
									if (levelObjectArray[yIndex-1][xIndex] instanceof Wall) {
										valid = false;
										break;
									}
								}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
							}
							case down:{
								try {
									if (levelObjectArray[yIndex+1][xIndex] instanceof Wall) {
										valid = false;
										break;
									}
								}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
							}
							case left:{
								try {
									if (levelObjectArray[yIndex][xIndex-1] instanceof Wall) {
										valid = false;
										break;
									}
								}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
							}
							case right:{
								try {
									if (levelObjectArray[yIndex][xIndex+1] instanceof Wall) {
										valid = false;
										break;
									}
								}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
							}
							if (valid == true) {
								validMoveExists = true;
							}
						}
					}
					if (!validMoveExists) {
						player.setPrevDirection(null);
					}
				}
			}

			if(levelObjectArray[yIndex][xIndex] instanceof PickUp && !player.getIsGhost()) {
				player.modifyScore(((PickUp)(levelObjectArray[yIndex][xIndex])).getScoreValue());
				currentLevel.getChildren().remove((levelObjectArray[yIndex][xIndex].getModel()));
				if (player == null) {
					print("Player is null");
				}
				if (currentScoreText == null) {
					print("currentScoreText is null");
				}
				currentScoreText.setText(player.getScoreString());

				pelletsRemaining--;

				if (player.getAbility() == Player.Ability.snake) {
					if (player.incrementPelletCounter()) {
						//println("Spawning new snake bit");
						SnakePiece newPiece;
						Random rand = new Random();
						double rand1 = 0;//(rand.nextInt(2*gridSquareSize)-gridSquareSize)/16.0;
						double rand2 = 0;//(rand.nextInt(2*gridSquareSize)-gridSquareSize)/16.0;
						if (snakePieces.isEmpty()) {
							newPiece = new SnakePiece(new Rectangle(gridSquareSize, gridSquareSize,Color.SEAGREEN), (int)player.getSpeed(), player);
							newPiece.moveTo(player.getPosition()[0] + rand1 , player.getPosition()[1] + rand2);
						}
						else {
							SnakePiece lastPiece = snakePieces.get(snakePieces.size() - 1);
							newPiece = new SnakePiece(new Rectangle(gridSquareSize, gridSquareSize,Color.SEAGREEN), (int)player.getSpeed(), lastPiece);
							newPiece.moveTo(lastPiece.getPosition()[0] + rand1, lastPiece.getPosition()[1] + rand2);
						}
						snakePieces.add(newPiece);
						currentLevel.getChildren().add(newPiece.getModel());
					}
				}
				//Is this pickup a power pellet?
				if (((PickUp)(levelObjectArray[yIndex][xIndex])).getPickUpType() == PickUp.PickUpType.powerPellet) {
					//What ability does the player have?
					usePlayerAbility(true);
				}
			}

			//Set and clear the player's position in the object array, unless the player is doing something weird like using the wall jump ability
			if (!player.getIsGhost() &&  !(levelObjectArray[yIndex][xIndex] instanceof Wall) ){
				//println("setting " + xIndex + ", " + yIndex + " to be player");
				levelObjectArray[yIndex][xIndex] = player; // set new player position in array
			}

			if (!player.getIsGhost() && levelObjectArray[player.getPrevIndex()[1]][player.getPrevIndex()[0]] instanceof Player) {
				//println("clearing " + xIndex + ", " + yIndex);
				levelObjectArray[player.getPrevIndex()[1]][player.getPrevIndex()[0]] = null; //clear old player position in collision detection array
			}

			player.setPrevIndex(xIndex, yIndex);


			if (isBoostActive && ( player.getBoost() == Player.Boost.superPelletMagnet ||  player.getBoost() == Player.Boost.pelletMagnet)){
				 for (int i = -pelletPickupSize; i <= pelletPickupSize; i++){
					 for (int j = -pelletPickupSize ; j <= pelletPickupSize; j++){
						 try{
							 if (AdjacencyMatrix.calcDistance(new Integer[] {xIndex, yIndex}, new Integer[] {xIndex + i, yIndex + j}) <= pelletPickupSize) {
								 if (levelObjectArray[yIndex + j][xIndex + i] instanceof PickUp){
									 if (((PickUp)levelObjectArray[yIndex + j][xIndex + i]).getPickUpType() == PickUp.PickUpType.powerPellet){
										 usePlayerAbility(true);
									 }
									 player.modifyScore(((PickUp)(levelObjectArray[yIndex + j][xIndex + i])).getScoreValue());
									 currentScoreText.setText(player.getScoreString());
									 currentLevel.getChildren().remove((levelObjectArray[yIndex + j][xIndex + i].getModel()));
									 levelObjectArray[yIndex + j][xIndex + i] = null;
									 pelletsRemaining--;
								 }
							 }
						 }
						 catch(ArrayIndexOutOfBoundsException e) {}
					 }
				 }
			}

			//Loop through the held movement keys in order of preference
			for (int n = 0; n< Integer.min(player.getHeldButtons().size(), 2) ; n++) {

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
