package localizationutils;

/**
 * 
 * @author trist
 * 
 * This class includes static methods used to localize various arm components in a cylindrical coordinate system. Some of the specifications
 * refer to a drawing. This drawing is located on my surface pro 4, but will soon be found in a file within this project. TODO: include
 * the drawing in a file.
 *
 */
public class LocalizeArm {
    /**
     * TODO: further limit the degrees that some of the joints can rotate between based on measured freedoms.
     * TODO: include a drawing of all of the joints and their defined starting angles for the specs
     * 
     * @param baseDegrees
     * @param bicepDegrees must be between 0 and 270, inclusive
     * @param forearmDegrees must be between 15 and 180 - forearmDegreeOffset, inclusive - TODO: keep it 90 for now unil bug is found
     * @param upperWristDegrees must yield conglomerateWristAngle (see code for definition of this) that is between 90 and 360, inclusive.
     *        upperWristDegrees must be greater than 90, itself.
     * @param lowerWristDegrees must be between 0 and 180
     * @param handElement The hand element to localize (the center of the gripper, the distance sensor, or a point 1 cm behind the distance sensing axis of the distance sensor)
     * @return an array that represents the cylindrical coordinates (where the origin is the center of the base of the gripper) of the
     *         hand element specified. the 0th index is the radial component, the 1st index is the angle (where 0 degrees is the angle at 
     *         which the baseDegrees is zero and the arm is pointing straight outward), the 2nd index is the z component. Because the wrist of the arm can move left and right,
     *         the degree range should be from a few degrees less than 0 to a few degrees more than 180.
     *         
     *         This method uses certain values such as measured lengths of pieces of the arm and may have a smail amount of error.
     *         
     *         See drawing for more information about where the degrees of different joints are defined to start.
     */
    public static double[] localizeHandElement(int baseDegrees, int bicepDegrees, int forearmDegrees, int upperWristDegrees, int lowerWristDegrees, HandElement handElement){
        final double bicepLength = 14.85;
        final double forearmLength = 16;
        final double upperWristLength = 3.48;
        
        final double middleWristLength;
        final double lowerWristLength;
        
        if(handElement == HandElement.GRIPPER){
            middleWristLength = 3.5;
            lowerWristLength = 10;
        }else if(handElement == HandElement.DISTANCE_SENSOR){
            middleWristLength = 0;
            lowerWristLength = 9;
        }else{
            middleWristLength = 0;
            lowerWristLength = 8;
        }
        
        final double lineBetweenMiddleAndUpperWrist = Math.sqrt(upperWristLength*upperWristLength + middleWristLength*middleWristLength);
        final double inherentAngleBetweenMiddleAndUpperWrist = 48.98;
        final double forearmDegreeOffset = 23;
        final double conglomerateWristAngleToMiddleWrist = inherentAngleBetweenMiddleAndUpperWrist + upperWristDegrees + forearmDegrees + forearmDegreeOffset;
        final double wristAngle = upperWristDegrees + forearmDegrees + forearmDegreeOffset;
        final double bicepBaseHeight = 9.5;
        final double bicepBaseRadialOffset = 1;
        
        final double[] coordinatesAtBicepBase = new double[3];
        coordinatesAtBicepBase[0] = bicepBaseRadialOffset;
        coordinatesAtBicepBase[1] = baseDegrees;
        coordinatesAtBicepBase[2] = bicepBaseHeight;
        final double[] coordinatesAtBicep = localizeBicep(coordinatesAtBicepBase,bicepDegrees,bicepLength);
        final double[] coordinatesAtForearm = localizeForearm(coordinatesAtBicep, forearmDegrees, forearmLength);
        //the definition of the middle wrist changes based on the hand element
        final double[] coordinatesAtMiddleWrist = localizeMiddleWrist(coordinatesAtForearm, conglomerateWristAngleToMiddleWrist, lineBetweenMiddleAndUpperWrist);
        
        
        return getCoordinatesAtHandElement(coordinatesAtMiddleWrist, wristAngle, lowerWristDegrees, lowerWristLength);
        
    }
    
    /**
     * @param vectorOrigin the point in cylindrical coordinates of the beginning of the bicep.
     * @param bicepDegrees the rotation of the bicep degrees. Must be in [0,270]. See drawing for more information about where the degrees 
     *        of different joints are defined to start.
     * @param bicepLength the length of the bicep from start to end points in centimeters.
     * @return The cylindrical coordinates at the end of the bicep.
     */
    public static double[] localizeBicep(double[] vectorOrigin, int bicepDegrees, double bicepLength){
        final double[] vectorToBicep = new double[3];
        
        if(bicepDegrees >= 0 && bicepDegrees <= 90){
            vectorToBicep[0] = -bicepLength*Math.cos(Math.toRadians(bicepDegrees));
            vectorToBicep[1] = 0;
            vectorToBicep[2] = bicepLength*Math.sin(Math.toRadians(bicepDegrees));
        }else if(bicepDegrees > 90 && bicepDegrees <= 180){
            vectorToBicep[0] = bicepLength*Math.sin(Math.toRadians(bicepDegrees - 90));
            vectorToBicep[1] = 0;
            vectorToBicep[2] = bicepLength*Math.cos(Math.toRadians(bicepDegrees - 90));
        }else{//bicepDegrees is in (180,270]
            vectorToBicep[0] = bicepLength*Math.cos(Math.toRadians(bicepDegrees - 180));
            vectorToBicep[1] = 0;
            vectorToBicep[2] = -bicepLength*Math.sin(Math.toRadians(bicepDegrees - 180));
        }
        return addVectors(vectorToBicep, vectorOrigin);
    }
    
    /**
     * @param vectorOrigin the point in cylindrical coordinates of the beginning of the forearm.
     * @param forearmDegrees the rotation of the forearm degrees. Must be in [0,270]. See drawing for more information about where the degrees 
     *        of different joints are defined to start.
     * @param forearmLength the length of the forearm from start to end points in centimeters.
     * @return The cylindrical coordinates at the end of the forearm.
     */
    public static double[] localizeForearm(double[] vectorOrigin, int forearmDegrees, double forearmLength){
        final double[] vectorToForearm = new double[3];
        
        if(forearmDegrees >= 0 && forearmDegrees <= 90){
            vectorToForearm[0] = forearmLength*Math.cos(Math.toRadians(forearmDegrees));
            vectorToForearm[1] = 0;
            vectorToForearm[2] = -forearmLength*Math.sin(Math.toRadians(forearmDegrees));
        }else{//forearmDegrees + forearmDegreeOffset is in (90, 180]
            vectorToForearm[0] = -forearmLength*Math.sin(Math.toRadians(forearmDegrees - 90));
            vectorToForearm[1] = 0;
            vectorToForearm[2] = -forearmLength*Math.cos(Math.toRadians(forearmDegrees - 90));
        }
        
        return addVectors(vectorToForearm, vectorOrigin);
    }
    
    /**
     * 
     * @param vectorOrigin the point at which the middle wrist starts
     * @param middleWristDegrees the degrees that the middle wrist is tilted. See drawing for the starting degree position definition.
     * @param middleWristLength the length from the start to the tip of the middle wrist.
     * @return the cylindrical coordinates of the tip of the MiddleWrist. See drawing for more information.
     */
    private static double[] localizeMiddleWrist(double[] vectorOrigin, double middleWristDegrees, double middleWristLength){
        final double[] vectorToMiddleWrist = new double[3];
        if(middleWristDegrees > 90 && middleWristDegrees <= 180){
            vectorToMiddleWrist[0] = middleWristLength*Math.sin(Math.toRadians(middleWristDegrees - 90));
            vectorToMiddleWrist[1] = 0;
            vectorToMiddleWrist[2] = middleWristLength*Math.cos(Math.toRadians(middleWristDegrees - 90));
        }else if(middleWristDegrees > 180 && middleWristDegrees <= 270){
            vectorToMiddleWrist[0] = middleWristLength*Math.cos(Math.toRadians(middleWristDegrees - 180));
            vectorToMiddleWrist[1] = 0;
            vectorToMiddleWrist[2] = - middleWristLength*Math.sin(Math.toRadians(middleWristDegrees - 180));
        }else{//conglomerateWristAngle is in (270, 360]
            vectorToMiddleWrist[0] = - middleWristLength*Math.sin(Math.toRadians(middleWristDegrees - 270));
            vectorToMiddleWrist[1] = 0;
            vectorToMiddleWrist[2] = - middleWristLength*Math.cos(Math.toRadians(middleWristDegrees - 270));
        }
        
        return addVectors(vectorToMiddleWrist,vectorOrigin);
    }
    
    /**
     * 
     * @param coordinatesAtMiddleWrist the coordinates on the axis that the hand spins left or right. 
     *        One can travel parallel to the lower hand (wrist) from this point to reach the hand element.
     * @param wristAngle the angle that the lower wrist is tilted (up down) with respect to the forearm. See drawing for more information. Should be between 
     *        90 and 360.
     * @param lowerWristDegrees the left/right degrees from 0 to 180 with 90 being center, that the wrist is at.
     * @param lowerWristLength the length from the coordinates at the middle wrist to the hand element.
     * @return the coordinates of the hand element.
     */
    private static double[] getCoordinatesAtHandElement(double[] coordinatesAtMiddleWrist, double wristAngle, double lowerWristDegrees, double lowerWristLength){
        final double[] coordinatesAtGripper = new double[3];
        
        //I perform some transformations that briefly use a vector in 3D cartesian coordinates to figure the final cylindrical coordinates
        double yVectorToGripper = - lowerWristLength*Math.cos(Math.toRadians(lowerWristDegrees));
        if(wristAngle > 270){
            yVectorToGripper = - yVectorToGripper;
        }
        double yCoordinateToGripper = yVectorToGripper;
        
        double xVectorToGripper = lowerWristLength*Math.sin(Math.toRadians(lowerWristDegrees));
        if(wristAngle > 270){
            xVectorToGripper = -xVectorToGripper;
        }
        double xCoordinateToGripper = xVectorToGripper + coordinatesAtMiddleWrist[0];
        
        double zVectorToGripper = mapToTiltedPlaneAlongYAxisAndGetZVectorAtPoint(wristAngle, xVectorToGripper);
        
        final double gripperRadiusFromOrigin = Math.sqrt((xCoordinateToGripper)*(xCoordinateToGripper) +
                yCoordinateToGripper*yCoordinateToGripper);
        
        //using the law of sines
        final double additionalAngleResultingFromYOffset = Math.toDegrees( Math.asin(Math.toRadians( (yCoordinateToGripper*Math.toDegrees( Math.sin(Math.toRadians(90)) )/gripperRadiusFromOrigin) )) );
        
        coordinatesAtGripper[0] = gripperRadiusFromOrigin;
        coordinatesAtGripper[1] = coordinatesAtMiddleWrist[1] + additionalAngleResultingFromYOffset;
        coordinatesAtGripper[2] = zVectorToGripper + coordinatesAtMiddleWrist[2];
        
        return coordinatesAtGripper;
    }
    
    /**
     * In this method, x and y are arbitrarily defined and are designed for specific use as a subroutine of getCoordinatesAtHandElement
     * to get the elevation that the hand gains or looses from spinning left or right.
     * 
     * @param tilt the tilt of a plane starting from the xy plane on the -x side
     * @param x the value of the x coordinate on the tilted plane
     * @return z the corresponding z coordinate on the tilted plane
     */
    private static double mapToTiltedPlaneAlongYAxisAndGetZVectorAtPoint(double tilt, double x){
        double z;
        if(tilt <= 90){
            z = -Math.sin(Math.toRadians(tilt))*x/Math.cos(Math.toRadians(tilt));
        }else if(tilt > 90 && tilt <= 180){
            z = Math.cos(Math.toRadians(tilt-90))*x/Math.sin(Math.toRadians(tilt-90));
        }else if(tilt > 180 && tilt <= 270){
            z = -Math.sin(Math.toRadians(tilt-180))*x/Math.cos(Math.toRadians(tilt-180));
        }else{
            z = Math.cos(Math.toRadians(tilt-270))*x/Math.sin(Math.toRadians(tilt-270));
        }
        return z;
    }
    
    
    
    
    
    /**
     * 
     * @param vector1 an array of length 3
     * @param vector2 an array the same length as vector 1
     * @return an array that is the result of adding vector1 and vector2's value at each index.
     */
    public static double[] addVectors(double[] vector1, double[] vector2){
        final double[] vectorSum = new double[3];
        for(int index = 0; index < vector1.length; index++){
            vectorSum[index] = vector1[index] + vector2[index];
        }
        return vectorSum;
    }
    
    /**
     * 
     * @param cartesian an array representing Cartesian coordinates where index 0 is the x coordinate, index 1 is the y coordinate and
     *        index 2 is the zth coordinate;
     * @return an array representing the same point in cylindrical coordinates (r,theta,z), where the first quadrant corresponds to angles 0 to 90, the second 
     *         corresponds to angles 90 to 180, the third corresponds to angles 180 to 270 and the fourth corresponds to angles 270 to 360.
     */
    public static double[] toCylindrical(double[] cartesian){
        double[] cylindrical = new double[3];
        cylindrical[0] = Math.sqrt(cartesian[0]*cartesian[0] + cartesian[1]*cartesian[1]);
        cylindrical[1] = Math.abs(Math.toDegrees(Math.atan(cartesian[1]/cartesian[0])));
        if(cartesian[0] >= 0 && cartesian[1] >= 0){
            //keep cylindrical[1] the way it was
        }else if(cartesian[0] <= 0 && cartesian[1] >= 0){
            cylindrical[1] = 180 - cylindrical[1];
        }else if(cartesian[0] <= 0 && cartesian[1] <= 0){
            cylindrical[1] = 180 + cylindrical[1];
        }else{//(cartesian[0] >= 0 && cartesian[1] <= 0)
            cylindrical[1] = 360 - cylindrical[1];
        }
        cylindrical[2] = cartesian[2];
        return cylindrical;
    }
    
    /**
     * 
     * @param cylindrical an array representing cylindrical coordinates where index 0 is the r (radius, index 1 is the angle and
     *        index 2 is the zth coordinate;
     * @return any array representing the same point in Cartesian coordinates (x,y,z), where the first quadrant corresponds to angles 0 to 90, the second 
     *         corresponds to angles 90 to 180, the third corresponds to angles 180 to 270 and the fourth corresponds to angles 270 to 360.
     */
    public static double[] toCartesian(double[] cylindrical){
        double[] cartesian = new double[3];
        cartesian[0] = cylindrical[0]*Math.cos(Math.toRadians(cylindrical[1]));
        cartesian[1] = cylindrical[0]*Math.sin(Math.toRadians(cylindrical[1]));
        cartesian[2] = cylindrical[2];
        
        return cartesian;
    }
    
    /**
     * 
     * @return the maximum distance that can be reached by the arm in centimeters.
     */
    public static double maximumReachableRadius(){
        return 42;
    }
    
}
