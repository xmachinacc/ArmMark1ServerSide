package parsers;

import java.util.ArrayList;
import java.util.List;

import qrutils.QRCollection;
import qrutils.QRElement;
import qrutils.QRTitle;

public class BarcodeParser {
    
    public static QRCollection parse(String input, int imageWidth, int imageHeight){
        List<QRElement> output = new ArrayList<>();
        String[] barcodeStrings = parseIndividualBarcodeStrings(input);
        for(String barcodeString : barcodeStrings){
            int[] locationInfo = parseFirstPartOfBarcodeInfo(barcodeString);
            QRTitle title = parseLastPartOfBarcodeInfo(barcodeString);
            int x = locationInfo[0];
            int y = locationInfo[1];
            int width = locationInfo[2];
            int height = locationInfo[3];
            int barcodeArea = width*height;
            double totalArea = imageWidth*imageHeight;
            
            double percentTakenByBarcode = 100*barcodeArea/totalArea;
            double distance = 29.83581617*Math.exp(-.05695178779*percentTakenByBarcode);
            
            output.add(new QRElement(x,y,width,height,distance,title, imageWidth, imageHeight));
        }
        return new QRCollection(output);
    }
    
    private static String[] parseIndividualBarcodeStrings(String input){
        return input.split(";");
        
    }
    
    private static int[] parseFirstPartOfBarcodeInfo(String input){
        input = input.substring(1,input.length()-1);
        String[] splittedInput = input.split(", ");
        int[] parsedInput = new int[4];
        for(int index = 0; index < 4; index++){
            parsedInput[index] = Integer.parseInt(splittedInput[index]);
        }
        return parsedInput;
    }
    
    private static QRTitle parseLastPartOfBarcodeInfo(String input){
        input = input.substring(1,input.length()-1);
        String[] splittedInput = input.split(", ");
        String parsedInput = splittedInput[4];
        if(parsedInput.equals("'mug'")){
            return QRTitle.MUG;
        }else if(parsedInput.equals("'cube'")){
            return QRTitle.CUBE;
        }
        else{
        
            return QRTitle.MISSING_TITLE;
        }
    }
}
