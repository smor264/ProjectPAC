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
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class Main extends Application {
	public static int windowWidth = 1280;
	public static int windowHeight = 720;
	public static int[] centre = {windowWidth/2, windowHeight/2};
	public static int levelWidth = 10;
	public static int levelHeight = 10;
	public static enum Direction {
		up,
		down,
		left,
		right,
	}
	
	LevelObject[][] levelObjectArray = new LevelObject[levelWidth][levelHeight];
	
	Player player = new Player(new Circle(10, Color.YELLOW), 2);
	SetArrayList<Direction> directionArray = new SetArrayList<Direction>();
	
	//Wall wall = new Wall(Wall.WallType.end, Direction.up);
	
	Group currentLevel = new Group(player.getModel());
	
	Scene scene = new Scene(currentLevel, windowWidth, windowHeight, Color.GREY);
	Level test = new Level();
	
	
	private void initialiseLevel(Level level) {
		int[][] array = level.getArray();
		System.out.println(array.length);
		System.out.println(array[0].length);
		boolean playerExists = false;
		for (int i = 0; i < array[0].length; i++) {
			for (int j = 0; j< array.length; j++) {
				if (array[i][j] == 1) { // If wall exists
					// Actual code
					/*ArrayList<Object> wallType = new ArrayList<Object>();
					wallType = determineWallType(array,i,j);
					
					Wall wall = new Wall((Wall.WallType)wallType.get(0), (Direction)wallType.get(1));
					levelObjectArray[i][j] = wall;
					
					wall.moveTo(10*i+10, 10*j+10);
					currentLevel.getChildren().add(wall.getModel());*/
					
					// Test code
					Wall wall = new Wall(Wall.WallType.full, Direction.up); // Make all walls full type
					System.out.println("Making a wall at x pos:" + (10*i+100) + ", and ypos:" + (10*j+100));
					wall.moveTo(LevelObject.width*i+100, LevelObject.height*j+100);
					currentLevel.getChildren().add(wall.getModel());
				}
				else if (array[i][j] == 2) { // player
					if (playerExists){
						throw new UnsupportedOperationException();
					}
					else {
						player.moveTo(LevelObject.width*i + 100, LevelObject.height*j + 100);
						playerExists = true;
					}
				}
			}
		}
	}
	

	
	@Override
	public void start(Stage primaryStage) {
		try {
			initialiseLevel(test);
			//player.moveTo(centre[0], centre[1]);
			//wall.moveTo(centre[0],centre[1]);
			
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
					int dx = 0, dy = 0;

					if(directionArray.getTop() == Direction.up) {
						dy = -(int)player.getSpeed();
					}
					else if(directionArray.getTop() == Direction.down) {
						dy = (int)player.getSpeed();
					}
					else if(directionArray.getTop() == Direction.left) {
						dx = -(int)player.getSpeed();
					}
					else if(directionArray.getTop() == Direction.right) {
						dx = (int)player.getSpeed();
					}
					player.moveBy(dx, dy);
				}
			};

			timer.start();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		launch(args);
	}
	
	private ArrayList<Object> determineWallType(int[][] array, int i, int j) {
		boolean northNeighbour = false, southNeighbour = false, leftNeighbour = false, rightNeighbour = false;
		try {
			if (array[i-1][j] == 1) {
				northNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		try {
			if (array[i+1][j] == 1) {
				southNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		try {
			if (array[i][j-1] == 1) {
				leftNeighbour = true;
			}
		}
		catch(ArrayIndexOutOfBoundsException e){;}
		
		try {
			if (array[i][j+1] == 1) {
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
						type.add(Direction.right);
					}
					else if (northNeighbour && leftNeighbour) {
						type.add(Direction.down);
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
