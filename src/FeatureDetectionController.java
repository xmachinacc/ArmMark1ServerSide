
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import ballutils.BallCollection;
import ballutils.BallCollectionContainer;
import ballutils.BallElement;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import parsers.BallParser;
import qrutils.QRCollection;
import qrutils.QRCollectionContainer;
import qrutils.QRElement;
import visionservers.ArmVisionServer;
import visionservers.BallExtractionServer;
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

    private static final int DEFAULT_VISION_PORT = 9797;

    private static final int DEFAULT_BARCODE_PORT = 9898;

    private static final int DEFAULT_BALL_PORT = 9696;

    private ArmVisionServer visionServer;

    private BarcodeExtractionServer barcodeServer;

    private BallExtractionServer ballServer;

    private ArmServerAutonomous autonomyServer;

    double[] averageBallColor = new double[3];

    // A threadsafe datatype that can be shared among threads (such as this
    // thread and the autonomyServer thread so that the arm can decide where to
    // go to grab a barcode).
    private QRCollectionContainer barcodesContainer = new QRCollectionContainer(new QRCollection(new ArrayList<>()));

    // A threadsafe datatype that can be shared among threads (such as this
    // thread and the autonomyServer thread so that the arm can decide where to
    // go to grab a ball).
    private BallCollectionContainer ballsContainer = new BallCollectionContainer(new BallCollection(new ArrayList<>()));

    // FXML buttons
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;

    // a flag to change the button behavior
    private final BooleanContainer cameraActive = new BooleanContainer(false);

    private final BooleanContainer shouldDetectElements = new BooleanContainer(true);

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

        averageBallColor[0] = 0;
        averageBallColor[1] = 0;
        averageBallColor[2] = 0;

        visionServer = new ArmVisionServer(DEFAULT_VISION_PORT);

        barcodeServer = new BarcodeExtractionServer(DEFAULT_BARCODE_PORT);

        ballServer = new BallExtractionServer(DEFAULT_BALL_PORT);

        autonomyServer = new ArmServerAutonomous(DEFAULT_AUTONOMY_PORT, shouldDetectElements);

        this.cameraButton.setDisable(false);

        frameGrabber = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    while (true) {
                        if (cameraActive.getBoolean()) {
                            byte[] bytesFlipped = visionServer.getImageAsByteArray();
                            if (bytesFlipped.length != 0) {
                                Mat imageAsMatFlipped = Imgcodecs.imdecode(new MatOfByte(bytesFlipped),
                                        Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
                                Mat imageAsMat = new Mat();
                                Core.flip(imageAsMatFlipped, imageAsMat, -1);
                                MatOfByte byteMat = new MatOfByte();
                                Imgcodecs.imencode(".jpg", imageAsMat, byteMat);
                                byte[] bytes = byteMat.toArray();

                                if (shouldDetectElements.getBoolean()) {
                                    // QRCollection barcodes =
                                    // barcodeServer.extractBarcodes(bytes,
                                    // imageAsMat.width(),
                                    // imageAsMat.width());
                                    BallCollection balls = ballServer.extractBalls(bytes, imageAsMat.width(),
                                            imageAsMat.width());

                                    // barcodesContainer.update(barcodes);
                                    ballsContainer.update(balls);

                                    // drawRectangleAndLabelAroundBarcodes(imageAsMat);
                                    drawCircleAroundBalls(imageAsMat);
                                } else {
                                    ballsContainer.update(new BallCollection(new ArrayList<>()));
                                    barcodesContainer.update(new QRCollection(new ArrayList<>()));
                                }

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
                        if (cameraActive.getBoolean()) {

                            // localizes the arm and tells it where to move
                            // based on the barcodes identified in the image by
                            // the frameGrabber
                            autonomyServer.handleAutonomous(barcodesContainer.look().getCollection(),
                                    ballsContainer.look().getCollection());

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

        if (!cameraActive.getBoolean()) {

            cameraActive.updateBoolean(true);
            // update the button content
            cameraButton.setText("TERMINATE");

        } else {

            cameraActive.updateBoolean(false);
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
    private void drawRectangleAndLabelAroundBarcodes(Mat frame) {

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
     * 
     * @param frame
     * 
     *            Takes in frame and draws circles around all circles in the
     *            ballsContainer on the frame (this is a private internal
     *            method, so its not rep exposure for me to refer to
     *            ballsContainer).
     * @throws InterruptedException
     */
    private void drawCircleAroundBalls(Mat frame) throws InterruptedException {

        List<BallElement> balls = ballsContainer.look().getCollection();

        for (BallElement ball : balls) {

            Point drawPoint = new Point();
            drawPoint.x = ball.x();
            drawPoint.y = ball.y();

            Point textPoint = new Point();
            textPoint.x = ball.x() - ball.radius();
            textPoint.y = ball.y() - ball.radius();

            Point topLeft = new Point();
            topLeft.x = ball.x() - Math.sqrt(2) * .5 * ball.radius();
            topLeft.y = ball.y() - Math.sqrt(2) * .5 * ball.radius();

            Point bottomRight = new Point();
            bottomRight.x = ball.x() + Math.sqrt(2) * .5 * ball.radius();
            bottomRight.y = ball.y() + Math.sqrt(2) * .5 * ball.radius();

            if (textPoint.x > 0 && textPoint.y > 0 && bottomRight.x < frame.cols() && bottomRight.y < frame.rows()) {

                averageBallColor = regionalAverage(frame, (int) topLeft.y, (int) bottomRight.y, (int) topLeft.x,
                        (int) bottomRight.x);

                Imgproc.circle(frame, drawPoint, ball.radius(), new Scalar(59, 59, 187));

                Imgproc.rectangle(frame, topLeft, bottomRight, new Scalar(59, 59, 187), 1);

                Imgproc.putText(frame, "BALL", textPoint, 2, .5, new Scalar(0, 59, 187));
            }
        }
        
        if (balls.isEmpty()) {

            int[] bestFitData = searchForBestFitRegion(frame, averageBallColor);
            
            if(bestFitData != null){
            BallElement ball = new BallElement(bestFitData[0], bestFitData[1], bestFitData[2], 
                    BallParser.distance(bestFitData[2], frame.cols(), frame.rows()), frame.cols(), frame.rows());
            
            List<BallElement> ballList = new ArrayList<>();
            
            ballList.add(ball);
            
            ballsContainer.update(new BallCollection(ballList));
            System.out.println(ballsContainer);
            }

        }


    }

    private double[] regionalAverage(Mat frame, int dtop, int dbottom, int dleft, int dright) {
        Mat ballFrame = frame.submat(dtop, dbottom, dleft, dright);

        int row = ballFrame.rows();
        int col = ballFrame.cols();

        int numberOfPixels = row * col;

        double[] averageColor = new double[3];

        while (row > 0) {
            row--;
            col = ballFrame.cols();
            while (col > 0) {
                col--;
                double[] color = ballFrame.get(row, col);
                averageColor[0] += color[0];
                averageColor[1] += color[1];
                averageColor[2] += color[2];
            }
        }
        averageColor[0] /= numberOfPixels;
        averageColor[1] /= numberOfPixels;
        averageColor[2] /= numberOfPixels;

        return averageColor;

    }

    private int[] searchForBestFitRegion(Mat frame, double[] averageColorToMatch) throws InterruptedException {

        int[] returnData = null;

        Point closestPoint = new Point();
        closestPoint.x = 0;
        closestPoint.y = 0;
        double[] colorData = frame.get(0, 0);
        double bestDifference = Math.abs(averageBallColor[0] - colorData[0])
                + Math.abs(averageBallColor[1] - colorData[1]) + Math.abs(averageBallColor[2] - colorData[2]);

        int row = frame.rows();
        int col = frame.cols();

        while (row > 0) {
            row--;
            col = frame.cols();
            while (col > 0) {
                col--;
                double[] color = frame.get(row, col);
                double difference = Math.abs(averageBallColor[0] - color[0]) + Math.abs(averageBallColor[1] - color[1])
                        + Math.abs(averageBallColor[2] - color[2]);
                if (difference < bestDifference) {
                    bestDifference = difference;
                    closestPoint.x = col;
                    closestPoint.y = row;
                }
            }
        }
        
        if(bestDifference < 5){
        
            Mat frameToBeModified = frame.clone();
            
            Core.inRange(frame, new Scalar(averageColorToMatch[0]-60, averageColorToMatch[1]-60, averageColorToMatch[2]-60), 
                    new Scalar(averageColorToMatch[0]+60, averageColorToMatch[1]+60, averageColorToMatch[2]+60), frameToBeModified);
            
            List<MatOfPoint> contours = new ArrayList<>();
            
            List<MatOfPoint> contourToKeep = new ArrayList<>();
            
            Imgproc.findContours(frameToBeModified, contours, new Mat(), 1, 1);
            
            int areaOfContourToKeep = 0;
            
            for(MatOfPoint contour : contours){
                boolean shouldAdd = false;
                areaOfContourToKeep = 0;
                for(Point point : contour.toArray()){
                    
                    //if the point we think is on the ball is in a contour, then that contour probably represents the ball
                    if(Math.abs(point.x - closestPoint.x) < 2 && Math.abs(point.y - closestPoint.y) < 2){
                        shouldAdd = true;
                    }
                    areaOfContourToKeep++;
                    
                }
                if(shouldAdd){
                    contourToKeep.add(contour);
                    break;
                }
            }
            Imgproc.drawContours(frame, contourToKeep, -1, new Scalar(255,0,0));
            
            int suspectedCircleRadius = (int)Math.sqrt(5*areaOfContourToKeep/Math.PI);
            
            Imgproc.circle(frame, closestPoint, suspectedCircleRadius, new Scalar(59, 59, 187));
            
            returnData = new int[3];
            returnData[0] = (int)closestPoint.x;
            returnData[1] = (int)closestPoint.y;
            returnData[0] = suspectedCircleRadius;
            
        }
        
        //originalFrame.setImage(mat2Image(frameToBeModified));

        return returnData;
    }
    
    /*
    private int blobAreaFromBlobSeed(Point seed){
        
        BlockingQueue
        
        while 
    }
    
    private List<Point> successors(Mat frame, Point parent, Point seed){
        List<Point> children = new ArrayList<>();
        
        List<Point> possibleChildren = new ArrayList<>();
        possibleChildren.add(new Point(parent.x++,parent.y++));
        possibleChildren.add(new Point(parent.x++,parent.y--));
        possibleChildren.add(new Point(parent.x--,parent.y++));
        possibleChildren.add(new Point(parent.x--,parent.y--));
        possibleChildren.add(new Point(parent.x++,parent.y));
        possibleChildren.add(new Point(parent.x,parent.y++));
        possibleChildren.add(new Point(parent.x--,parent.y));
        possibleChildren.add(new Point(parent.x,parent.y--));
        
        for(Point possibleChild : possibleChildren){
            if(possibleChild.x > 0 && possibleChild.x < frame.cols() && possibleChild.y > 0 && possibleChild.y < frame.rows() && 
                    colorDistance(frame.get((int)possibleChild.y, (int)possibleChild.x), frame.get((int)seed.y, (int)seed.x)) < 100){
                children.add(possibleChild);
            }
        }
        
        return children;
        
    }
    
    */
    
    private double colorDistance(double[] color1, double[] color2){
        return Math.abs(color1[0] - color2[0]) + Math.abs(color1[1] - color2[1]) + Math.abs(color1[2] - color2[2]);
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
