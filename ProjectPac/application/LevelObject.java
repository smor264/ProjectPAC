package application;

import javafx.scene.shape.Shape;

public abstract class LevelObject {
	protected int width = Main.gridSquareSize;
	protected int height = Main.gridSquareSize;

	protected Shape model;

	public LevelObject() {

	}

	public LevelObject(Shape model) {
		this.model = model;
	}

	public Shape getModel() {
		return model;
	}
	public double[] getPosition() {
		return (new double[] {model.getLayoutX()+ width/2, model.getLayoutY() + height/2});
	}

	public void moveBy(int dx, int dy) {
		if(dx == 0 && dy == 0) return;

		final double cx = model.getBoundsInLocal().getWidth()/2;
		final double cy = model.getBoundsInLocal().getHeight()/2;

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