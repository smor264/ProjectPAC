package application;

public class Level {
	private int[][] positionArray = new int[Main.levelWidth][Main.levelHeight];
	
	public Level() {
		positionArray = new int[][] {{1,1,1,1,1,1,1,1,1,1},
									 {1,0,0,0,0,0,0,0,0,1},
									 {1,0,1,1,1,0,1,1,0,1},
								     {1,0,0,2,0,0,1,1,0,1},
								     {1,0,0,1,1,0,1,1,0,1},
								     {1,0,0,1,1,0,0,0,0,1},
								     {1,0,0,0,1,0,1,1,0,1},
								     {1,0,1,0,0,0,3,0,0,1},
								     {1,0,0,1,1,1,0,0,0,1},
								     {1,1,1,1,1,1,1,1,1,1}};
	}
	public Level(int[][] array) {
		positionArray = array;
	}
	public int [][] getArray() {
		return positionArray;
	}
}
