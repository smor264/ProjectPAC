package application;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public abstract class LevelObject {
	protected double width = Main.gridSquareSize;
	protected double height = Main.gridSquareSize;

	protected Shape model;

	public LevelObject() {

	}

	public LevelObject(Shape model) {
		this.model = model;
		
		int mult = 1;
		if (model instanceof Rectangle) {
			//God only knows why, but Rectangles behave weirdly. This band-aid fixes it
			mult = 2;
		}
		width = model.getBoundsInLocal().getWidth() * mult;
		height = model.getBoundsInLocal().getHeight() * mult;
		
		// Offset to ensure that the object is always in the centre of the grid, regardless of it's size
		this.model.setTranslateX(width/(2*mult) - Main.gridSquareSize/2);
		this.model.setTranslateY(height/(2*mult) - Main.gridSquareSize/2);
	}

	public Shape getModel() {
		return model;
	}
	
	public double[] getPosition() {
		return (new double[] {model.getLayoutX() + width/2, model.getLayoutY() + height/2});
	}

	public void moveBy(int dx, int dy) {
		if(dx == 0 && dy == 0) return;
		
		final double cx = width/2.0;
		final double cy = height/2.0;
		double x = cx + model.getLayoutX() + dx;
		double y = cy + model.getLayoutY() + dy;
						
		moveTo(x,y);
	};

	public void moveTo(double x, double y) {
		final double cx = model.getBoundsInLocal().getWidth()/2;
		final double cy = model.getBoundsInLocal().getHeight()/2;
		//System.out.println("moving object to " + Double.toString(x-2*cx) + ", " + Double.toString(y-2*cy));
		if((x - 2*cx >= 0) && (x <= Main.windowWidth) && (y - 2*cy >= 0) && (y <= Main.windowHeight)) {
			model.relocate(x-2*cx, y-2*cy);
		}
	}
}