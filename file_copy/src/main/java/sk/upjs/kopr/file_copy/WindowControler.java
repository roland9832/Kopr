package sk.upjs.kopr.file_copy;

import java.util.concurrent.CountDownLatch;

import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import sk.upjs.kopr.file_copy.client.Client;
import sk.upjs.kopr.file_copy.server.Server;

public class WindowControler {

	private int numOfTCP;
	private Client client;
	private DialogPane dialog;
	private CountDownLatch latch;

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
	void initialize() {
		copy_lable.setText(Server.FILE_TO_SHARE);
		filePath_lable.setText(Client.FINAL_DESTINATION);
		stop_button.setDisable(true);
	}
	
	

	
	
	@FXML
	void onStartClick(ActionEvent event) {
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
		
		numOfTCP_textfield.setDisable(true);
		latch = new CountDownLatch(numOfTCP);
		stop_button.setDisable(false);
		start_button.setDisable(true);
		client = new Client(numOfTCP, latch);
		client.start();
	}

	@FXML
	void onStopClick(ActionEvent event) {
		client.cancel();
		start_button.setDisable(false);
		
	}

}
