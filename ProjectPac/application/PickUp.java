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

	private final static double powerPelletSize = 4.0;


	public PickUp(int ID) {
		switch(ID){
			case 4:{ //Pellet, increases score by 10
				type = PickUpType.pellet;
				Polygon shape = new Polygon();
				shape.setFill(Color.LIGHTSKYBLUE);
				shape.getPoints().addAll(new Double [] {0.0,-2.5, 2.5,0.0, 10.0,10.0, 2.5,0.0, 0.0,2.5, -2.5, 0.0});
				super.model = shape;
				break;
				}

			case 5:{ //Power Pellet, increases score by 100, has an effect
				type = PickUpType.powerPellet;
				Polygon shape = new Polygon();
				shape.setFill(Color.GOLD);
				shape.getPoints().addAll(new Double [] {-powerPelletSize,-powerPelletSize, powerPelletSize,-powerPelletSize, powerPelletSize,powerPelletSize, 10.0,10.0, powerPelletSize,powerPelletSize, -powerPelletSize,powerPelletSize});
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

}
