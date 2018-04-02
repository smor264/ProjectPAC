package application;

import javafx.scene.shape.Shape;

public class Player extends Character{
	int Score = 0;

	public Player(Shape model, double speed) {
		super(model, speed);
	}

	public void setScore (int score) {
		Score = score;
	}

	public void modifyScore (int change) {
		if (change < 0) {
			if(change > Score){
				Score = 0;
			}
			else {
			Score -= change;
			}
		}
		else {
			Score += change;
		}
	}

	public int getScore(){
		return Score;
	}

	public String getScoreString(){
		return Integer.toString(Score);
	}

}
