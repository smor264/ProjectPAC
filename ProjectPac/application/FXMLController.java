package application;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class FXMLController {

	@FXML
	private Text currentScoreText;

	@FXML
	private void initialize() {
		currentScoreText.setText("--");
	}



}
