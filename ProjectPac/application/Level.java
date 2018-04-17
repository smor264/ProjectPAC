package application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javafx.scene.paint.Color;

public class Level {
	private int[][] positionArray = new int[Main.levelHeight][Main.levelWidth];

	private Color background;
	private Color wall;
	private String levelName;
/*
	public enum Difficulty {
		easy, // 56% chance of dumb AI, 35% moderate, 7% chance smart, 1% chance perfect
		medium, // 26% dumb, 43% moderate, 26% smart, 5% perfect
		hard, //5% dumb, 26% moderate, 43% smart, 26% perfect
	}
	Difficulty difficulty;*/

	public Level(String levelName) {

		this.levelName = levelName;
		String filename = "res/levels/" + levelName + ".txt";
		String line = null;

		try {
			FileReader fileReader = new FileReader(filename);
			BufferedReader buffered = new BufferedReader(fileReader);

			int yPos = 0;
			int extraLines = 0;
			while  ((line = buffered.readLine()) != null) {
				if (yPos < Main.levelHeight) {
					String[] characters = line.replace("{", "").replace("}", "").split(",");
					int xPos = 0;

					for (int i = 0; i < characters.length; i++) {
						positionArray[yPos][xPos] = Integer.parseInt(characters[i]);
						xPos++;
					}
					yPos++;
				}
				else {
					extraLines++;
					String r = line.substring(0, 2); // Hex string
					String g = line.substring(2, 4);
					String b = line.substring(4, 6);

					switch(extraLines) {
						case 1:{
							background = Color.rgb(Integer.parseInt(r,16), Integer.parseInt(g,16), Integer.parseInt(b,16));
							break;
						}
						case 2:{
							wall = Color.rgb(Integer.parseInt(r, 16), Integer.parseInt(g, 16), Integer.parseInt(b, 16));
							break;
						}
						default: return;
					}

				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}


	public Level(int[][] array) {
		positionArray = array;
	}

	public int [][] getArray() {
		return positionArray;
	}

	public Color getBackground() {
		return background;
	}
	public Color getWallColor() {
		return wall;
	}
	public String getLevelName() {
		return levelName;
	}
}
