package application;

import java.util.Arrays;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Laser {
	private double width;
	private double height;
	
	private Rectangle white;
	private Rectangle orange;
	private Rectangle red;
	
	private Group lasers;
	private Integer animationTick = null;
	
	private double xPos;
	private double yPos;
	private boolean isHorizontal;
	
	public Laser() {
		
	}
	/**
	 * Creates a new laser at the given position in the given direction. Returns true if creation was successful, otherwise false.
	 * */
	public boolean createNewLaser(double xPos, double yPos, boolean isHorizontal){
		if (animationTick != null) {
			return false;
		}
		else {
			animationTick = 0;
			this.xPos = xPos;
			this.yPos = yPos;
			this.isHorizontal = isHorizontal;
			
			if (isHorizontal) {
				width = Main.windowWidth;
				height = Main.gridSquareSize * 1.5;
			}
			else {
				width = Main.gridSquareSize * 1.5;
				height = Main.windowHeight;
			}
			
			if (isHorizontal) {		
				white = new Rectangle(width, height , Color.WHITE);
				orange = new Rectangle(width, height * 1.1, Color.ORANGE);
				red = new Rectangle(width, height * 1.2, Color.RED);
				
				white.setTranslateY(0);
				orange.setTranslateY(- height * 0.05);
				red.setTranslateY(- height * 0.1);
				
				
			}
			else {
				white = new Rectangle(width , height, Color.WHITE);
				orange = new Rectangle(width * 1.1, height, Color.ORANGE);
				red = new Rectangle(width * 1.2, height, Color.RED);

				white.setTranslateX(0);
				orange.setTranslateX(- width * 0.05);
				red.setTranslateX(- width * 0.1);
			}
			
			white.setOpacity(0.9);
			orange.setOpacity(0.8);
			red.setOpacity(0.6);
			
			lasers = new Group(Arrays.asList(red, orange, white));
			lasers.relocate(xPos, yPos);
			lasers.toFront();
			
			if (isHorizontal) {
				white.setScaleY(1/1.5 * 1/3.0 * 1/2.0);
				orange.setScaleY(1/1.28 * 1/2.0 * 1/2.0);
				red.setScaleY(1/1.05 * 1/1.5 * 1/2.0);
			}
			else {
				white.setScaleX(1/1.5 * 1/3.0 * 1/2.0);
				orange.setScaleX(1/1.28 * 1/2.0 * 1/2.0);
				red.setScaleX(1/1.05 * 1/1.5 * 1/2.0);
			}
			
			return true;
		}
	}
	
	/*I hand animated a few frames and then found functions roughly equivalent to my hand drawn frames*/
	//I modelled the wind-up as an exponential
	private double whiteExponential(int x) {
		return 0.0848*Math.exp(0.32*x);
	}
	private double orangeExponential(int x) {
		return 0.176*Math.exp(0.23*x);
	}
	private double redExponential(int x) {
		return 0.329*Math.exp(0.154*x);
	}
	
	//Then the middle needed to join the increasing exponential with the decreasing polynomial
	private double whiteSine(int x) {
		return 0.8*Math.sin(x/9.0 - 0.1)+0.3;
	}
	private double orangeSine(int x) {
		return 0.8*Math.sin(x/10.0)+0.35;
	}
	private double redSine(int x) {
		return Math.sin(x/10.0 - 0.1)+0.4;
	}
	
	
	//The wind-down I modelled as a polynomial
	private double whitePolynomial(int x) {
		return 1.15 + -0.0506*x+ 8.15/(10000)*Math.pow(x,2) - 5.69/(1000000)*Math.pow(x,3)+1.46/(100000000)*Math.pow(x,4);
	}
	private double orangePolynomial(int x) {
		return 1.12 - 0.0317*x +2.48/(10000)*Math.pow(x,2) - 6.17/(100000000)*Math.pow(x,3) - 3.7/(1000000000)*Math.pow(x,4);
	}
	private double redPolynomial(int x) {
		return 0.981 -9.26/10000*x -5.35/(10000)*Math.pow(x,2) + 7.02/1000000*Math.pow(x,3) - 2.53/100000000*Math.pow(x,4);
	}
	
	public void createNextLaserFrame() {	
		if (animationTick == null) { // Animation over
			return;
		}
		else if (animationTick > 130) { // Animation over
			red.setScaleY(0);
			animationTick = null;
			return;
		}
		if (isHorizontal) {
			if (animationTick < 7) {
				white.setScaleY(whiteExponential(animationTick));
				orange.setScaleY(orangeExponential(animationTick));
				red.setScaleY(redExponential(animationTick));
			}
			else if (animationTick < 30) {
				white.setScaleY(whiteSine(animationTick));
				orange.setScaleY(orangeSine(animationTick));
				red.setScaleY(redSine(animationTick));
				
			}
			else if (animationTick < 60) {
				white.setScaleY(whitePolynomial(animationTick));
				orange.setScaleY(orangePolynomial(animationTick));
				red.setScaleY(redPolynomial(animationTick));
			}
			else if (animationTick < 70) {
				white.setScaleY(0);
				orange.setScaleY(orangePolynomial(animationTick));
				red.setScaleY(redPolynomial(animationTick));
			}
			else {
				white.setScaleY(0);
				orange.setScaleY(0);
				red.setScaleY(redPolynomial(animationTick));
			}
		}
		else {
			if (animationTick < 7) {
				white.setScaleX(whiteExponential(animationTick));
				orange.setScaleX(orangeExponential(animationTick));
				red.setScaleX(redExponential(animationTick));
			}
			else if (animationTick < 30) {
				white.setScaleX(0.8*Math.sin(animationTick/9.0 - 0.1)+0.3);
				orange.setScaleX(0.8*Math.sin(animationTick/10.0)+0.35);
				red.setScaleX(Math.sin(animationTick/10.0 - 0.1)+0.4);
				
			}
			else if (animationTick < 60) {
				white.setScaleX(whitePolynomial(animationTick));
				orange.setScaleX(orangePolynomial(animationTick));
				red.setScaleX(redPolynomial(animationTick));
			}
			else if (animationTick < 70) {
				white.setScaleX(0);
				orange.setScaleX(orangePolynomial(animationTick));
				red.setScaleX(redPolynomial(animationTick));
			}
			else {
				white.setScaleX(0);
				orange.setScaleX(0);
				red.setScaleX(redPolynomial(animationTick));
			}
		}
		animationTick++;
		lasers.toFront();
	}
	public Group getLaserGroup() {
		return lasers;
	}
	
	public Integer getAnimationTick() {
		return animationTick;
	}
}
