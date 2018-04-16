package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class PickUp extends LevelObject {

	public static enum PickUpType {
		pellet,
		powerPellet,
		powerUp,
	}

	private PickUpType type;

	private final static double powerPelletSize = Main.gridSquareSize / 5.0; // 4 when gridsquare = 20

	
	public PickUp(int ID) {
		super();
		switch(ID){
			case 4:{ //Pellet, increases score by 10
				type = PickUpType.pellet;
				Polygon shape = new Polygon();
				shape.setFill(Color.LIGHTSKYBLUE);
				shape.getPoints().addAll(new Double [] {0.0,-powerPelletSize/1.6, powerPelletSize/1.6,0.0, powerPelletSize*2.5,powerPelletSize*2.5, powerPelletSize/1.6,0.0, 0.0,powerPelletSize/1.6, -powerPelletSize/1.6, 0.0});
				super.model = shape;
				break;
				}

			case 5:{ //Power Pellet, increases score by 100, has an effect
				type = PickUpType.powerPellet;
				Polygon shape = new Polygon();
				shape.setFill(Color.GOLD);
				shape.getPoints().addAll(new Double [] {-powerPelletSize,-powerPelletSize, powerPelletSize,-powerPelletSize, powerPelletSize,powerPelletSize, powerPelletSize*2.5,powerPelletSize*2.5, powerPelletSize,powerPelletSize, -powerPelletSize,powerPelletSize});
				super.model = shape;
				break;
			}

			case 6:{//PowerUp, has no score value
				type = PickUpType.powerUp;
				Shape shape = new Circle();
				shape.setFill(Color.GREENYELLOW);
				super.model = shape;
				break;
			}
			default:{break;}
		}
		model.layoutXProperty().bind(container.layoutXProperty());
		model.layoutYProperty().bind(container.layoutYProperty());
		model.translateXProperty().bind(container.translateXProperty());
		model.translateYProperty().bind(container.translateYProperty());
		width = container.getBoundsInLocal().getWidth();
		height = container.getBoundsInLocal().getHeight();
		container.getChildren().add(model);
		regularModel = model;
	}

	public int getScoreValue() {
		if (type == PickUpType.pellet) {
			return 10;
		}
		else if (type == PickUpType.powerPellet) {
			return 100;
		}
		else {
			return 0;
		}
	}
	public PickUpType getPickUpType() {
		return type;
	}

}
