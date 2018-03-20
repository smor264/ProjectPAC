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
	Circle Pacman = new Circle(10, Color.YELLOW);
	@Override
	public void start(Stage primaryStage) {
		try {
			Group level = new Group(Pacman);
			Scene scene = new Scene(level,800,800,Color.BLACK);

			movePacmanTo(400, 400);

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

					if(goUp) dy -= 1;
					if(goDown) dy += 1;
					if(goLeft) dx -= 1;
					if(goRight) dx += 1;

					movePacBy(dx, dy);
				}
			};

			timer.start();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void movePacBy(int dx, int dy) {
		if(dx == 0 && dy == 0) return;

		final double cx = Pacman.getBoundsInLocal().getWidth()/2;
		final double cy = Pacman.getBoundsInLocal().getHeight()/2;

		double x = cx + Pacman.getLayoutX() + dx;
		double y = cy + Pacman.getLayoutY() + dy;

		movePacmanTo(x,y);
	};

	private void movePacmanTo(double x, double y) {
		final double cx = Pacman.getBoundsInLocal().getWidth()/2;
		final double cy = Pacman.getBoundsInLocal().getHeight()/2;

		System.out.println("Xpos is: " + x);
		System.out.println("ypos is:" + y);

		if((x - cx >= 0) && (x + cx <= 800) && (y - cy >= 0) && (y + cy <= 800)) {
			Pacman.relocate(x-2*cx, y-cy);
		}

		System.out.println("UpdatedXpos is: " + x);
		System.out.println("Updatedypos is:" + y);

	}


	public static void main(String[] args) {
		launch(args);
	}
}
