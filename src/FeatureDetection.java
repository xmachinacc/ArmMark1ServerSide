




import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

/**
 * The main class for a JavaFX application. It creates and handle the main
 * window with its resources (style, graphics, etc.).
 * 
 * This application handles a video stream and try to find any possible human
 * face in a frame. It can use the Haar or the LBP classifier.
 * 
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 1.0 (2014-01-10)
 * @since 1.0
 * 
 */
public class FeatureDetection extends Application
{
    
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			// load the FXML resource
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FeatureDetection.fxml"));
			BorderPane root = (BorderPane) loader.load();
			// set a whitesmoke background
			root.setStyle("-fx-background-color: #4B4B4B;");
			// create and style a scene
			Scene scene = new Scene(root, 800, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			primaryStage.getIcons().add(new Image("icon.png"));
			primaryStage.setTitle("ARM MARK 1");
			primaryStage.setScene(scene);
			// show the GUI
			primaryStage.show();
			// init the controller
			FeatureDetectionController controller = loader.getController();
            
			controller.init();
			
			//TODO: change this
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("python BarcodeFinder.py");

            primaryStage.setOnCloseRequest(event -> {
                try {
                    //TODO: change once the barcode is compiled into a unique .exe
                    runtime.exec("taskkill /F /IM python.exe");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args)
	{
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
