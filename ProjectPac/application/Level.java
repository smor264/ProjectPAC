package application;

public class Level {
	private int[][] positionArray = new int[Main.levelWidth][Main.levelHeight];
	
	public enum Difficulty {
		easy, // 56% chance of dumb AI, 35% moderate, 7% chance smart, 1% chance perfect
		medium, // 26% dumb, 43% moderate, 26% smart, 5% perfect
		hard, //5% dumb, 26% moderate, 43% smart, 26% perfect
	}
	Difficulty difficulty;
	
	public Level() {
		/*positionArray = new int[][] {{1,1,1,1,0,1,1,1,1,1},
									 {1,4,0,0,0,0,0,0,0,1},
									 {1,4,1,1,1,0,1,1,0,1},
								     {1,5,0,2,0,0,1,1,0,1},
								     {1,4,0,1,1,0,1,1,0,1},
								     {0,0,0,1,1,0,0,0,0,0},
								     {1,0,0,0,1,0,1,1,0,1},
								     {1,0,1,0,0,0,0,3,0,1},
								     {1,0,0,1,0,1,0,0,0,1},
								     {1,1,1,1,0,1,1,1,1,1}};
		*/
		difficulty = Difficulty.medium;
		/*positionArray = new int[][] {{1,1,1,1,0,1,1,1,1,1},
									 {1,4,2,4,4,4,4,4,4,1},
									 {1,4,1,1,4,1,1,1,4,1},
									 {1,4,1,1,4,1,1,1,4,1},
									 {1,4,1,1,4,1,1,1,4,1},
									 {0,4,4,4,5,4,4,4,4,0},
									 {1,4,1,1,4,1,1,1,4,1},
									 {1,4,1,1,4,1,1,1,4,1},
									 {1,4,4,4,4,4,4,3,4,1},
									 {1,1,1,1,0,1,1,1,1,1}};*/
		positionArray = new int[][] {{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
									 {1,4,4,4,4,4,4,4,4,4,4,4,4,1,4,4,4,4,4,4,4,4,4,4,4,4,1},
									 {1,4,1,1,1,1,4,1,1,1,1,1,4,1,4,1,1,1,1,1,4,1,1,1,1,4,1},
									 {1,5,1,1,1,1,4,1,1,1,1,1,4,1,4,1,1,1,1,1,4,1,1,1,1,5,1},
									 {1,4,1,1,1,1,4,1,1,1,1,1,4,1,4,1,1,1,1,1,4,1,1,1,1,4,1},
									 {1,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,1},
									 {1,4,1,1,1,1,4,1,4,1,1,1,1,1,1,1,1,1,4,1,4,1,1,1,1,4,1},
									 {1,4,4,4,4,4,4,1,4,4,4,4,4,1,4,4,4,4,4,1,4,4,4,4,4,4,1},
									 {1,1,1,1,1,1,4,1,1,1,1,1,0,1,0,1,1,1,1,1,4,1,1,1,1,1,1},
									 {1,1,1,1,1,1,4,1,0,0,0,0,0,-8,0,0,0,0,0,1,4,1,1,1,1,1,1},
									 {1,1,1,1,1,1,4,1,0,1,1,1,1,0,1,1,1,1,0,1,4,1,1,1,1,1,1},
									 {1,1,1,1,1,1,4,1,0,1,0,0,0,0,0,0,0,1,0,1,4,1,1,1,1,1,1},
									 {0,0,0,0,0,0,4,0,0,1,0,-8*3,0,0,0,0,0,1,0,0,4,0,0,0,0,0,0},
									 {1,1,1,1,1,1,4,1,0,1,0,0,0,0,0,0,0,1,0,1,4,1,1,1,1,1,1},
									 {1,1,1,1,1,1,4,1,0,1,1,1,1,1,1,1,1,1,0,1,4,1,1,1,1,1,1},
									 {1,1,1,1,1,1,4,1,0,0,0,0,0,0,0,0,0,0,0,1,4,1,1,1,1,1,1},
									 {1,1,1,1,1,1,4,1,0,1,1,1,1,1,1,1,1,1,0,1,4,1,1,1,1,1,1},
									 {1,4,4,4,4,4,4,4,4,4,4,4,4,1,4,4,4,4,4,4,4,4,4,4,4,4,1},
									 {1,4,1,1,1,1,4,1,1,1,1,1,4,1,4,1,1,1,1,1,4,1,1,1,1,4,1},
									 {1,5,4,4,4,1,4,4,4,4,4,4,4,2,4,4,4,4,4,4,4,1,4,4,4,5,1},
									 {1,1,1,1,4,1,4,1,4,1,1,1,1,1,1,1,1,1,4,1,4,1,4,1,1,1,1},
									 {1,4,4,4,4,4,4,1,4,4,4,4,4,1,4,4,4,4,4,1,4,4,4,4,4,4,1},
									 {1,4,1,1,1,1,1,1,1,1,1,1,4,1,4,1,1,1,1,1,1,1,1,1,1,4,1},
									 {1,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,1},
									 {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}};
	}
	public Level(int[][] array) {
		positionArray = array;
	}
	public int [][] getArray() {
		return positionArray;
	}
}
