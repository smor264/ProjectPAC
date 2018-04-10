package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
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
	private Text currentCharacter;

	@FXML
	private StackPane pacmanSelect;

	@FXML
	private StackPane msPacmanSelect;

	@FXML
	private StackPane packidSelect;

	@FXML
	private StackPane robotSelect;

	@FXML
	private StackPane snacSelect;

	@FXML
	private StackPane glitchSelect;


	@FXML
	private void initialize() {
		currentScoreText.setText("--");
	}



}
