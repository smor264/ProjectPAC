package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class FXMLController {

	@FXML
	private Text currentScoreText;

	@FXML 
	private Text currentAbility;
	
	@FXML 
	private Text currentBoost;
	
	@FXML
	private AnchorPane HUDBar;

	@FXML
	private AnchorPane launchScreen;

	@FXML
	private Button playButton;

	
	@FXML
	private void initialize() {
		currentScoreText.setText("--");
	}



}
