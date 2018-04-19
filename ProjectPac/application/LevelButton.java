package application;
import javafx.scene.Node;
import javafx.scene.control.Button;

/**LevelButton is simply a button that can have a Level connected to it.
 * This makes the onAction method much easier to code.*/
public class LevelButton extends Button {
	
	Level connectedLevel;
	
	public LevelButton() {
		// TODO Auto-generated constructor stub
	}

	public LevelButton(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public LevelButton(String arg0, Node arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	public LevelButton(String arg0, Level level) {
		super(arg0);
		this.connectedLevel = level;
	}
	
	public Level getConnectedLevel(){
		return this.connectedLevel;
	}
	public void setConnectedLevel(Level connectedLevel){
		this.connectedLevel = connectedLevel;
	}


}
