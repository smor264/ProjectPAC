package application;

import java.util.LinkedList;
import java.util.Queue;

import javafx.scene.shape.Shape;

/**snakePiece are instances of Snac The snake's body as he grows longer.
 * They record the movements of their parent and perform them later*/
public class SnakePiece extends Character {
	int positionNumber;
	
	Queue<Main.Direction> nextMoves;
	
	public SnakePiece(Shape model, int speed, Character parent) {
		super(model, speed);
		nextMoves = new LinkedList<Main.Direction>();
	}
	public Main.Direction dequeueMove() {
		if (nextMoves.size() != Main.gridSquareSize/speed) {
			return null;
		}
		else {
			return nextMoves.remove();
		}
	}
	public void enqueueMove(Main.Direction direction) {
		if (direction == null) {
			;
		}
		else {
			nextMoves.add(direction);
		}
	}
	public void removeMoves(int n) {
		if (nextMoves.size() < n) {
			nextMoves.clear();
		}
		else {
			for (int i = 0; i < n; i++) {
				nextMoves.remove();
			}
		}
	}

}
