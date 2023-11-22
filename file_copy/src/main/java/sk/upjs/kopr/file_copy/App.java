package sk.upjs.kopr.file_copy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
	
	
	@Override
	public void start(Stage stage) throws Exception{
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
		WindowControler controller = new WindowControler();
		fxmlLoader.setController(controller);
		Parent parent = fxmlLoader.load();
		Scene scene = new Scene(parent);
		stage.setScene(scene);
		stage.setTitle("Kopr_project");
		stage.show();
				
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
