
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import qrutils.QRCollection;
import qrutils.QRCollectionContainer;
import qrutils.QRElement;
import visionservers.ArmVisionServer;
import visionservers.BarcodeExtractionServer;

//TODO: specs, checkrep, safety arguments.
public class FeatureDetectionController {
    private static final int DEFAULT_PORT = 4444;

    /** Default vision server port. */
    private static final int DEFAULT_VISION_PORT = 9797;

    /** Default barcode server port. */
    private static final int DEFAULT_BARCODE_PORT = 9898;

    private ArmVisionServer vision;

    private BarcodeExtractionServer barcodeServer;
    
    private ArmServerAutonomous autonomy;

    // a string containing information about barcodes in the image. Gets
    // reassigned often to keep up with the updating image.
    private QRCollectionContainer barcodesContainer = new QRCollectionContainer(new QRCollection(new ArrayList<>()));

    // FXML buttons
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;

    // a flag to change the button behavior
    private boolean cameraActive = false;

    private Stage stage;

    private Thread frameGrabber;
    
    private Thread autonomyUpdater;

    // private VideoCapture capture = new VideoCapture(0);
    /**
     * Init the controller, at start time
     * 
     * @throws IOException
     */
    public void init(Stage primaryStage) throws IOException {

        this.stage = primaryStage;

        vision = new ArmVisionServer(DEFAULT_VISION_PORT);

        barcodeServer = new BarcodeExtractionServer(DEFAULT_BARCODE_PORT);
        
        autonomy = new ArmServerAutonomous(DEFAULT_PORT);

        this.cameraButton.setDisable(false);

        frameGrabber = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    // for testing with this computer's camera
                    /*
                     * Mat localImageAsMat = new Mat();
                     * capture.read(localImageAsMat);
                     * 
                     * MatOfByte matByte = new MatOfByte();
                     * Imgcodecs.imencode(".jpg",localImageAsMat, matByte);
                     * 
                     * byte[] bytes = matByte.toArray();
                     */
                    while (true) {
                        Thread.sleep(1);
                        if (cameraActive) {
                            byte[] bytesFlipped = vision.getImageAsByteArray();
                            if(bytesFlipped.length != 0){
                            Mat imageAsMatFlipped = Imgcodecs.imdecode(new MatOfByte(bytesFlipped),
                                    Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
                            Mat imageAsMat = new Mat();
                            Core.flip(imageAsMatFlipped, imageAsMat, -1);
                            MatOfByte byteMat = new MatOfByte();
                            Imgcodecs.imencode(".jpg", imageAsMat, byteMat);
                            byte[] bytes = byteMat.toArray();
                            QRCollection barcodes = barcodeServer.extractBarcodes(bytes, imageAsMat.width(),
                                    imageAsMat.width());
                            
                            barcodesContainer.update(barcodes);
                            //autonomy.handleAutonomous(barcodes.getCollection());
                            
                            drawRectangleAndLabelAroundBarcode(imageAsMat);

                            Image imageToShow = mat2Image(imageAsMat);

                            originalFrame.setImage(imageToShow);
                            }else{
                                System.out.println("empty");
                            }
                        } else {
                            if (originalFrame.getImage() != null) {
                                originalFrame.setImage(null);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // continue. TODO: make this better
                }
            }
        });

        frameGrabber.setDaemon(true);
        frameGrabber.start();
        
        autonomyUpdater = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    while (true) {
                        Thread.sleep(1);
                        if (cameraActive) {
                            
                            autonomy.handleAutonomous(barcodesContainer.look().getCollection());
                            
                        } else {
                            //continue waiting
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // continue. TODO: make this better
                }
            }
        });
        
        autonomyUpdater.setDaemon(true);
        autonomyUpdater.start();

        Runtime runtime = Runtime.getRuntime();
        
        //TODO: change this
        runtime.exec("python BarcodeFinder.py");

        stage.setOnCloseRequest(event -> {
            try {
                //TODO: change once the barcode is compiled into a unique .exe
                runtime.exec("taskkill /F /IM python.exe");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

    }

    /**
     * The action triggered by pushing the button on the GUI
     * 
     * @throws IOException
     */
    @FXML
    protected void startCamera() throws IOException {
        // set a fixed width for the frame
        originalFrame.setFitWidth(640);
        // preserve image ratio
        originalFrame.setPreserveRatio(true);

        if (!cameraActive) {

            cameraActive = true;
            // update the button content
            cameraButton.setText("TERMINATE");

        } else {

            cameraActive = false;
            // update again the button content
            cameraButton.setText("INITIATE");
        }
    }

    private void drawRectangleAndLabelAroundBarcode(Mat frame) {

        List<QRElement> barcodes = barcodesContainer.look().getCollection();

        for (QRElement barcode : barcodes) {
            int topLeftX = barcode.x() - barcode.width() / 2;
            int topLeftY = barcode.y() - barcode.height() / 2;

            int bottomRightX = barcode.x() + barcode.width() / 2;
            int bottomRightY = barcode.y() + barcode.height() / 2;

            Point topLeft = new Point();
            topLeft.x = topLeftX;
            topLeft.y = topLeftY;

            Point bottomRight = new Point();
            bottomRight.x = bottomRightX;
            bottomRight.y = bottomRightY;

            Point textStart = new Point();
            textStart.x = topLeft.x;
            textStart.y = topLeft.y - 2;

            Imgproc.rectangle(frame, topLeft, bottomRight, new Scalar(59, 59, 187), 1);

            Imgproc.putText(frame, barcode.title().toString(), textStart, 2, .5, new Scalar(0, 59, 187));
        }

    }

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     * 
     * @param frame
     *            the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".jpg", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

}
