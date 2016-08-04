package localizationutils;
import java.util.Arrays;

/**
 * 
 * @author trist
 *
 *This class contains static methods used for the localization in cylindrical coordinates of detected objects using 
 *a distance sensor reading and the position and angle of the distance sensor itself (distance sensor location and angle can be readily 
 *calculated from methods in LocalizeArmExperimental.
 */
public class ObjectPosition {
    
    /**
     * 
     * @param jointPositions the positions of the arm joints: an array with 0th index the base rotation degrees,
 *            the 1st index the bicep degrees, the 2nd index the forearm degrees, the 3rd index the wrist (up/down) degrees and the
 *            4th index the wrist (left/right) degrees. See the drawing for degree position definitions for different joints.
     * @param distanceReading the reading (in cm) from the distance sensor to the object that will be localized.
     * @return the cylindrical coordinates of the object (defined as the same coordinate system that the arm localizes itself in - see 
     *         LocalizeArmExperimental
     */
    public static double[] localizeObjectCylindricalCoordinates(int[] jointPositions, double distanceReading){
        
        double[] cylindricalCoordinatesToDistanceSensor = LocalizeArm.localizeHandElement(jointPositions[0],jointPositions[1],jointPositions[2],jointPositions[3],jointPositions[4],HandElement.DISTANCE_SENSOR);
        //System.out.println("Distance Sensor Position "+Arrays.asList(cylindricalCoordinatesToDistanceSensor[0],cylindricalCoordinatesToDistanceSensor[1],cylindricalCoordinatesToDistanceSensor[2]));
        double[] cylindricalCoordinatesToDescretePointOnSameAxisAsDistanceSensor = LocalizeArm.localizeHandElement(jointPositions[0],jointPositions[1],jointPositions[2],jointPositions[3],jointPositions[4],HandElement.CENTIMETER_BEHIND_DISTANCE_SENSOR);
    
        double[] cartesianToDistanceSensor = LocalizeArm.toCartesian(cylindricalCoordinatesToDistanceSensor);
        double[] cartesianToAxisPoint = LocalizeArm.toCartesian(cylindricalCoordinatesToDescretePointOnSameAxisAsDistanceSensor);
        
        double pointDistance = Math.sqrt((cartesianToAxisPoint[0] - cartesianToDistanceSensor[0])*(cartesianToAxisPoint[0] - cartesianToDistanceSensor[0]) +
                               (cartesianToAxisPoint[1] - cartesianToDistanceSensor[1])*(cartesianToAxisPoint[1] - cartesianToDistanceSensor[1]) +
                               (cartesianToAxisPoint[2] - cartesianToDistanceSensor[2])*(cartesianToAxisPoint[2] - cartesianToDistanceSensor[2]));
        double xYpointDistance = Math.sqrt((cartesianToAxisPoint[0] - cartesianToDistanceSensor[0])*(cartesianToAxisPoint[0] - cartesianToDistanceSensor[0]) +
                                (cartesianToAxisPoint[1] - cartesianToDistanceSensor[1])*(cartesianToAxisPoint[1] - cartesianToDistanceSensor[1]));
        
        //angles here are in radians
        double xZAngleToObject = Math.asin(Math.abs(cartesianToDistanceSensor[2] - cartesianToAxisPoint[2])/pointDistance);
        double xYAngleToObject = Math.asin(Math.abs(cartesianToDistanceSensor[1] - cartesianToAxisPoint[1])/xYpointDistance);
        
        double xYObjectDistance = distanceReading*Math.cos(xZAngleToObject);
        double[] cartesianVectorToObject = new double[3];
        
        if(cartesianToDistanceSensor[0] > cartesianToAxisPoint[0]){
            cartesianVectorToObject[0] = xYObjectDistance*Math.cos(xYAngleToObject); 
        }else{
            cartesianVectorToObject[0] = -xYObjectDistance*Math.cos(xYAngleToObject); 
        }
        
        
        if(cartesianToDistanceSensor[1] > cartesianToAxisPoint[1]){
            cartesianVectorToObject[1] = xYObjectDistance*Math.sin(xYAngleToObject); 
        }else{
            cartesianVectorToObject[1] = -xYObjectDistance*Math.sin(xYAngleToObject); 
        }

        if(cartesianToDistanceSensor[2] > cartesianToAxisPoint[2]){
            cartesianVectorToObject[2] = distanceReading*Math.sin(xZAngleToObject); 
        }else{
            cartesianVectorToObject[2] = -distanceReading*Math.sin(xZAngleToObject);
        }
        
        double[] cartesianToObject = LocalizeArm.addVectors(cartesianVectorToObject,cartesianToDistanceSensor);
        
        //convert object position back to cylindrical finally
        return LocalizeArm.toCylindrical(cartesianToObject);
    }
    
}
