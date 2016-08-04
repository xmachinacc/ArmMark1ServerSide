package unusedfornow;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class ObjectPathFit {
    
    public static FallingObjectPath FallingObjectPathFit(List<double[]> pointsWithTime) throws Exception{
        double distanceTraveled = 0;
        List<double[]> xYPoints = new ArrayList<>();
        List<double[]> movementAxisZPoints = new ArrayList<>();
        for(int index = 0; index < pointsWithTime.size(); index++){
            
            if(index + 1 < pointsWithTime.size()){
            
                double[] first = pointsWithTime.get(index);
                double[] second = pointsWithTime.get(index+1);
            
                distanceTraveled += Math.sqrt((first[0]-second[0])*(first[0]-second[0]) + (first[1]-second[1])*(first[1]-second[1]) +
                               (first[2]-second[2])*(first[2]-second[2]));
            }
            
            xYPoints.add(array(pointsWithTime.get(index)[0],pointsWithTime.get(index)[1]));
            
        }
        double time = pointsWithTime.get(pointsWithTime.size()-1)[3] - pointsWithTime.get(0)[3];
        double groundSpeed = distanceTraveled/time;
        
        double[] linearRegression = regression(xYPoints, 1);
        double[] parabolicRegression = regression(movementAxisZPoints, 2);
        
        return new FallingObjectPath(linearRegression[1],linearRegression[0],
                parabolicRegression[2],parabolicRegression[1],parabolicRegression[0],groundSpeed,pointsWithTime.get(0));
        
    }
    

    private static double[] regression(List<double[]> points, int order) throws Exception{
        double[][] designArray = new double[points.size()][order+1];
        for(int index = 0; index < designArray.length; index++){
            double x = points.get(index)[0];
            for(int m = 0; m <= order; m++){
                designArray[index][m] = Math.pow(x, m);
            }
        }
        
        double[][] responseArray = new double[points.size()][1];
        for(int index = 0; index < designArray.length; index++){
            double y = points.get(index)[1];
            responseArray[index][0] = y;
        }
        
        Matrix design = new Matrix(designArray);
        Matrix response = new Matrix(responseArray);
        
        Matrix parameter = ((design.transpose().times(design)).inverse().times(design.transpose())).times(response);
        
        return parameter.getColumnPackedCopy();
        
    }
    
    private static double[] array(double x, double y){
        double[] array = new double[2];
        array[0] = x;
        array[1] = y;
        return array;
    }
}
