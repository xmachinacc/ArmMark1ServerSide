package parsers;

import java.util.ArrayList;
import java.util.List;

import ballutils.BallCollection;
import ballutils.BallElement;

public class BallParser {

    public static BallCollection parse(String input, int imageWidth, int imageHeight) {
        List<BallElement> output = new ArrayList<>();
        String[] ballStrings = parseIndividualBallStrings(input);
        for (String ballString : ballStrings) {
            int[] locationInfo = parseBallInfo(ballString);

            int x = locationInfo[0];
            int y = locationInfo[1];
            int radius = locationInfo[2];

            double distance = distance(radius, imageHeight, imageWidth);

            output.add(new BallElement(x, y, radius, distance, imageWidth, imageHeight));
        }
        return new BallCollection(output);
    }
    
    public static double distance(int radius, int imageWidth, int imageHeight){
        double ballArea = Math.PI * radius * radius;
        double totalArea = imageWidth * imageHeight;

        double percentTakenByBall = 100 * ballArea / totalArea;
        double distance = 29.83581617 * Math.exp(-.05695178779 * percentTakenByBall); // change
        
        return distance;
    }

    private static String[] parseIndividualBallStrings(String input) {
        return input.split(";");

    }

    private static int[] parseBallInfo(String input) {
        input = input.substring(1, input.length() - 1);
        String[] splittedInput = input.split(", ");
        int[] parsedInput = new int[3];
        for (int index = 0; index < 3; index++) {
            parsedInput[index] = Integer.parseInt(splittedInput[index]);
        }
        return parsedInput;
    }
}

