package localizationutils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * This class contains static methods used to search for arm positions that cause the arm to travel to possible locations. 
 */

public class ArmPositionSearch {
    
/**
 * This method utilizes BFS to search over the space of possible locations that can be reached given the constraints. 
 * LocalizeHandElement can be used to find the coordinates of the gripper given integer joint positions representing degrees.
 * See LocalizeArmExperimental for the degree range of each joint).
 * 
 * @param requestedCylindricalCoordinates the desired position of the center of the gripper in cylindrical coordinates 
 *        (the origin is the center of the base of the arm, as always).
 * @param initialArmJointPositions the initial positions of the arm joints: an array with 0th index the base rotation degrees,
 *        the 1st index the bicep degrees and the 2nd index the forearm degrees.
 * @param givenHandJointPositions the hand positions that must stay static: an array with 0th index the wrist up/down degrees and 
 *        the 1st index the wrist left/right degrees.
 * @return the positions of all of the servos, (in the order of the second input to the method followed by the third input) in degrees (must be integers), that will cause the center of the gripper to end up in the 
 *         cylindrical coordinates requested with an error of at most .1 for each coordinate, or null if the coordinates are not reachable within the specified range of error.
 * @throws InterruptedException
 */
public static int[] gripperPositionSearch(double[] requestedCylindricalCoordinates, int[] initialArmJointPositions, int[] givenHandJointPositions) throws InterruptedException{
        
        int[] initialJointPositionsWithCorrectBaseRotation = initialArmJointPositions.clone();
        initialJointPositionsWithCorrectBaseRotation[0] = (int) requestedCylindricalCoordinates[1];
        List<Integer> initialJointPositionsWithCorrectBaseRotationAsList = Arrays.asList(initialJointPositionsWithCorrectBaseRotation[0], initialJointPositionsWithCorrectBaseRotation[1], 
                initialJointPositionsWithCorrectBaseRotation[2]);
        
        BlockingQueue<int[]> queue = new ArrayBlockingQueue<>(10000);
        queue.put(initialJointPositionsWithCorrectBaseRotation);
        
        Set<List<Integer>> visited = new HashSet<>();
        visited.add(initialJointPositionsWithCorrectBaseRotationAsList);
        
        while(queue.peek() != null){
            int[] currentNode = queue.take();
            
            for(int[] childNode : successors(currentNode)){
                
                //when computers become about 10,000 times faster at least, then search over the space of all joint movements. for now, leave out the hand joint space search because it would take 
                //48,000 times longer
                double[] childPosition = LocalizeArm.localizeHandElement(childNode[0],childNode[1],childNode[2],givenHandJointPositions[0],givenHandJointPositions[1],HandElement.GRIPPER);
                boolean positionFound = true;
                for(int index = 0; index < childPosition.length; index++){
                    if(Math.abs(childPosition[index] - requestedCylindricalCoordinates[index]) > 1){
                        positionFound = false;
                    }
                }
                if (positionFound){
                    int[] positions = new int[5];
                    positions[0] = childNode[0];
                    positions[1] = childNode[1];
                    positions[2] = childNode[2];
                    positions[3] = givenHandJointPositions[0];
                    positions[4] = givenHandJointPositions[1];
                    for(double pos : childPosition){
                        System.out.println(pos);
                    }
                    
                    return positions;
                }
                
                List<Integer> childNodeAsList = Arrays.asList(childNode[0], childNode[1], childNode[2]);
                if(!visited.contains(childNodeAsList)){
                    queue.put(childNode);
                    visited.add(childNodeAsList);
                }
            }
            
        }
        return null;
        
        
        
    }
    
    /**
     * 
     * @param node; an array with 0th index base degrees, 1st index bicep degrees and 2nd index forearmDegrees
     * @return a list containing the following successors:
     *              -a copy of the input array with bicep degrees incremented if it is still <= 270 (this is the upper limit of the bicep)
     *              -a copy of the input array with bicep degrees decremented if it is still >= 0 (this is the lower limit of the bicep)
     *              -a copy of the input array with forearm degrees incremented if it is still <= 180 (this is the upper limit of the forearm)
     *              -a copy of the input array with bicep degrees decremented if it is sill >= 0 (this is the lower limit of the forearm)
     */
    private static List<int[]> successors(int[] node){
        List<int[]>successors = new ArrayList<>();
        
        int[] potentialSuccessor1 = node.clone();
        int[] potentialSuccessor2 = node.clone();
        int[] potentialSuccessor3 = node.clone();
        int[] potentialSuccessor4 = node.clone();
        
        //bicep up and down 1 degree successors
        if(potentialSuccessor1[1]<270){
            potentialSuccessor1[1]++;
            successors.add(potentialSuccessor1);
        }
        if(potentialSuccessor2[1]>0){
            potentialSuccessor2[1]--;
            successors.add(potentialSuccessor2);
        }
        
        //forearm up and down 1 degree successors
        if(potentialSuccessor3[2]<90){
            potentialSuccessor3[2]++;
            successors.add(potentialSuccessor3);
        }
        if(potentialSuccessor4[2]>0){
            potentialSuccessor4[2]--;
            successors.add(potentialSuccessor4);
        }

        return successors;
        
    }
    
    public static void main(String[] args) throws InterruptedException{
        double[] requested = new double[3];
        requested[0] = 30;
        requested[1] = 180;
        requested[2] = 15;
        
        int[] initialArm = new int[3];
        initialArm[0] = 0;
        initialArm[1] = 0;
        initialArm[2] = 0;
        
        int[] givenHand = new int[3];
        givenHand[0] = 180;
        givenHand[1] = 90;
        givenHand[2] = 90;
        
        for(int degree : gripperPositionSearch(requested, initialArm, givenHand)){
            System.out.println(degree);
        }
        System.out.println("\n");
        
        double[] requested2 = new double[3];
        requested2[0] = 26;
        requested2[1] = 150;
        requested2[2] = 15;
        
        int[] givenHand2 = new int[3];
        givenHand2[0] = 180;
        givenHand2[1] = 120;
        givenHand2[2] = 90;
        
        for(int degree : gripperPositionSearch(requested2, initialArm, givenHand2)){
            System.out.println(degree);
        }
    }
    
    //TODO: fix that bug that doesn't find correct angle
    
}
