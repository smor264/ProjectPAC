package application;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**LevelObject is any object that is designed to be in the level, e.g Enemies, Players, Walls, Pickups, etc.
 * A LevelObject has a model and a StackPane that the model sits inside. 
 * So long as the model is contained within one tile ( < squareGridSize * squareGridSize), changing the model does not move the LevelObject.*/
public abstract class LevelObject {
	protected double width = Main.gridSquareSize;
	protected double height = Main.gridSquareSize;
	protected Shape regularModel;
	protected Shape hitbox = new Polygon(-width/2,-height/2, -width/2,height/2, width/2,height/2, width/2,-height/2);
	protected Shape model;
	protected StackPane container = new StackPane(hitbox);
	
	public LevelObject() {
		hitbox.setVisible(false);
	}

	public LevelObject(Shape model) {
		hitbox.setVisible(false);
		this.model = model;
		regularModel = model;
		container.getChildren().add(model);
		
		this.model.layoutXProperty().bind(container.layoutXProperty());
		this.model.layoutYProperty().bind(container.layoutYProperty());
		
		int mult = 1;
		if (model instanceof Rectangle) {
			//God only knows why, but Rectangles behave weirdly. This band-aid fixes it
			mult = 2;
		}
		width = container.getBoundsInLocal().getWidth() * mult;
		height = container.getBoundsInLocal().getHeight() * mult;
		
		// Because rectangles are pieces of crap, they need to be offset.
		if (model instanceof Rectangle) {
			this.model.setTranslateX(-width/(2*mult));
			this.model.setTranslateY(-height/(2*mult));
		}
	}

	public Shape getModel() {
		return model;
	}
	public void setModel(Shape model){
		container.getChildren().remove(this.model);
		this.model = model;
		container.getChildren().add(this.model);
		regularModel = model;
		this.model.layoutXProperty().bind(container.layoutXProperty());
		this.model.layoutYProperty().bind(container.layoutYProperty());
		if (model instanceof Rectangle) {
			this.model.setTranslateX(-width/(2*2));
			this.model.setTranslateY(-height/(2*2));
		}
	}
	public void resetModel(){
		model = regularModel;
	}
	
	public StackPane getContainer() {
		return container;
	}
	
	public double[] getPosition() {
		return (new double[] {container.getLayoutX(), container.getLayoutY()});
	}

	public void moveBy(int dx, int dy) {
		if(dx == 0 && dy == 0) return;
		
		final double cx = 0;
		final double cy = 0;
		double x = cx + container.getLayoutX() + dx;
		double y = cy + container.getLayoutY() + dy;
						
		moveTo(x,y);
	};

	public void moveTo(double x, double y) {
		final double cx = container.getBoundsInLocal().getWidth()/2;
		final double cy = container.getBoundsInLocal().getHeight()/2;

		if((x - 2*cx >= 0) && (x <= Main.windowWidth) && (y - 2*cy >= 0) && (y <= Main.windowHeight)) {
			container.relocate(x-2*cx, y-2*cy);
		}
	}
}