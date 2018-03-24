package application;

import javafx.scene.shape.Shape;

public abstract class LevelObject {
	
	private Shape model;
	
	public LevelObject(Shape model) {
		this.model = model;
	}
	
	public Shape getModel() {
		return model;
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

		if((x - 2*cx >= 0) && (x <= 800) && (y - 2*cy >= 0) && (y <= 800)) {
			model.relocate(x-2*cx, y-2*cy);
		}
	}
}