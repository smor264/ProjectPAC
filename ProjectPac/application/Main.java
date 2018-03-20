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
	boolean goUp, goDown, goLeft, goRight;
	//Circle player = new Circle(10, Color.YELLOW);
	Player player = new Player(new Circle(10, Color.YELLOW), 1);
	
	@Override
	public void start(Stage primaryStage) {
		try {
			Group level = new Group(player.getModel());
			Scene scene = new Scene(level,800,800,Color.BLACK);

			player.moveTo(400, 400);

			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
					case UP: goUp = true; break;
					case DOWN: goDown = true; break;
					case LEFT: goLeft = true; break;
					case RIGHT: goRight = true; break;
					default:
						break;
					}
				}

			});

			scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
					case UP: goUp = false; break;
					case DOWN: goDown = false; break;
					case LEFT: goLeft = false; break;
					case RIGHT: goRight = false; break;
					default:
						break;
					}
				}

			});


			primaryStage.show();
			primaryStage.setScene(scene);


			AnimationTimer timer = new AnimationTimer() {
				@Override
				public void handle(long now) {
					int dx = 0, dy = 0;

					if(goUp) dy -= player.getSpeed();
					if(goDown) dy += player.getSpeed();
					if(goLeft) dx -= player.getSpeed();
					if(goRight) dx += player.getSpeed();

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
