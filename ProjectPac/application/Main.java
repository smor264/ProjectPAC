package application;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Group;
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
	public final static int gridSquareSize = 36; // ONLY WORKS FOR EVEN NUMBERS (multiples of four?)
	public final static int levelOffsetX = 154+36;
	public final static int levelOffsetY = 100;


	private LevelTree levelTree = new LevelTree();
	private Level loadedLevel;

	//Managed Variables and Objects
	private int retries = 2;
	private LevelObject[][] levelObjectArray = new LevelObject[levelHeight][levelWidth]; //Array storing all objects in the level (walls, pellets, enemies, player)
	private Player player; //= new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());
	private ArrayList<Enemy> enemyList = new ArrayList<Enemy>(); // Stores all enemies so we can loop through them for AI pathing
	private AdjacencyMatrix adjMatrix; // Pathfinding array
	private ArrayList<Player> playerList = new ArrayList<Player>();

	private int pelletsRemaining = 0;
	private boolean pausePressed = false;


	private int playerPowerUpDuration = 10 * 60; // Powerup duration time in ticks
	private int playerPowerUpTimer = 0;// This counts down from playerPowerUpDuration to zero, at which point the powerup expires
	boolean isBoostActive = false;
	int boostDuration;

	private int ateGhostScore = 200; //Score given for eating a ghost
	private double currentGameTick = 0;
	private double maxTime = 120 * 60;

	private boolean playerIsWallJumping = false; // This should go in player eventually

	boolean waitingForGridAlignment = false; // used for dash and super dash boosts
	public int changeColor = 0;

	SoundController sound = new SoundController();

	Laser laserFactory = new Laser();

	ArrayList<SnakePiece> snakePieces = new ArrayList<SnakePiece>(); // stores snake pieces if the player is snake
	Random rand = new Random();
	private Scanner scanFile;

	private AnimationTimer gameLoop;
	private Boolean isPostScreenShowing = false;

	//Scenes and Panes
	private AnchorPane gameUI = new AnchorPane();
	private AnchorPane launchScreen = new AnchorPane();
	private Scene launchScene = new Scene(launchScreen, windowWidth, windowHeight);
	private Group currentLevel = new Group();
	private Scene gameScene = new Scene(gameUI, windowWidth, windowHeight, Color.GREY); //Scene is where all visible objects are stored to be displayed on the stage (i.e window)

	//Levels
	private String loadedLevelName;


	//Overlays
	private Rectangle pauseScreen = new Rectangle(0,0, (double) windowWidth,(double) windowHeight); //Pause Overlay
	private Text pauseText = new Text("Paused");
	private StackPane pauseOverlay = new StackPane(pauseScreen, pauseText);
	private Rectangle startScreen = new Rectangle(0,0, (double) windowWidth, (double) windowHeight);
	private Text startText = new Text();
	private StackPane startOverlay = new StackPane(startScreen, startText);

	private ProgressBar timeBar = new ProgressBar();

    //Post level elements
    public Text postLevelTitle = new Text();

    public LevelButton level1Select = new LevelButton("Home", LevelTree.level1);
    public LevelButton medieval1Select = new LevelButton("Village", LevelTree.medieval1);
    public LevelButton future1Select = new LevelButton("Target", LevelTree.future1);
    public LevelButton medieval2Select = new LevelButton("Castle", LevelTree.medieval2);
    public LevelButton future2Select = new LevelButton("City", LevelTree.future2);
    public LevelButton ice1Select = new LevelButton("Glacier", LevelTree.ice1);
    public LevelButton rock1Select = new LevelButton("Canyon", LevelTree.rock1);
    public LevelButton garden1Select = new LevelButton("Garden", LevelTree.garden1);
    public LevelButton ice2Select = new LevelButton("Iceberg", LevelTree.ice2);
    public LevelButton rock2Select = new LevelButton("Temple", LevelTree.rock2);
    public LevelButton garden2Select = new LevelButton("Maze", LevelTree.garden2);

    public Button givenBoostButton = new Button();
    public Button randomBoostButton = new Button("Random Boost");
	private LevelButton[] levelSelectButtons = {level1Select,
												future1Select, future2Select,
												medieval1Select, medieval2Select,
												rock1Select, rock2Select,
												ice1Select, ice2Select,
												garden1Select, garden2Select};

	private HBox postLevelTitles = new HBox(25);
	private HBox postLevelElements = new HBox(25);
	private VBox postLevelScreen = new VBox(25);
	private Rectangle postLevelBackground = new Rectangle(1000, 500);
    private StackPane postLevelOverlay = new StackPane(postLevelBackground, postLevelScreen);
    private GridPane worldMap = new GridPane();
    private Text postLevelStory = new Text();

	//FXML
	//Game FXML
	public FXMLController controller = new FXMLController();
	public Text currentScoreText = (Text) gameScene.lookup("#currentScoreText");
    public AnchorPane HUDBar = (AnchorPane) gameScene.lookup("#HUDBar");
    public Text currentLevelText;

    //Start Screen FXML
    public Button playButton;
    public Button exitButton;
    public Button loadSaveButton;
    public Button twoPlayerButton;
    public Button threePlayerButton;
    public Button helpButton;
    public Text currentAbility;
    public Text currentBoost;
    public Text currentSaveFileName;

    //StartScreen elements
	Text currentCharacter;
	StackPane pacmanSelect;
	StackPane msPacmanSelect;
	StackPane packidSelect;
	StackPane robotSelect;
	StackPane snacSelect;
	StackPane glitchSelect;
	VBox helpFrame = new VBox(20);
	Button exitHelpButton = new Button("Close");
	Rectangle helpBackground = new Rectangle(windowWidth, windowHeight-100, Color.WHITESMOKE);
	Text helpText = new Text();
	StackPane textFrame = new StackPane(helpBackground, helpText);

    //Save/Load
    private File saveFile;
    private String playerName;
    private String charsUnlocked;
    private String levsComplete;
    private ArrayList<PlayerCharacter> charList = new ArrayList<PlayerCharacter>();
    private Charset utf8 = StandardCharsets.UTF_8;
    private String baseSaveData = "Player1\r\n110000\r\n00000000000";

	Story story = new Story("Player 1");


	public PlayerCharacter playerCharacter = PlayerCharacter.SNACTHESNAKE;

	public enum GameMode {
		SINGLEPLAYER,
		TWOPLAYER,
		THREEPLAYER;
	}

	public GameMode currentGameMode;

	private static Shape glitchTheGhostModel = new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0);
	private static Polygon mouth = new Polygon(-18.0,-18.0, 40.0,-40.0, 15.6,-9.0, 0.0,0.0, 15.6,9.0, 40.0,40.0, -18.0,18.0 );
	/**
	 * A list of all characters that the player can use.
	 * Each PlayerCharacter has a model (Shape), colour (Color), an ability (Ability) and a speed (int)
	 * */
	public enum PlayerCharacter {
		PACMAN (Shape.intersect(new Circle(gridSquareSize/2), mouth), Color.YELLOW, Player.Ability.EATGHOSTS, 2),
		MSPACMAN (Shape.intersect(new Circle(gridSquareSize/2), mouth), Color.LIGHTPINK, Player.Ability.EATGHOSTS, 2),
		PACKID (Shape.intersect(new Circle(gridSquareSize/3), mouth), Color.GREENYELLOW, Player.Ability.WALLJUMP, 2),
		GLITCHTHEGHOST (glitchTheGhostModel, null, Player.Ability.EATSAMECOLOR, 2),
		SNACTHESNAKE (Shape.intersect(new Polygon(-gridSquareSize/2,-gridSquareSize/2, -gridSquareSize/2,gridSquareSize/2, gridSquareSize/2,gridSquareSize/2, gridSquareSize/2,-gridSquareSize/2), mouth), Color.SEAGREEN, Player.Ability.SNAKE, 3),
		ROBOT (Shape.intersect(new Polygon(-gridSquareSize/4.0,-gridSquareSize/4.0, -gridSquareSize/4.0,gridSquareSize/4.0, gridSquareSize/4.0,gridSquareSize/4.0, gridSquareSize/4.0,-gridSquareSize/4.0), mouth), Color.DARKGREY , Player.Ability.LASER, 2);

		private final Shape model;
		private final Player.Ability ability;
		private final int speed;
		private boolean isUnlocked;
		private final Color originalColor;

		PlayerCharacter(Shape model, Color originalColor, Player.Ability ability, int speed){
			this.model = model;
			this.ability = ability;
			this.speed = speed;
			this.isUnlocked = true;
			this.originalColor = originalColor;
		}
		public Shape model() {return model;}
		public Player.Ability ability() {return ability;}
		public int speed() {return speed;}
		public void setUnlockedState(Boolean state) {isUnlocked = state;}
		public void resetColor() { this.model().setFill(originalColor);}
		public Color originalColor() {return originalColor;}
	}

	/**
	 * A list of all valid directions characters can move
	 * */
	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
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

	/** Converts a position value (pixel position on screen) to an index (tile position in level)*/
	private int convertToIndex(double position, boolean isXCoord) {
		return (int)(position-(isXCoord ? levelOffsetX:levelOffsetY)) / gridSquareSize;
	}

	/** Converts an index value (tile posititon in level) to a position (pixel position on screen) */
	private double convertToPosition(int index, boolean isXCoord) {
		return (index * gridSquareSize) + (isXCoord ? levelOffsetX : levelOffsetY);
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

	/**
	 * Places LevelObjects (walls, pickups, enemies, player) in the level*/
	private void placeLevelObject(LevelObject obj, int x, int y) {
		currentLevel.getChildren().add(obj.getModel());
		obj.moveTo(convertToPosition(x, true), convertToPosition(y, false));
		levelObjectArray[y][x] = obj;
	}

	/**Resets LevelObjects to their initial values*/
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

	private void configureFileChooser(FileChooser fileChooser) {
		fileChooser.setTitle("Choose your save file");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
	}

	private void readFromSaveFile(File saveFile) {
		try {
			scanFile = new Scanner(saveFile);

			while(scanFile.hasNext()){
				playerName = scanFile.next();
				charsUnlocked = scanFile.next();
				levsComplete = scanFile.next();
			}

			//Creates a save if one doesn't exist
			if(playerName == null) {
				playerName = "player1";
			}
			if(charsUnlocked == null) {
				charsUnlocked = "110000";
			}
			if(levsComplete == null) {
				levsComplete = "00000000000";
			}


			scanFile.close();
			currentSaveFileName.setText(saveFile.getName());


			for(int i = 0; i < charsUnlocked.length(); i++) {
				if(charsUnlocked.charAt(i) == '0') {
					charList.get(i).setUnlockedState(false);
				}
				else if(charsUnlocked.charAt(i) == '1') {
					charList.get(i).resetColor();
					charList.get(i).setUnlockedState(true);
				}
				else {
					println("Corrupted/Incompatible save file");
				}
			}

			for(int i = 0; i < LevelTree.levelList.size(); i++) {
				if(levsComplete.charAt(i) == '1') {
					levelTree.addCompletedLevel(LevelTree.levelList.get(i));
				}
			}


		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeSave(File saveFile) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(saveFile);
		BufferedWriter buffWriter = new BufferedWriter(writer);

		String newLvl;

		if(levsComplete == null) {
			println("LevsUnlocked is null!");
		}

		newLvl = levsComplete.substring(0, LevelTree.levelList.indexOf(loadedLevel)) + "1" + levsComplete.substring(LevelTree.levelList.indexOf(loadedLevel)+1);
		levsComplete = newLvl;


		if(levelTree.isCompleted(LevelTree.level1)) {
			String pacKidUnlock;
			pacKidUnlock = charsUnlocked.substring(0, 2) + "1" + charsUnlocked.substring(3);
			charsUnlocked = pacKidUnlock;
		}
		if(levelTree.isCompleted(LevelTree.future2)) {
			String robotUnlock;
			robotUnlock = charsUnlocked.substring(0, 3) + "1" + charsUnlocked.substring(4);
			charsUnlocked = robotUnlock;
		}
		if (levelTree.isCompleted(LevelTree.garden2)) {
			String snacUnlock;
			snacUnlock = charsUnlocked.substring(0,4) + "1" + charsUnlocked.substring(5);
			charsUnlocked = snacUnlock;
		}
		if (levelTree.isCompleted(LevelTree.garden2) && levelTree.isCompleted(LevelTree.ice2) && levelTree.isCompleted(LevelTree.rock2)) {
			String glitchUnlock;
			glitchUnlock = charsUnlocked.substring(0,5) + "1";
			charsUnlocked = glitchUnlock;
		}

		try {
			buffWriter.write(playerName);
			buffWriter.newLine();
			buffWriter.write(charsUnlocked);
			buffWriter.newLine();
			buffWriter.write(levsComplete);
			buffWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the next level, clears previous level
	 * @param primaryStage
	 * @param newLevel
	 * @return true
	 */
	private boolean loadNewLevel(Stage primaryStage, Level newLevel) {
		hidePostLevelScreen();
		disableBoost();
		resetPlayerPowerUpState();
		levelObjectArray = new LevelObject[levelHeight][levelWidth];
		currentLevel.getChildren().clear();
		initialiseLevel(newLevel);
		currentLevelText.setText(loadedLevelName);
		currentLevelText.setStyle("-fx-font: 20px System");
		currentGameTick = 0;
		currentLevel.getChildren().add(timeBar);
		currentBoost.setText(player.getBoost().text());
		return true;
	}

	/**
	 * Stops the gameLoop and hands game over UI
	 */
	private void gameOver() {

		player.setScore((int)(player.getScore()/2.0));
		gameLoop.stop();

		if (retries >= 0){
			retries--;
			player.resetLives();
			println("You have " + (retries + 1) + " retries remaining.");
		}
		else{
			levelTree.clearCompletedLevels();
			println("Bad Luck! You have no more retries! Better luck next time!");
		}
		try {
			writeSave(saveFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		showPostLevelScreen(false);
	}

	private void closeGame(Stage primaryStage) {
		if(!isPostScreenShowing) {
			gameLoop.stop();
		}

		Button closeButton = new Button("Close game");
		Button cancelButton = new Button("Return to game");
		Rectangle exitBackground = new Rectangle(400,200,Color.WHITESMOKE);
		HBox exitButtons = new HBox(75);
		VBox exitTitle = new VBox(50);
		Text exitDialog = new Text();

		exitDialog.setText("Are you sure you want to quit?");
		exitDialog.setStyle("-fx-font: 20px Arial");
		exitDialog.setTranslateX(50);
		exitDialog.setTranslateY(50);

		exitTitle.getChildren().addAll(exitDialog,exitButtons);

		StackPane exitBox = new StackPane(exitBackground, exitTitle);

		exitBox.relocate(primaryStage.getWidth()/2-200,primaryStage.getHeight()/2-50);
		exitBackground.setArcHeight(20);
		exitBackground.setArcWidth(20);
		exitButtons.setTranslateY(20);
		exitButtons.setTranslateX(70);

		exitButtons.getChildren().addAll(cancelButton, closeButton);


		currentLevel.getChildren().add(exitBox);


		closeButton.setOnAction(e -> primaryStage.close());
		cancelButton.setOnAction(e -> {
			currentLevel.getChildren().remove(exitBox);
			if(!isPostScreenShowing) {
				gameLoop.start();
			}
		});

	}

	private boolean showOverlay(StackPane overlay) {
		return currentLevel.getChildren().add(overlay);
	}

	private boolean hideOverlay(StackPane overlay) {
		return currentLevel.getChildren().removeAll(overlay);
	}

	private boolean showPostLevelScreen(boolean firstTime) {
		int randBoostIndex = rand.nextInt(5) * 2;
		givenBoostButton.setText(Player.Boost.values()[randBoostIndex].text());
		givenBoostButton.setOnAction(e -> {player.setBoost(Player.Boost.values()[randBoostIndex]);} );
		checkUnlockedLevels();
		isPostScreenShowing = true;
		if (firstTime){
			postLevelStory.setText(story.getInitialStory());
		}
		else{
			postLevelStory.setText(story.getStoryFor(loadedLevel));
		}
		return currentLevel.getChildren().add(postLevelOverlay);
	}

	private boolean hidePostLevelScreen() {
		isPostScreenShowing = false;
		return currentLevel.getChildren().removeAll(postLevelOverlay);
	}

	private void initialiseLevel(Level level) {
		int[][] array = level.getArray();
		enemyList.clear();
		playerList.clear();
		boolean playerExists = false;
		Rectangle background = new Rectangle(windowWidth, windowHeight);
		background.setFill(level.getBackground());
		currentLevel.getChildren().add(background);
		background.toBack();
		background.setTranslateY(60);
		pelletsRemaining = 0;
		loadedLevelName = level.getLevelName();
		loadedLevel = level;

		for (int xPos = 0; xPos < array[0].length; xPos++) {
			for (int yPos = 0; yPos < array.length; yPos++) {
				if (array[yPos][xPos] == 1) { // Wall
					Object[] wallType;
					wallType = determineWallType(array,xPos,yPos);
					SolidWall wall = new SolidWall( (Wall.WallType)wallType[0], (Direction)wallType[1], level.getWallColor());

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
				else if (array[yPos][xPos] == 3) { // ghost gate
					Object[] wallType;
					wallType = determineWallType(array,xPos,yPos);
					GhostGate gate = new GhostGate( (Wall.WallType)wallType[0], (Direction)wallType[1]);

					placeLevelObject(gate, xPos, yPos);
				}
				else if (array[yPos][xPos] < 0) { //Enemy
					Object[] characteristics = determineEnemyCharacteristics(-array[yPos][xPos]);
					double g = gridSquareSize;
					Shape top = new Circle(g/2.0);
					top.setTranslateY(-g/8.0);
					
					Shape bottom = new Polygon(-g/2,-g/4, -g/2,5*g/8.0, -g/3,g/2, -g/6,5*g/8, 0,g/2, g/6,5*g/8, g/3,g/2, g/2,5*g/8, g/2,-g/4);
					Shape model = Shape.union(top, bottom);
					model.setFill(enemyColors[enemyList.size()]);
					model.setScaleY(0.9);
					model.setScaleX(0.9);
					Enemy enemy = new Enemy(model, 2, (Enemy.Intelligence)characteristics[0], (Enemy.Behaviour)characteristics[1], (Enemy.Algorithm)characteristics[2]);
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
		}
		resetPlayerPowerUpState();

		/*Check for solid areas*/
		for (int xPos = 0; xPos < array[0].length-1; xPos++) {
			for (int yPos = 0; yPos < array.length-1; yPos++) {
				if (array[yPos][xPos] == 1 && array[yPos][xPos+1] == 1 && array[yPos+1][xPos] == 1 && array[yPos+1][xPos+1] == 1) {
					// If you find a solid area, just plonk a wall in the centre so it looks solid
					double xAvg = (convertToPosition(xPos+1, true) +convertToPosition(xPos+2, true))/2.0 - Main.gridSquareSize/4;
					double yAvg = (convertToPosition(yPos+1, false) +convertToPosition(yPos+2, false))/2.0 - Main.gridSquareSize/4;
					SolidWall wall = new SolidWall(Wall.WallType.SINGLE, Direction.UP, level.getWallColor());
					wall.moveTo(xAvg, yAvg);
					currentLevel.getChildren().add(wall.getModel());
				}
			}
		}
		player.getModel().toFront(); // Draw player and enemies over top of pellets, etc.
		for (Enemy enemy: enemyList) {
			enemy.getModel().toFront();
		}
		adjMatrix = new AdjacencyMatrix(levelObjectArray);


		//For multiplayer, replaces AI with playable ghosts
		if(currentGameMode != GameMode.SINGLEPLAYER) {
			for(int i = 0; i < currentGameMode.ordinal(); i++) {
				double g = gridSquareSize;
				Shape top = new Circle(g/2.4);
				top.setTranslateY(-3);
				top.setScaleX(1.2);
				
				Shape bottom = new Polygon(-g/2,0, -g/2,g/2.0, -g/3,g/2-g/8, -g/6,g/2, 0,g/2-g/8, g/6,g/2, g/3,g/2-g/8, g/2,g/2, g/2,0);
				Shape model = Shape.union(top, bottom);
				model.setFill(enemyColors[enemyList.size()]);
				
				Player playerGhost = new Player(model, playerCharacter.speed(), true,enemyColors[i]);
				println(playerGhost.getContainer().getBoundsInLocal().getWidth() +"");
				playerList.add(playerGhost);
				playerGhost.moveTo(convertToPosition(enemyList.get(0).getStartIndex()[0]+1,true), convertToPosition(enemyList.get(0).getStartIndex()[1]+1,false));
				playerGhost.setStartIndex(enemyList.get(0).getStartIndex());
				currentLevel.getChildren().remove(enemyList.get(0).getModel());
				enemyList.remove(0);
				currentLevel.getChildren().add(playerGhost.getModel());
				println(playerGhost.getPosition()[0] +", " + playerGhost.getPosition()[1]);
			}
		}

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

	public void initCharList() {
		if(charList.isEmpty()) {
		charList.add(PlayerCharacter.PACMAN);
		charList.add(PlayerCharacter.MSPACMAN);
		charList.add(PlayerCharacter.PACKID);
		charList.add(PlayerCharacter.ROBOT);
		charList.add(PlayerCharacter.SNACTHESNAKE);
		charList.add(PlayerCharacter.GLITCHTHEGHOST);
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
		twoPlayerButton = (Button) launchScene.lookup("#twoPlayerButton");
		threePlayerButton = (Button) launchScene.lookup("#threePlayerButton");
		helpButton = (Button) launchScene.lookup("#helpButton");

		helpButton.setOnAction(e -> {
			helpFrame.getChildren().addAll(textFrame,exitHelpButton);
			launchScreen.getChildren().add(helpFrame);

			helpFrame.prefWidthProperty().bind(textFrame.widthProperty());
			exitHelpButton.setTranslateX(550);
			helpText.setStyle("-fx-font: 16px System");

			helpText.setText("Controls:\n"
					+ "Single Player: W,A,S,D or Arrow keys for movement\n"
					+ "C for boost\n"
					+ "V for ability where applicable\nMulti-Player:\n"
					+ "Arrows for player\n"
					+ "W,A,S,D and I,J,K,L for ghosts\n"
					+ "\n"
					+ "P: Pauses the game\n"
					+ "ESC: Closes game\n"
					+ "\nCharacters:\nPacman: Loves eating pellets and ghosts OM-NOM-NOM.\n"
					+ "Ability: Can eat ghosts for a short time after eating a power pellet.\n"
					+ "Ms.Pacman: Girls just wanna eat pellets and ghosts.\n"
					+ "Ability: Can eat ghosts for a short time after eating a power pellet.\n"
					+ "PacKid: Full of ENERGY!\n"
					+ "Ability: Can jump over two walls after eating a power pellet.\n"
					+ "Robot: Beep-boop-beep.\nAbility: Can fire a laser that kills(?) ghost in front and behind him\n"
					+ "Snac the Snake: Everyday is a loooong day\n"
					+ "Ability: Grows in size as snac eats pellets\n"
					+ "\nGlitch the ghost: &^$*%^$)*&^*%$$#@@*)*(&*^()\n\nBoosts:\nChoose at start of each level, limited use, if random chosen has a chance of being super.\n"
					+ "Dash\nShield\nPellet Magnet\nInvisibility\nTime Slow\nInvert Controls\nRandom Teleport\n");


		});

		exitHelpButton.setOnAction(e -> launchScreen.getChildren().removeAll(helpFrame));

		twoPlayerButton.setOnAction(e -> {
			currentGameMode = GameMode.TWOPLAYER;
			player = new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());

			println("TwoPlayer Selected!");
			game(primaryStage);
		});

		threePlayerButton.setOnAction(e -> {
			currentGameMode = GameMode.THREEPLAYER;
			player = new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());
			println("ThreePlayer Selected");
			game(primaryStage);
		});

		FileChooser fileChooser = new FileChooser();

		loadSaveButton.setOnAction(e -> {
			configureFileChooser(fileChooser);
			saveFile = fileChooser.showOpenDialog(primaryStage);
			currentSaveFileName.setText(saveFile.getName());
			readFromSaveFile(saveFile);
		});

		exitButton.setOnAction(e -> {primaryStage.close();});
		playButton.setDefaultButton(true);
		playButton.setOnAction(e -> {
			readFromSaveFile(saveFile);
			currentGameMode = GameMode.SINGLEPLAYER;
			player = new Player(playerCharacter.model(), playerCharacter.speed(), playerCharacter.ability());
			game(primaryStage);
			});


		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	private void unlockNewLevels(){
		levelTree.addCompletedLevel(loadedLevel);
	}

	private void checkUnlockedLevels(){
		for (LevelButton button : levelSelectButtons) {
			button.setDisable(true);
			if ( levelTree.isUnlocked(button.getConnectedLevel()) ) {
				println(button.getConnectedLevel().getLevelName() + " is unlocked!");
				button.setDisable(false);
			}
		}
	}

	/**
	 * Loads , displays, and runs the game itself
	 * @param primaryStage
	 */
	private void game(Stage primaryStage) {
		try {
		initialiseLevel(LevelTree.level1);
		initRootGameLayout();
		initialiseOverlays();
		primaryStage.show();
		primaryStage.setScene(gameScene);
		//Binds the variables to their FXML counter parts
		HUDBar = (AnchorPane) gameScene.lookup("#HUDBar");
		currentScoreText = (Text) gameScene.lookup("#currentScoreText");
		currentAbility = (Text) gameScene.lookup("#currentAbility");
		currentBoost = (Text) gameScene.lookup("#currentBoost");
		currentLevelText = (Text) gameScene.lookup("#currentLevelText");

		currentScoreText.setStyle("-fx-font: 20px System");

		showPostLevelScreen(true);


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
				case SINGLEPLAYER: {
					switch (event.getCode()) {
					case W:
					case UP: {
						player.getHeldButtons().append( (inverted ? Direction.DOWN: Direction.UP) ); break;}

					case S:
					case DOWN: { player.getHeldButtons().append( (inverted ? Direction.UP: Direction.DOWN) ); break;}

					case A:
					case LEFT: { player.getHeldButtons().append(inverted ? Direction.RIGHT : Direction.LEFT); break;}

					case D:
					case RIGHT: { player.getHeldButtons().append(inverted ? Direction.LEFT : Direction.RIGHT); break;}

					case C :{ if (currentGameTick >= 240) {usePlayerBoost();} break; }
					case V:{ if (currentGameTick >= 240) {usePlayerAbility(false);} break; }

					case N:{
						unlockNewLevels();
						try {
							writeSave(saveFile);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						showPostLevelScreen(false);
						gameLoop.stop();
						break;
					}
					case PAGE_DOWN:{ currentGameTick = maxTime; break;}

					case ESCAPE:{
						closeGame(primaryStage);
						 break;
						}
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
					case TWOPLAYER: {
						switch (event.getCode()) {
							case W: {playerList.get(1).getHeldButtons().append(Direction.UP);break;}
							case UP: {player.getHeldButtons().append( (inverted ? Direction.DOWN: Direction.UP) ); break;}
	
							case S: {playerList.get(1).getHeldButtons().append(Direction.DOWN);break;}
							case DOWN: { player.getHeldButtons().append( (inverted ? Direction.UP: Direction.DOWN) ); break;}
	
							case A: {playerList.get(1).getHeldButtons().append(Direction.LEFT);break;}
							case LEFT: { player.getHeldButtons().append(inverted ? Direction.RIGHT : Direction.LEFT); break;}
	
							case D: {playerList.get(1).getHeldButtons().append(Direction.RIGHT);break;}
							case RIGHT: { player.getHeldButtons().append(inverted ? Direction.LEFT : Direction.RIGHT); break;}
							case PAGE_DOWN:{ currentGameTick = maxTime; break;}
	
							case ESCAPE:{ closeGame(primaryStage); break;}
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
							case N:{
								unlockNewLevels();
								try {
									writeSave(saveFile);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
								showPostLevelScreen(false);
								gameLoop.stop();
								break;
							}
						}
						break;}
					case THREEPLAYER: {
						switch (event.getCode()) {
							case W: {playerList.get(1).getHeldButtons().append(Direction.UP);break;}
							case UP: {player.getHeldButtons().append( (inverted ? Direction.DOWN: Direction.UP) ); break;}
							case I: {playerList.get(2).getHeldButtons().append(Direction.UP);break;}
	
							case S: {playerList.get(1).getHeldButtons().append(Direction.DOWN);break;}
							case DOWN: { player.getHeldButtons().append( (inverted ? Direction.UP: Direction.DOWN) ); break;}
							case K: {playerList.get(2).getHeldButtons().append(Direction.DOWN);break;}
	
							case A: {playerList.get(1).getHeldButtons().append(Direction.LEFT);break;}
							case LEFT: { player.getHeldButtons().append(inverted ? Direction.RIGHT : Direction.LEFT); break;}
							case J: {playerList.get(2).getHeldButtons().append(Direction.LEFT);break;}
	
							case D: {playerList.get(1).getHeldButtons().append(Direction.RIGHT);break;}
							case RIGHT: { player.getHeldButtons().append(inverted ? Direction.LEFT : Direction.RIGHT); break;}
							case L: {playerList.get(2).getHeldButtons().append(Direction.RIGHT);break;}
							case PAGE_DOWN:{ currentGameTick = maxTime; break;}
	
							case ESCAPE:{ closeGame(primaryStage); break;}
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
					case SINGLEPLAYER:
						switch (event.getCode()) {
						case W:
						case UP: { player.getHeldButtons().remove((inverted ? Direction.DOWN: Direction.UP)); break; }

						case S:
						case DOWN: { player.getHeldButtons().remove((inverted ? Direction.UP: Direction.DOWN)); break; }

						case A:
						case LEFT: { player.getHeldButtons().remove((inverted ? Direction.RIGHT: Direction.LEFT)); break; }

						case D:
						case RIGHT: { player.getHeldButtons().remove((inverted ? Direction.LEFT: Direction.RIGHT)); break; }

						default: break;
						}
						break;
					case TWOPLAYER:
						switch (event.getCode()) {
						case W: { playerList.get(1).getHeldButtons().remove(Direction.UP); break; }
						case UP: { player.getHeldButtons().remove((inverted ? Direction.DOWN: Direction.UP)); break; }

						case S: { playerList.get(1).getHeldButtons().remove(Direction.DOWN); break; }
						case DOWN: { player.getHeldButtons().remove((inverted ? Direction.UP: Direction.DOWN)); break; }

						case A: { playerList.get(1).getHeldButtons().remove(Direction.LEFT); break; }
						case LEFT: { player.getHeldButtons().remove((inverted ? Direction.RIGHT: Direction.LEFT)); break; }

						case D: { playerList.get(1).getHeldButtons().remove(Direction.RIGHT); break; }
						case RIGHT: { player.getHeldButtons().remove((inverted ? Direction.LEFT: Direction.RIGHT)); break; }

						default: break;
						}
						break;

					case THREEPLAYER:
						switch (event.getCode()) {
						case W: { playerList.get(1).getHeldButtons().remove(Direction.UP); break; }
						case UP: { player.getHeldButtons().remove((inverted ? Direction.DOWN: Direction.UP)); break; }
						case I: { playerList.get(2).getHeldButtons().remove(Direction.UP); break; }

						case S: { playerList.get(1).getHeldButtons().remove(Direction.DOWN); break; }
						case DOWN: { player.getHeldButtons().remove((inverted ? Direction.UP: Direction.DOWN)); break; }
						case K: { playerList.get(2).getHeldButtons().remove(Direction.DOWN); break; }

						case A: { playerList.get(1).getHeldButtons().remove(Direction.LEFT); break; }
						case LEFT: { player.getHeldButtons().remove((inverted ? Direction.RIGHT: Direction.LEFT)); break; }
						case J: { playerList.get(2).getHeldButtons().remove(Direction.LEFT); break; }

						case D: { playerList.get(1).getHeldButtons().remove(Direction.RIGHT); break; }
						case RIGHT: { player.getHeldButtons().remove((inverted ? Direction.LEFT: Direction.RIGHT)); break; }
						case L: { playerList.get(2).getHeldButtons().remove(Direction.RIGHT); break; }

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
							sound.levelComplete();
							throw new LevelCompleteException();
						}
						int[] delta = {0,0};

						if (player.getAbility() == Player.Ability.EATSAMECOLOR) {
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
							if (i == 0) {
								manageAnimation(playerList.get(0));
							}
						}

						if (player.getAbility() == Player.Ability.SNAKE) {
							manageSnake();
						}
						else if (player.getAbility() == Player.Ability.EATGHOSTS && player.isAbilityActive()) {
							manageEatGhosts();
						}


						for (int i=0; i< enemyList.size(); i++){
							delta = new int[] {0,0};
							delta = calculateEnemyMovement(enemyList.get(i));
							enemyList.get(i).moveBy(delta[0], delta[1]);
							manageAnimation(enemyList.get(i));
						}

						if (isBoostActive){
							if (waitingForGridAlignment && isGridAligned(player)) {
								if (player.getBoost() == Player.Boost.DASH) {
									player.setTempSpeed(4);
								}
								else if (player.getBoost() == Player.Boost.SUPERDASH) {
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
								if (player.getLives() < 0) {
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
								player.resetLives();
								unlockNewLevels();
								writeSave(saveFile);
								showPostLevelScreen(false);
								return;
							}
							catch(InterruptedException | FileNotFoundException e2){
								Thread.currentThread().interrupt(); // I'm sure this does something, but right now it's just to stop the compiler complaining.
							}
						}
						else {
							exception.printStackTrace();
						}
					}
				}
			};

			//gameLoop.start();

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected void manageAnimation(Enemy enemy) {
				
	}

	private void manageAnimation(Player player){
		if (player.getPrevDirection() == null) {
			return;
		}
		Shape baseModel;
		if (playerCharacter == PlayerCharacter.SNACTHESNAKE){
			baseModel = new Polygon(-gridSquareSize/2,-gridSquareSize/2, -gridSquareSize/2,gridSquareSize/2, gridSquareSize/2,gridSquareSize/2, gridSquareSize/2,-gridSquareSize/2);
		}
		else if (playerCharacter == PlayerCharacter.ROBOT){
			baseModel = new Polygon(-gridSquareSize/4.0,-gridSquareSize/4.0, -gridSquareSize/4.0,gridSquareSize/4.0, gridSquareSize/4.0,gridSquareSize/4.0, gridSquareSize/4.0,-gridSquareSize/4.0);
		}
		else if (playerCharacter == PlayerCharacter.PACMAN || playerCharacter == PlayerCharacter.MSPACMAN ){
			baseModel = new Circle(gridSquareSize/2.0);
		}
		else if (playerCharacter == PlayerCharacter.PACKID){
			baseModel = new Circle(gridSquareSize/3.0);
		}
		else {
			return;
			//baseModel = new Polygon(0.0,-Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, Main.gridSquareSize/2.0, -Main.gridSquareSize/2.0,Main.gridSquareSize/2.0);
			//baseModel.setRotate(180);
		}


		double rotation = player.getModel().getRotate();
		int animationFrame;

		switch (player.getPrevDirection()){
			default:
			case UP:
			case DOWN:{
				animationFrame = (int) (player.getPosition()[1] - levelOffsetY) % (gridSquareSize); break;}
			case LEFT:
			case RIGHT:{
				animationFrame = (int) (player.getPosition()[0] - levelOffsetX) % (gridSquareSize); break;}
		}
		currentLevel.getChildren().remove(player.getModel());
		player.manageAnimation(animationFrame, baseModel);
		currentLevel.getChildren().add(player.getModel());
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
				if (levelObjectArray[randYIndex][randXIndex] instanceof SolidWall) {
					validMove = false;
				}
			}
			catch (ArrayIndexOutOfBoundsException e) {validMove = false;}
		} while ( validMove == false );
		return new Integer[] {randXIndex, randYIndex};
	}

	/**Checks to see if a Character is aligned with the tile grid, or if it is currently moving between tiles*/
	private boolean isGridAligned(Character character) {
		if ( ((character.getPosition()[0] - levelOffsetX) % gridSquareSize == 0) && ((character.getPosition()[1] - levelOffsetY) % gridSquareSize == 0) ) {
			return true;
		}
		else {
			return false;
		}
	}

	private void initPostLevel(Stage primaryStage) {

		levelSelectButtons[0].setOnAction( e -> {
			loadNewLevel(primaryStage, LevelTree.level1);
			gameLoop.start();
		});

		for (int i = 1; i < LevelTree.levelList.size(); i++){
			final int j = i;
			levelSelectButtons[i].setOnAction( e -> {
				loadNewLevel(primaryStage, LevelTree.levelList.get(j));
				gameLoop.start();
			});
		}


		randomBoostButton.setOnAction(e -> {player.setBoost(Player.Boost.RANDOM);} );

		postLevelOverlay.relocate((windowWidth/2)-500, (windowHeight/2)-200);
		postLevelBackground.setArcHeight(100);
		postLevelBackground.setArcWidth(100);
		postLevelBackground.setFill(Color.SILVER);
		postLevelBackground.setOpacity(0.9);

		postLevelTitle.setTranslateX(50);
		postLevelTitle.setTranslateY(50);
		postLevelTitle.setText("Pick a boost!");
		postLevelTitle.setStyle("-fx-font-size: 24; -fx-font-family: System;");

		postLevelElements.setTranslateX(50);
		postLevelElements.setTranslateY(50);

		worldMap.setTranslateX(200);
		worldMap.setTranslateY(-50);
		worldMap.getColumnConstraints().add(new ColumnConstraints(100));
		worldMap.getRowConstraints().add(new RowConstraints(100));
		worldMap.setHgap(10);
		worldMap.setVgap(10);
		worldMap.setAlignment(Pos.CENTER);

		worldMap.add(level1Select, 0, 2);
		worldMap.add(future1Select, 1, 1);
		worldMap.add(medieval1Select, 1, 3);
		worldMap.add(future2Select, 2, 1);
		worldMap.add(medieval2Select, 2, 3);
		worldMap.add(ice1Select, 3, 0);
		worldMap.add(rock1Select, 3, 2);
		worldMap.add(garden1Select, 3, 4);
		worldMap.add(ice2Select, 4, 0);
		worldMap.add(rock2Select, 4, 2);
		worldMap.add(garden2Select, 4, 4);

		worldMap.setValignment(ice1Select, VPos.BOTTOM);
		worldMap.setValignment(ice2Select, VPos.BOTTOM);
		worldMap.setHalignment(future1Select, HPos.LEFT);
		worldMap.setHalignment(medieval1Select, HPos.LEFT);
		worldMap.setHalignment(level1Select, HPos.RIGHT);

		postLevelStory.setText("--");
		postLevelStory.setTranslateY(-100);
		postLevelStory.setTranslateX(25);
		postLevelStory.setStyle("-fx-font: 14 System");
		postLevelScreen.getChildren().addAll(postLevelTitles, postLevelElements, postLevelStory);
		postLevelTitles.getChildren().addAll(postLevelTitle);
		postLevelElements.getChildren().addAll(givenBoostButton,randomBoostButton,worldMap);

		for(int i = 0; i < levelSelectButtons.length; i++) {
			levelSelectButtons[i].setPrefSize(80, 25);

			/*
			levelSelectButtons[i].setStyle("-fx-background-radius: 5em; " +
                "-fx-min-width: 60px; " +
                "-fx-min-height: 60px; " +
                "-fx-max-width: 60px; " +
                "-fx-max-height: 60px;" +
                "-fx-base: #" + LevelTree.levelList.get(i).getBackground().toString());
			*/
				levelSelectButtons[i].setStyle("-fx-background-radius: 5em; " +
		                "-fx-min-width: 60px; " +
		                "-fx-min-height: 60px; " +
		                "-fx-max-width: 60px; " +
		                "-fx-max-height: 60px;" +
		                "-fx-base: #41C7F6");

		}

	}

	@Override
	public void start(Stage primaryStage) {
		try {

			initCharList();
			initRootLaunchLayout(primaryStage);

			File saveCheck = new File("auto-save.txt");

			try {
				if(!saveCheck.exists()) {
					println("No save exists! Creating one now");
					Files.write(Paths.get("auto-save.txt"), baseSaveData.getBytes(utf8));
					saveFile = new File("auto-save.txt");
					readFromSaveFile(saveFile);
				}
				else {
					saveFile = new File("auto-save.txt");
					readFromSaveFile(saveFile);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			initPostLevel(primaryStage);
			primaryStage.setScene(launchScene);
			primaryStage.show();

			glitchTheGhostModel.setRotate(180);
			glitchTheGhostModel.setFill(Color.RED);

			//Unlocks characters based off save file
			for(int i = 0; i < charList.size(); i++) {
				if (!charList.get(i).isUnlocked) {
					charList.get(i).model().setFill(Color.WHITESMOKE);
				}
			}

			timeBar.setLayoutY(50);
			timeBar.setLayoutX(610);
			timeBar.setScaleX(10);

			Text currentCharacter = (Text) launchScene.lookup("#currentCharacter");
			StackPane pacmanSelect = (StackPane) launchScene.lookup("#pacmanSelect");
			StackPane msPacmanSelect = (StackPane) launchScene.lookup("#msPacmanSelect");
			StackPane packidSelect = (StackPane) launchScene.lookup("#packidSelect");
			StackPane robotSelect = (StackPane) launchScene.lookup("#robotSelect");
			StackPane snacSelect = (StackPane) launchScene.lookup("#snacSelect");
			StackPane glitchSelect = (StackPane) launchScene.lookup("#glitchSelect");


			pacmanSelect.setStyle("-fx-border-color: black");
			pacmanSelect.getChildren().add(PlayerCharacter.PACMAN.model());

			msPacmanSelect.getChildren().add(PlayerCharacter.MSPACMAN.model());
			msPacmanSelect.setStyle("-fx-border-color: black");

			packidSelect.getChildren().add(PlayerCharacter.PACKID.model());
			packidSelect.setStyle("-fx-border-color: black");

			robotSelect.getChildren().add(PlayerCharacter.ROBOT.model());
			robotSelect.setStyle("-fx-border-color: black");

			snacSelect.getChildren().add(PlayerCharacter.SNACTHESNAKE.model());
			snacSelect.setStyle("-fx-border-color: black");

			glitchSelect.getChildren().add(PlayerCharacter.GLITCHTHEGHOST.model());
			glitchSelect.setStyle("-fx-border-color: black");

			currentCharacter.setText("Pacman");
			playerCharacter = PlayerCharacter.PACMAN;

			pacmanSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (charList.get(0).isUnlocked) {
					currentCharacter.setText("Pacman");
					playerCharacter = PlayerCharacter.PACMAN;
					}
				}
			});

			msPacmanSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (charList.get(1).isUnlocked) {
					currentCharacter.setText("msPacman");
					playerCharacter = PlayerCharacter.MSPACMAN;
					}
				}
			});
			packidSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (charList.get(2).isUnlocked) {
						currentCharacter.setText("Packid");
						playerCharacter = PlayerCharacter.PACKID;
					}
				}
			});
			robotSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (charList.get(3).isUnlocked) {
					currentCharacter.setText("Robot");
					playerCharacter = PlayerCharacter.ROBOT;
					}
				}
			});
			snacSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (charList.get(4).isUnlocked) {
					currentCharacter.setText("Snac The Snake");
					playerCharacter = PlayerCharacter.SNACTHESNAKE;
					}
				}
			});
			glitchSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (charList.get(5).isUnlocked) {
					currentCharacter.setText("Glitch");
					playerCharacter = PlayerCharacter.GLITCHTHEGHOST;
					}
				}
			});

		} catch(Exception e) {
			e.printStackTrace();
		}


	}

	public static void main(String[] args) {
		launch(args);
	}

	private void enemyKilled(Character enemy) {
		player.modifyScore(ateGhostScore);
		enemy.moveTo(convertToPosition(enemy.getStartIndex()[0],true), convertToPosition(enemy.getStartIndex()[1],false));
	}

	private void playerCaught() throws InterruptedException {
		println("CAUGHT!");
		println("You have " + (player.getLives()) + " lives remaining");
		currentGameTick = 0;
		TimeUnit.SECONDS.sleep(1);
		player.decrementLives();

		restartLevel();
	}

	private int[] calculateEnemyMovement(Enemy enemy) throws PlayerCaughtException {
		int[] delta = {0,0};

		// Is this enemy colliding with the player?
		if ((Math.abs(enemy.getPosition()[0] - player.getPosition()[0]) < gridSquareSize/2) && (Math.abs(enemy.getPosition()[1] - player.getPosition()[1]) < gridSquareSize/2)) {
			if ((player.isAbilityActive() && player.getAbility() == Player.Ability.EATGHOSTS) || (player.getAbility() == Player.Ability.EATSAMECOLOR && enemy.model.getFill() == player.model.getFill()) ) {
				enemyKilled(enemy);
				sound.ghostEaten();
			}
			else if (player.getShield() != null){
				enemyKilled(enemy);
				deleteShield();
				sound.shieldHit();
			}
			else {
				sound.playerEaten();
				throw new PlayerCaughtException(); }
		}
		if (player.getAbility() == Player.Ability.SNAKE) {
			for (SnakePiece snakePiece : snakePieces) {
				if ((Math.abs(enemy.getPosition()[0] - snakePiece.getPosition()[0]) < gridSquareSize/2) && (Math.abs(enemy.getPosition()[1] - snakePiece.getPosition()[1]) < gridSquareSize/2)) {
					sound.playerEaten();
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
			if (player.getAbility() == Player.Ability.EATGHOSTS && player.isAbilityActive() ) {
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
							if (levelObjectArray[yIndex + randYIndex][xIndex + randXIndex] instanceof SolidWall) {
								validMove = false;
							}
						}
						catch (ArrayIndexOutOfBoundsException e) {validMove = false;}
					}
					chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {yIndex + randYIndex, xIndex + randXIndex});
				}
			}
			else {
				switch(enemy.getBehaviour()) {
					case HUNTER: {
						chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						break;
					}
					case AMBUSHER: {
						switch (enemy.getAmbusherState()) {
							case RETREAT:{
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
										if (levelObjectArray[randYIndex][randXIndex] instanceof SolidWall) {
											validMove = false;
										}
									}
									catch (Exception e) { validMove = false; }
								} while(!validMove);

								chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {randYIndex, randXIndex});
								enemy.manageAmbusherFSM();
								break;
							}
							case REPOSITION:{

								enemy.manageAmbusherFSM();
								break;
							}
							case AMBUSH:{
								//If far away, try to cut the player off
								//use player direction to aim ahead of the player
								//println("Time to ambush!");
								int[] del = {0,0};
								if (player.getPrevDirection() != null) {
									switch (player.getPrevDirection()) {
										case UP:    {del[1] = -3; break;}
										case DOWN:  {del[1] = 3; break;}
										case LEFT:  {del[0] = -3; break;}
										case RIGHT: {del[0] = 3; break;}
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
									if (levelObjectArray[playerYIndex+del[1]][playerXIndex + del[0]] instanceof SolidWall) {
										break;
									}
								} catch(ArrayIndexOutOfBoundsException e){break;}
								chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex+del[1], playerXIndex + del[0]});

								enemy.manageAmbusherFSM();
								break;
							}
							case CHASE:{
								//println("Chasing the player...");
								chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
								enemy.manageAmbusherFSM();
								break;
							}
							default: {break;}

						}
						break;
					}
					case GUARD: {
						int guardRadius = 10;
						//int guardYIndex = 5;
						//int guardXIndex = 6;
						Integer[] guardIndexes = findRandomValidIndexes();
						if (AdjacencyMatrix.calcDistance(new Integer[] {guardIndexes[1], guardIndexes[0]}, new Integer[] {playerYIndex, playerXIndex}) < guardRadius) {
							// Is the player near my guard point? Chase them!
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						}
						else if (AdjacencyMatrix.calcDistance(new Integer[] {yIndex, xIndex}, new Integer[] {guardIndexes[1], guardIndexes[0]}) > guardRadius) {
							// Am I far from my guard point? Move closer
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {guardIndexes[1], guardIndexes[0]});
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
									if (levelObjectArray[guardIndexes[1] - randomYCoord][guardIndexes[0] - randomXCoord] instanceof SolidWall) {
										validMove = false;
									}
								}
								catch(ArrayIndexOutOfBoundsException e) {
									validMove = false;
								}

							} while (!validMove);
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {guardIndexes[1] - randomYCoord, guardIndexes[0] - randomXCoord});
						}
						break;
					}
					/*case patrol: {
						int aggroRadius = 4;
						if (AdjacencyMatrix.calcDistance(new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex}) < aggroRadius) {
							//If close to the player, chase
							chooseMoveFromAlgorithm(enemy, new Integer[] {yIndex, xIndex}, new Integer[] {playerYIndex, playerXIndex});
						}
						else {
							//move between points
						}
						break;
					}*/
					case SCARED: {
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
				case UP: {
					//If wrapping around level...
					if ((yIndex == 0) && !(levelObjectArray[levelObjectArray.length-1][xIndex] instanceof SolidWall)){
						enemy.moveTo(convertToPosition(xIndex, true), convertToPosition(levelObjectArray.length-1, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex-1][xIndex] instanceof SolidWall) {break;}

					//Otherwise, regular move...
					delta[1] = -(int)enemy.getSpeed();
					enemy.setPrevDirection(Direction.UP); break;}
				case DOWN:{
					//If wrapping around level...
					if ((yIndex == levelObjectArray.length - 1) && !(levelObjectArray[0][xIndex] instanceof SolidWall)){
						enemy.moveTo(convertToPosition(xIndex, true), convertToPosition(0, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex+1][xIndex] instanceof SolidWall) {break;}

					//Otherwise, regular move...
					delta[1] = (int)enemy.getSpeed();
					enemy.setPrevDirection(Direction.DOWN); break;}
				case LEFT:{
					//If wrapping around level...
					if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof SolidWall)) {
						enemy.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex][xIndex-1] instanceof SolidWall) {break;}

					//Otherwise, regular move...
					delta[0] = -(int)enemy.getSpeed();
					enemy.setPrevDirection(Direction.LEFT); break;}
				case RIGHT:{
					//If wrapping around level...
					if ((xIndex == levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][0] instanceof SolidWall)) {
						enemy.moveTo(convertToPosition(0, true), convertToPosition(yIndex, false));
						break;
					}

					// Is this needed?
					if (levelObjectArray[yIndex][xIndex+1] instanceof SolidWall) {break;}

					//Otherwise, regular move...
					delta[0] = (int)enemy.getSpeed();
					enemy.setPrevDirection(Direction.RIGHT); break;}
				default:{ throw new IllegalArgumentException("No move to make!");}

			}

		}
		else { // Otherwise continue moving until you're aligned with the grid
			switch(enemy.getPrevDirection()) {
				case UP:{ delta[1] = -(int)enemy.getSpeed(); break;}
				case DOWN:{ delta[1] = (int)enemy.getSpeed(); break;}
				case LEFT:{ delta[0] = -(int)enemy.getSpeed(); break;}
				case RIGHT:{ delta[0] = (int)enemy.getSpeed(); break;}
				default: { throw new IllegalArgumentException("prevDirection is undefined");}
			}
		}

		return delta;
	}

	/**This function sets an enemy's next moves based on their algorithm attribute*/
	private void chooseMoveFromAlgorithm(Enemy enemy, Integer[] source, Integer[] destination) {
		try {
			switch (enemy.getAlgorithm()) {
				case BFS:{
					// set next moves to be the directions from enemy to player
					enemy.setNextMoves(adjMatrix.findBFSPath(source, destination));
					break;
				}
				case DFS:{
					// Since DFS's paths are so windy, we actually need to let them complete before repathing
					if (enemy.getPathLength() == 0) {
						enemy.setNextMoves(adjMatrix.findDFSPath(source, destination));
					}
					break;
				}
				case DIJKSTRA:{
					//System.out.println(targetXIndex + ", " + targetYIndex);
					enemy.setNextMoves(adjMatrix.findDijkstraPath(source, destination));
					break;
				}

				case EUCLIDEAN:{
					enemy.setNextMove(adjMatrix.findEuclideanDirection(source, destination, true));
					//println("euc");
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

	/** this function is in charge of stopping other snakePieces if the player stops moving*/
	private void manageSnakePieceMovement(){
		int xIndex = convertToIndex(player.getPosition()[0], true);
		int yIndex = convertToIndex(player.getPosition()[1], false);
		if (player.getHeldButtons().isEmpty()) {
			player.setPrevDirection(null);
		}
		else {
			boolean validMoveExists = false;
			for (int i = 0; i < player.getHeldButtons().size(); i++) {
				boolean valid = true;
				switch(player.getHeldButtons().getNFromTop(i)) {
					case UP:{
						try {
							if (levelObjectArray[yIndex-1][xIndex] instanceof Wall) {
								valid = false;
								break;
							}
						}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
					}
					case DOWN:{
						try {
							if (levelObjectArray[yIndex+1][xIndex] instanceof Wall) {
								valid = false;
								break;
							}
						}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
					}
					case LEFT:{
						try {
							if (levelObjectArray[yIndex][xIndex-1] instanceof Wall) {
								valid = false;
								break;
							}
						}catch(ArrayIndexOutOfBoundsException e) {valid = false;}
					}
					case RIGHT:{
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

	/** This function manages the player picking up pellets when they walk over them*/
	private void managePelletPickup(){
		int xIndex = convertToIndex(player.getPosition()[0], true);
		int yIndex = convertToIndex(player.getPosition()[1], false);

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

		if (player.getAbility() == Player.Ability.SNAKE) {
			if (player.incrementPelletCounter()) {
				//println("Spawning new snake bit");
				SnakePiece newPiece;
				if (snakePieces.isEmpty()) {
					newPiece = new SnakePiece(new Rectangle(gridSquareSize,gridSquareSize, Color.SEAGREEN), (int)player.getSpeed(), player);
					newPiece.moveTo(player.getPosition()[0]+gridSquareSize, player.getPosition()[1]+gridSquareSize);
				}
				else {
					SnakePiece lastPiece = snakePieces.get(snakePieces.size() - 1);
					newPiece = new SnakePiece(new Rectangle(gridSquareSize, gridSquareSize, Color.SEAGREEN), (int)player.getSpeed(), lastPiece);
					newPiece.moveTo(lastPiece.getPosition()[0]+gridSquareSize, lastPiece.getPosition()[1]+gridSquareSize);
				}
				snakePieces.add(newPiece);
				currentLevel.getChildren().add(newPiece.getModel());
			}
		}
		//Is this pickup a power pellet?
		if (((PickUp)(levelObjectArray[yIndex][xIndex])).getPickUpType() == PickUp.PickUpType.powerPellet) {
			sound.powerPelletPickup();
			usePlayerAbility(true);
		}
		else{
			sound.pelletPickup();
		}
	}

	/** This function gets the next player move by reading buttons pressed*/
	private int[] getNextPlayerMove(Player player){
		int xIndex = convertToIndex(player.getPosition()[0], true);
		int yIndex = convertToIndex(player.getPosition()[1], false);
		int[] delta = {0,0};
		for (int n = 0; n< Integer.min(player.getHeldButtons().size(), 2) ; n++) {

			if((player.getHeldButtons().getNFromTop(n) == Direction.UP) ) {
				if (player.getIsGhost()) {
					if ((yIndex != 0) && !(levelObjectArray[yIndex-1][xIndex] instanceof SolidWall)) {
						delta[1] = -(int)player.getSpeed();
						player.setPrevDirection(Direction.UP);
						break;
					}
					else if ((yIndex == 0) && !(levelObjectArray[levelObjectArray.length-1][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), (levelObjectArray.length-1)*gridSquareSize + levelOffsetY);
					}
				}
				else {
					if ((yIndex != 0) && !(levelObjectArray[yIndex-1][xIndex] instanceof Wall)) {
						delta[1] = -(int)player.getSpeed();
						player.pointModel(Direction.UP);
						player.setPrevDirection(Direction.UP);
						break;
					}
					else if ((yIndex == 0) && !(levelObjectArray[levelObjectArray.length-1][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), (levelObjectArray.length-1)*gridSquareSize + levelOffsetY);
					}
				}
			}
			else if(player.getHeldButtons().getNFromTop(n) == Direction.DOWN) {
				if (player.getIsGhost()) {
					if ((yIndex != levelObjectArray.length - 1) && !(levelObjectArray[yIndex+1][xIndex] instanceof SolidWall)) {
						delta[1] = (int)player.getSpeed();
						player.setPrevDirection(Direction.DOWN);
						break;
					}
				}
				else {
					if ((yIndex != levelObjectArray.length - 1) && !(levelObjectArray[yIndex+1][xIndex] instanceof Wall)) {
						delta[1] = (int)player.getSpeed();
						player.pointModel(Direction.DOWN);
						player.setPrevDirection(Direction.DOWN);
						break;
					}
					else if ((yIndex == levelObjectArray.length - 1) && !(levelObjectArray[0][xIndex] instanceof Wall)){
						player.moveTo(convertToPosition(xIndex, true), levelOffsetY);
					}
				}
				
			}
			else if(player.getHeldButtons().getNFromTop(n) == Direction.LEFT) {
				if (player.getIsGhost()) {
					if ((xIndex != 0) && !(levelObjectArray[yIndex][xIndex-1] instanceof SolidWall)) {
						delta[0] = -(int)player.getSpeed();

						player.setPrevDirection(Direction.LEFT);
						break;
					}
					else if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof Wall)) {
						player.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
					}
				}
				else {
					if ((xIndex != 0) && !(levelObjectArray[yIndex][xIndex-1] instanceof Wall)) {
						delta[0] = -(int)player.getSpeed();
						player.pointModel(Direction.LEFT);
						player.setPrevDirection(Direction.LEFT);
						break;
					}
					else if ((xIndex == 0) && !(levelObjectArray[yIndex][levelObjectArray[0].length - 1] instanceof Wall)) {
						player.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), convertToPosition(yIndex, false));
					}
				}			
			}
			else if(player.getHeldButtons().getNFromTop(n) == Direction.RIGHT) {
				if (player.getIsGhost()) {
					if ((xIndex != levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][xIndex+1] instanceof SolidWall)) {
						delta[0] = (int)player.getSpeed();
						player.setPrevDirection(Direction.RIGHT);
						break;
					}
					else if ((xIndex == levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][0] instanceof Wall)) {
						player.moveTo(convertToPosition(0, true), convertToPosition(yIndex, false));
					}
				}
				else {
					if ((xIndex != levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][xIndex+1] instanceof Wall)) {
						delta[0] = (int)player.getSpeed();
						player.pointModel(Direction.RIGHT);
						player.setPrevDirection(Direction.RIGHT);
						break;
					}
					else if ((xIndex == levelObjectArray[0].length - 1) && !(levelObjectArray[yIndex][0] instanceof Wall)) {
						player.moveTo(convertToPosition(0, true), convertToPosition(yIndex, false));
					}
				}
			}
		}
		return delta;
	}

	/**This function manages picking up pellets when the player uses the pelletMagnet boost */
	private void managePelletMagnet(){
		int xIndex = convertToIndex(player.getPosition()[0], true);
		int yIndex = convertToIndex(player.getPosition()[1], false);
		int pickupRadius = player.getPickupRadius();

		for (int i = -pickupRadius; i <= pickupRadius; i++){
			for (int j = -pickupRadius ; j <= pickupRadius; j++){
				try{
					if (AdjacencyMatrix.calcDistance(new Integer[] {xIndex, yIndex}, new Integer[] {xIndex + i, yIndex + j}) <= pickupRadius) {
						if (levelObjectArray[yIndex + j][xIndex + i] instanceof PickUp){
							if (((PickUp)levelObjectArray[yIndex + j][xIndex + i]).getPickUpType() == PickUp.PickUpType.powerPellet){
								usePlayerAbility(true);
								sound.powerPelletPickup();
							}
							else {
								sound.pelletPickup();
							}
							player.modifyScore(((PickUp)(levelObjectArray[yIndex + j][xIndex + i])).getScoreValue());
							currentScoreText.setText(player.getScoreString());
							currentLevel.getChildren().remove((levelObjectArray[yIndex + j][xIndex + i].getContainer()));
							levelObjectArray[yIndex + j][xIndex + i] = null;
							pelletsRemaining--;
						}
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {}
			}
		}
	}

	private int[] calculatePlayerMovement(Player player) throws LevelCompleteException, PlayerCaughtException{
		int[] delta = {0,0};
		// Is this playerGhost colliding with the player?
		if(player.getIsGhost()) {
			if ((Math.abs(player.getPosition()[0] - playerList.get(0).getPosition()[0]) < gridSquareSize/2) && (Math.abs(player.getPosition()[1] - playerList.get(0).getPosition()[1]) < gridSquareSize/2)) {
				if ((playerList.get(0).isAbilityActive() && playerList.get(0).getAbility() == Player.Ability.EATGHOSTS) || (playerList.get(0).getAbility() == Player.Ability.EATSAMECOLOR && player.getModel().getFill() == player.getModel().getFill()) ) {
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

			/* If the player is wall jumping, we need to take over for one gridSquare */
			if (playerIsWallJumping) {
				if (levelObjectArray[yIndex][xIndex] instanceof Wall) {
					playerIsWallJumping = false;
				}

				switch (player.getPrevDirection()) {
					case UP: {delta[1] = -(int)player.getSpeed(); break;}
					case DOWN: {delta[1] = (int)player.getSpeed(); break;}
					case LEFT: {delta[0] = -(int)player.getSpeed(); break;}
					case RIGHT: {delta[0] = (int)player.getSpeed(); break;}
					default: {break;}
				}
			}

			//This bit is for the snake player, who needs to stop all his other pieces if he stops
			if (player.getAbility() == Player.Ability.SNAKE) {
				manageSnakePieceMovement();
			}

			if(levelObjectArray[yIndex][xIndex] instanceof PickUp && !player.getIsGhost()) {
				managePelletPickup();
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

			if (isBoostActive && ( player.getBoost() == Player.Boost.SUPERPELLETMAGNET ||  player.getBoost() == Player.Boost.PELLETMAGNET)){
				managePelletMagnet();
			}
			//Loop through the held movement keys in order of preference
			delta = getNextPlayerMove(player);
		}
		else {
			switch (player.getPrevDirection()) {
				case UP: {delta[1] = -(int)player.getSpeed(); break;}
				case DOWN: {delta[1] = (int)player.getSpeed(); break;}
				case LEFT: {delta[0] = -(int)player.getSpeed(); break;}
				case RIGHT: {delta[0] = (int)player.getSpeed(); break;}
				default: {break;}
			}
		}
		return delta;
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
				type[0] = Wall.WallType.CROSS;
				type[1] = Direction.UP;
				break;
			}
			case (3): {
				type[0] = Wall.WallType.TEE;
				if (!northNeighbour) {
					type[1] = Direction.DOWN;}
				else if (!southNeighbour) {
					type[1] = Direction.UP;
				}
				else if (!leftNeighbour) {
					type[1] = Direction.RIGHT;
				}
				else {
					type[1] = Direction.LEFT;
				}
				break;
			}
			case (2): {
				if (northNeighbour && southNeighbour) {
					type[0] = Wall.WallType.STRAIGHT;
					type[1] = Direction.UP;
				}
				else if (leftNeighbour && rightNeighbour) {
					type[0] = Wall.WallType.STRAIGHT;
					type[1] = Direction.RIGHT;
				}
				else {
					type[0] = Wall.WallType.CORNER;
					if (southNeighbour && rightNeighbour) {
						type[1] = Direction.UP;
					}
					else if (leftNeighbour && southNeighbour) {
						type[1] = Direction.DOWN;
					}
					else if (northNeighbour && leftNeighbour) {
						type[1] = Direction.RIGHT;
					}
					else {
						type[1] = Direction.LEFT;
					}
				}
				break;
			}
			case (1): {
				type[0] = Wall.WallType.END;
				if (northNeighbour) {
					type[1] = Direction.UP;
				}
				else if (southNeighbour) {
					type[1] = Direction.DOWN;
				}
				else if (leftNeighbour) {
					type[1] = Direction.LEFT;
				}
				else {
					type[1] = Direction.RIGHT;
				}
				break;
			}
			case(0): {
				type[0] = Wall.WallType.SINGLE;
				type[1] = Direction.UP;
				break;
			}
			default: {throw new UnsupportedOperationException();}
		}
		return type;
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
			case 0: {array[0] = Enemy.Intelligence.DUMB; break;}
			case 1: {array[0] = Enemy.Intelligence.MODERATE; break;}
			case 2: {array[0] = Enemy.Intelligence.SMART; break;}
			case 3: {array[0] = Enemy.Intelligence.PERFECT; break;}
		}

		int threeExponent = 0;
		while (num%3 == 0) {
			threeExponent++;
			num = num/3;
		}
		switch (threeExponent) {
			case 0: {array[1] = Enemy.Behaviour.HUNTER; break;}
			case 1: {array[1] = Enemy.Behaviour.AMBUSHER; break;}
			case 2: {array[1] = Enemy.Behaviour.GUARD; break;}
			case 3: {array[1] = Enemy.Behaviour.patrol; break;}
			case 4: {array[1] = Enemy.Behaviour.SCARED; break;}
		}

		int fiveExponent = 0;
		while (num%5 == 0) {
			fiveExponent++;
			num = num/5;
		}
		switch (fiveExponent) {
			case 0: {array[2] = Enemy.Algorithm.DIJKSTRA; break;}
			case 1: {array[2] = Enemy.Algorithm.EUCLIDEAN; break;}
			case 2: {array[2] = Enemy.Algorithm.BFS; break;}
			case 3: {array[2] = Enemy.Algorithm.DFS; break;}
		}

		return array;
	}

	/**
	 * This function controls the players ability usage.
	 * A boolean parameter shows if this is being activated from the user pressing the ability button, or from a power pellet being collected*/
	private void usePlayerAbility(boolean fromPickup) {
		if (fromPickup == false) {
			switch (player.getAbility()) {
				case LASER:{
					if (player.getAbilityCharges() == 0) {
						return;
					}
					else {
						fireLaser();
					}

					break;
				}
				case WALLJUMP:{
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
				case EATGHOSTS: {
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
				case LASER:
				case WALLJUMP: {player.incrementAbilityCharges(); break;}

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
					case UP:{delta[1] = -2; break;}
					case DOWN:{delta[1] = 2; break;}
					case LEFT:{delta[0] = -2; break;}
					case RIGHT:{delta[0] = 2; break;}
				}

				if(levelObjectArray[yIndex + delta[1]][xIndex + delta[0]] instanceof Wall) {
					return;
				}
				else {
					playerIsWallJumping = true;
					player.setPrevDirection(player.getHeldButtons().getTop());
					player.decrementAbilityCharges();
					sound.wallJump();
				}
			}
			catch (ArrayIndexOutOfBoundsException e)  {;	}
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
			default:
			case UP:
			case DOWN: {isHorizontal = false; break;}

			case LEFT:
			case RIGHT: {isHorizontal = true; break;}
			}
		}
		else {
			switch(player.getHeldButtons().getTop()) {
				default:
				case UP:
				case DOWN:{isHorizontal = false; break;}

				case LEFT:
				case RIGHT: {isHorizontal = true; break;}
			}
		}

		if (isHorizontal) {
			height = 1.5*gridSquareSize;
			xPos = 0.0;
			yPos = player.getPosition()[1] - height/2.0 - 5;

		}
		else {
			width = 1.5*gridSquareSize;
			xPos = player.getPosition()[0] - width/2.0 - 5;
			yPos = 0.0;
		}
		if (laserFactory.createNewLaser(xPos, yPos, isHorizontal)) {
			currentLevel.getChildren().add(laserFactory.getLaserGroup());
			player.decrementAbilityCharges();
			sound.laser();
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

	private Circle createShield(Color color) {
		Circle shield = new Circle(gridSquareSize/1.5, color);
		shield.relocate(player.getPosition()[0] - shield.getRadius() - gridSquareSize/2, player.getPosition()[1] - shield.getRadius() - gridSquareSize/2 );
		shield.setOpacity(0.5);

		if (player.getModel() instanceof Circle){
			shield.layoutXProperty().bind(player.getModel().layoutXProperty());
			shield.layoutYProperty().bind(player.getModel().layoutYProperty());
			shield.translateXProperty().bind(player.getModel().translateXProperty());
			shield.translateYProperty().bind(player.getModel().translateYProperty());
		}
		else {
			shield.layoutXProperty().bind(player.getModel().layoutXProperty());
			shield.layoutYProperty().bind(player.getModel().layoutYProperty());
		}


		currentLevel.getChildren().add(shield);
		sound.activateShield();

		return shield;
	}

	private void deleteShield(){
		currentLevel.getChildren().remove(player.getShield());
		if (player.getShield() != null){
			sound.shieldHit();
		}
		player.clearShield();
	}

	private void invertHeldKeys(){
		SetArrayList<Direction> heldKeys = player.getHeldButtons();
		SetArrayList<Direction> newHeldKeys = new SetArrayList<Direction>();
		for (int i = 0; i < heldKeys.size(); i++){
			Direction direction = heldKeys.getNFromTop(heldKeys.size() - (i + 1));
			Direction newDirection;
			switch (direction){
				case UP:{newDirection = Direction.DOWN; break;}
				case DOWN:{newDirection = Direction.UP; break;}
				case LEFT:{newDirection = Direction.RIGHT; break;}
				case RIGHT:{newDirection = Direction.LEFT; break;}
				default:{newDirection = Direction.UP; break;}
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
				case TIMESLOW:{slowTime(false); break;}
				case SUPERTIMESLOW:{slowTime(true); break;}

				/*We may end up misaligned if we change speed whilst not aligned with the grid, so set a flag and do it when we are aligned.*/
				case DASH:
				case SUPERDASH:{ waitingForGridAlignment = true; break;}

				case PELLETMAGNET:
				case SUPERPELLETMAGNET:{ player.setPickupRadius(player.getBoost()); break;}

				case INVISIBILITY:
				case SUPERINVISIBILITY: {player.setInvisible(true); break;}

				case SHIELD: {player.setShield(createShield(Color.BLUE)); break;}
				case SUPERSHIELD: {player.setShield(createShield(Color.BLUE)); break;}

				case INVERTCONTROLS:{
					player.setControlsInverted(true);
					invertHeldKeys();
					break;
				}
				case RANDOMTELEPORT:{
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
				case RANDOM: {
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

	private void disableBoost(){
		switch(player.getBoost()){
			case TIMESLOW:
			case SUPERTIMESLOW:{
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

			case DASH:
			case SUPERDASH:{player.resetSpeed(); break;}

			case PELLETMAGNET:
			case SUPERPELLETMAGNET:{player.resetPickupRadius(); break;}

			case INVISIBILITY:
			case SUPERINVISIBILITY: {player.setInvisible(false); break;}

			case SHIELD:
			case SUPERSHIELD:  {deleteShield(); break;}

			case RANDOMTELEPORT:{ break; }
			case INVERTCONTROLS:{
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
		boostDuration = (isSuper ? Player.Boost.SUPERTIMESLOW : Player.Boost.TIMESLOW).duration();
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
					case UP:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[1] == 0) {
								//Wrap around
								snakePiece.moveTo(snakePiece.getPosition()[0], convertToPosition(levelObjectArray.length-1, false));
							}
						}
						snakePiece.moveBy(0, (int)-snakePiece.getSpeed());
						break;
					}
					case DOWN:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[1] == levelObjectArray.length - 1) {
								//Wrap around
								snakePiece.moveTo(snakePiece.getPosition()[0], convertToPosition(0, false) );
							}
						}
						snakePiece.moveBy(0, (int)snakePiece.getSpeed());
						break;
					}
					case LEFT:{
						if (isGridAligned(snakePiece)) {
							if (snakePiece.getPrevIndex()[0] == 0) {
								//Wrap around
								snakePiece.moveTo(convertToPosition(levelObjectArray[0].length - 1, true), snakePiece.getPosition()[1]);
							}
						}
						snakePiece.moveBy((int)-snakePiece.getSpeed(), 0);
						break;
					}
					case RIGHT:{
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

}


