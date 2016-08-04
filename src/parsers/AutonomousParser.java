package parsers;

public class AutonomousParser {
    public static double[] parse(String input){
        input = input.substring(1,input.length()-1);
        String[] splittedInput = input.split(", ");
        double[] parsedInput = new double[splittedInput.length];
        for(int index = 0; index < splittedInput.length; index++){
            parsedInput[index] = Double.parseDouble(splittedInput[index]);
        }
        return parsedInput;
    }
}
