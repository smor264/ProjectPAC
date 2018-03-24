package application;


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
	private static enum Direction {
		up,
		down,
		left,
		right,
	}
	
	Player player = new Player(new Circle(10, Color.YELLOW), 2);
	SetArrayList<Direction> directionArray = new SetArrayList<Direction>();

	
	@Override
	public void start(Stage primaryStage) {
		try {
			Group level = new Group(player.getModel());
			Scene scene = new Scene(level, windowWidth, windowHeight, Color.BLACK);

			player.moveTo(centre[0], centre[1]);

			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
						case UP: {
							directionArray.append(Direction.up);
							break;
						}
						case DOWN: {
							directionArray.append(Direction.down);
							break;
						}
						case LEFT: {
							directionArray.append(Direction.left);
							break;
						}
						case RIGHT: {
							directionArray.append(Direction.right);
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
					case UP: { 
						directionArray.remove(Direction.up); 
						break;
						}
					case DOWN: {
						directionArray.remove(Direction.down);
						break;
						}
					case LEFT: {
						directionArray.remove(Direction.left);
						break;
						}
					case RIGHT: {
						directionArray.remove(Direction.right);
						break;
						}
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
						dy -= player.getSpeed();
					}
					if(directionArray.getTop() == Direction.down) {
						dy += player.getSpeed();
					}
					if(directionArray.getTop() == Direction.left) {
						dx -= player.getSpeed();
					}
					if(directionArray.getTop() == Direction.right) {
						dx += player.getSpeed();
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
}
