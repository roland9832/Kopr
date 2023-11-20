package sk.upjs.kopr.file_copy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import sk.upjs.kopr.file_copy.client.Client;

public class WindowControler {

	private int numOfTCP;
	private Client client;
	private DialogPane dialog;

	@FXML
	private Button continue_button;

	@FXML
	private Label copy_lable;

	@FXML
	private Label filePath_lable;

	@FXML
	private TextField numOfTCP_textfield;

	@FXML
	private Label progress_lable;

	@FXML
	private Button start_button;

	@FXML
	private Button stop_button;
	
	
	
	

	@FXML
	void onContinueClick(ActionEvent event) {
		if (numOfTCP_textfield.getText().isBlank()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText("Nebol vyplneny pocet TCP");
			dialog = alert.getDialogPane();
			dialog.getStyleClass().add("dialog");
			alert.show();
			return;
		}else {
			try {
				numOfTCP = Integer.parseInt(numOfTCP_textfield.getText());
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("Zadajte ciselnu hodnotu");
				dialog = alert.getDialogPane();
				dialog.getStyleClass().add("dialog");
				alert.show();
				return;
			}
		}
		
	}
	
	@FXML
	void onStartClick(ActionEvent event) {

	}

	@FXML
	void onStopClick(ActionEvent event) {

	}

}
