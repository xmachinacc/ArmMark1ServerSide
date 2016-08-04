
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
import qrutils.QRCollection;
import qrutils.QRCollectionContainer;
import qrutils.QRElement;
import visionservers.ArmVisionServer;
import visionservers.BarcodeExtractionServer;

/**
 * 
 * @author trist
 *
 *         This class represents a controller for the GUI for the ARM MARK 1
 *         that ties many elements of its API together to create a fully
 *         autonomous robot that detects QR codes real time, can map out a way
 *         to grab them in milliseconds and then can decide what to do with them
 *         based off of what they are.
 */
public class FeatureDetectionController {

    /*
     * Thread safety arguments:
     * 
     * The only objects shared among threads are threadsafe with the exception
     * of ImageView. ImageView is only ever mutated by one thread.
     * 
     * Safety from rep exposure:
     * 
     * All instance variables are private. The return type of all public methods
     * is void.
     * 
     * Abstraction function:
     * 
     * This class represents a controller for a GUI that ties many elements of
     * the API together to drive the ARM MARK 1 to carrying out autonomous
     * tasks.
     * 
     * Rep invariants:
     * 
     * None decided on for now that aren't statically checked.
     */

    private static final int DEFAULT_AUTONOMY_PORT = 4444;

    private static final int DEFAULT_VISION_PORT = 9595;

    private static final int DEFAULT_BARCODE_PORT = 9898;

    private ArmVisionServer visionServer;

    private BarcodeExtractionServer barcodeServer;

    private ArmServerAutonomous autonomyServer;

    // A threadsafe datatype that can be shared among threads (such as this
    // thread and the autonomyServer thread so that the arm can decide where to
    // go to grab a barcode).
    private QRCollectionContainer barcodesContainer = new QRCollectionContainer(new QRCollection(new ArrayList<>()));

    // FXML buttons
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;

    // a flag to change the button behavior
    private boolean cameraActive = false;

    private Thread frameGrabber;

    private Thread autonomyUpdater;

    /**
     * Initializes the controller for a GUI that utilizes various Arm Mark 1 API
     * elements to identify and grab QR codes and then throw them.
     * 
     * Specifically, this method starts all of the servers that connect to the
     * client programs on the arm, all wrapped into one program to take
     * advantage of as many cores as possible.
     */
    public void init() throws IOException {

        visionServer = new ArmVisionServer(DEFAULT_VISION_PORT);

        barcodeServer = new BarcodeExtractionServer(DEFAULT_BARCODE_PORT);

        autonomyServer = new ArmServerAutonomous(DEFAULT_AUTONOMY_PORT);

        this.cameraButton.setDisable(false);

        frameGrabber = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    while (true) {
                        // cap framerate
                        Thread.sleep(1);
                        if (cameraActive) {
                            byte[] bytesFlipped = visionServer.getImageAsByteArray();
                            if (bytesFlipped.length != 0) {
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

                                drawRectangleAndLabelAroundBarcode(imageAsMat);

                                Image imageToShow = mat2Image(imageAsMat);

                                originalFrame.setImage(imageToShow);
                            }
                        } else {
                            if (originalFrame.getImage() != null) {
                                originalFrame.setImage(null);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
                        // cap response time
                        Thread.sleep(1);
                        if (cameraActive) {

                            // localizes the arm and tells it where to move
                            // based on the barcodes identified in the image by
                            // the frameGrabber
                            autonomyServer.handleAutonomous(barcodesContainer.look().getCollection());

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        autonomyUpdater.setDaemon(true);
        autonomyUpdater.start();

    }

    /**
     * The action triggered by pushing the button on the GUI
     * 
     * If clicked initially, it will turn on wireless video streaming and
     * autonomous actions from the robot, if clicked again it will turn this
     * off.
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

    /**
     * 
     * @param frame
     * 
     *            Takes in frame and draws rectangles with width, length and
     *            center points matching the width, length and center points of
     *            all barcodes in the barcodesContainer on the frame (this is a
     *            private internal method, so its not rep exposure for me to
     *            refer to barcodesContainer).
     */
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
