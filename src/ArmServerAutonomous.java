
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import localizationutils.ArmPositionSearch;
import localizationutils.LocalizeArm;
import localizationutils.ObjectPosition;
import motionutils.Motion;
import parsers.AutonomousParser;
import qrutils.QRElement;

public class ArmServerAutonomous {

    int timer = 0;
    
    Set<QRElement> seenAndReachableBarcodes = new HashSet<>();

    private final ServerSocket serverSocket;


    /**
     * TODO: spec!!
     * 
     * @param port
     * 
     * @throws IOException
     */
    public ArmServerAutonomous(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }
    
    public void handleAutonomous(List<QRElement> barcodes) throws IOException, InterruptedException{
        Socket socket = serverSocket.accept();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        String line = in.readLine();
        
        handleStandard(line, out, barcodes);
    }
    
    private void handleStandard(String line, PrintWriter out, List<QRElement> barcodes) throws InterruptedException {
        // System.out.println("Recieved " + line);
        double[] data = AutonomousParser.parse(line);
        String command = "";
        // System.out.println("Distance Reading: " + distanceReading);
        //timer++;
        timer = 180;
        if (timer == 180) {
            //timer = 0;
        }

        int[] wristDegreeChange = new int[2];
        if ((wristDegreeChange = lookToDesired(barcodes)) != null) {
            data[4] += wristDegreeChange[0];
            data[5] += wristDegreeChange[1];

            int[] jointPositions = new int[5];
            for (int index = 0; index < 5; index++) {
                jointPositions[index] = (int) data[index];
            }
            double[] objectPosition = ObjectPosition.localizeObjectCylindricalCoordinates(jointPositions,
                    wristDegreeChange[2]);
            System.out.println(
                    "Object Position " + Arrays.asList(objectPosition[0], objectPosition[1], objectPosition[2]));
            int[] initialArmJointPositions = new int[3];
            for (int index = 0; index < 3; index++) {
                initialArmJointPositions[index] = (int) data[index];
            }
            int[] givenHandJointPositions = new int[2];
            givenHandJointPositions[0] = 157;
            givenHandJointPositions[1] = 90;
            /*
             * for(int index = 3; index < 5; index++){
             * givenHandJointPositions[index-3] = (int) data[index]; }
             */
            int[] possiblePathToObject = ArmPositionSearch.gripperPositionSearch(objectPosition,
                    initialArmJointPositions, givenHandJointPositions);

            if (possiblePathToObject != null) {
                command = Motion.grab(possiblePathToObject, 1.5) + "," + Motion.throwObject(180, 1.5) + "," + Motion.restPosition();
                //command = Arrays.asList(possiblePathToObject[0], possiblePathToObject[1], possiblePathToObject[2],
                        //possiblePathToObject[3], possiblePathToObject[4], 90, 20).toString();
            }
        } else {
            //command = visionSweep(data);
        }

        // System.out.println("Sending: " + command);
        out.println(command);

    }

    private String visionSweep(double[] data) {
        data[0] = timer;
        int[] dataInt = new int[data.length];
        for (int index = 0; index < data.length; index++) {
            dataInt[index] = (int) data[index];
        }
        return Arrays.asList(dataInt[0], dataInt[1], dataInt[2], dataInt[3], dataInt[4], dataInt[5], dataInt[6])
                .toString();
    }

    private int[] lookToDesired(List<QRElement> barcodes) {
        if (barcodes.size() == 0) {
            return null;
        }

        double wristLengthCm = 10;

        QRElement barcodeToLookAt = barcodes.get(0);

        double distanceToBarcodeCm = barcodeToLookAt.distance();

        double barcodeSideCm = 3.81;
        int barcodeSideAveragePixel = (barcodeToLookAt.width() + barcodeToLookAt.height()) / 2;
        double pixelToCm = barcodeSideCm / barcodeSideAveragePixel;

        int pixelOffsetFromCenteredX = Math.abs(barcodeToLookAt.imageWidth() / 2 - barcodeToLookAt.x());
        int pixelOffsetFromCenteredY = Math.abs(barcodeToLookAt.imageHeight() / 2 - barcodeToLookAt.y());

        double cmOffsetFromCenteredX = pixelOffsetFromCenteredX * pixelToCm;
        double cmOffsetFromCenteredY = pixelOffsetFromCenteredY * pixelToCm;
        System.out.println(cmOffsetFromCenteredX + "," + cmOffsetFromCenteredY);

        int[] degreesToChange = new int[3];

        degreesToChange[0] = (int) Math.toDegrees(Math.asin(cmOffsetFromCenteredX
                / (Math.sqrt(distanceToBarcodeCm * distanceToBarcodeCm + cmOffsetFromCenteredX * cmOffsetFromCenteredX)
                        + wristLengthCm)));
        degreesToChange[1] = (int) Math.toDegrees(Math.asin(cmOffsetFromCenteredY
                / (Math.sqrt(distanceToBarcodeCm * distanceToBarcodeCm + cmOffsetFromCenteredY * cmOffsetFromCenteredY)
                        + wristLengthCm)));
        degreesToChange[2] = (int) distanceToBarcodeCm;
        System.out.println(degreesToChange[0] + "," + degreesToChange[1]);
        System.out.println(distanceToBarcodeCm);
        if (barcodeToLookAt.x() < barcodeToLookAt.imageWidth() / 2) {
            degreesToChange[0] = -degreesToChange[0];
        }

        if (barcodeToLookAt.y() < barcodeToLookAt.imageHeight() / 2) {
            degreesToChange[1] = -degreesToChange[1];
        }
        
        return degreesToChange;

    }


}
