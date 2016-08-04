package unusedfornow;

import localizationutils.LocalizeArm;

public class FallingObjectPath {
    final private double shadowSlope;
    final private double shadowYIntercept;
    final private double parabolaSecondOrderCoefficient;
    final private double parabolaFirstOrderCoefficient;
    final private double parabolaZerothOrderCoefficient;
    final private double groundSpeed;
    final private double[] initialPoint;
    final private double angleOfShadow;
    
    public FallingObjectPath(double shadowSlope, double shadowYIntercept, double parabolaSecondOrderCoefficient, double parabolaFirstOrderCoefficient, double parabolaZerothOrderCoefficient, double groundSpeed, double[] initialPoint){
        this.shadowSlope = shadowSlope;
        this.shadowYIntercept = shadowYIntercept;
        this.parabolaSecondOrderCoefficient = parabolaSecondOrderCoefficient;
        this.parabolaFirstOrderCoefficient = parabolaFirstOrderCoefficient;
        this.parabolaZerothOrderCoefficient = parabolaZerothOrderCoefficient;
        this.groundSpeed = groundSpeed;
        this.initialPoint = initialPoint;
        this.angleOfShadow = Math.abs(Math.atan(shadowSlope));
    }
    
    public double[] getCoordinatesOnPath(double time){
        final double distanceTraveled = time*groundSpeed;
        final double xCoordinate = initialPoint[0] + distanceTraveled*Math.cos(angleOfShadow);
        final double yCoordinate = shadowSlope*xCoordinate + shadowYIntercept;
        final double movementAxisCoordinate = Math.sqrt(xCoordinate*xCoordinate + yCoordinate*yCoordinate);
        
        final double zCoordinate = parabolaSecondOrderCoefficient*movementAxisCoordinate*movementAxisCoordinate + parabolaFirstOrderCoefficient*movementAxisCoordinate + parabolaZerothOrderCoefficient;
        final double[] coordinatesOnPath = new double[3];
        coordinatesOnPath[0] = xCoordinate;
        coordinatesOnPath[1] = yCoordinate;
        coordinatesOnPath[2] = zCoordinate;
        return LocalizeArm.toCylindrical(coordinatesOnPath);
    }
}
