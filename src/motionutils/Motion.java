package motionutils;

public class Motion {
    
    public static String grab(int[] armPosition, double speed){
        int openDegrees = 140;
        String comma = ",";
        String out = "";
        out += "j7:" + openDegrees + comma;
        
        out += "j1:"+armPosition[0] + comma;
        out += "j2:"+armPosition[1] + comma;
        out += "j3:"+armPosition[2] + comma;
        out += "j4:"+armPosition[3] + comma;
        out += "j5:"+armPosition[4] + comma;
        out += "j6:"+90 + comma;
        
        out += "s:" + .2/speed + comma;
        
        out += "j7:" + 40 + comma;
        
        out += "s:" + .2/speed + comma;
        
        return out;
    }
    
    public static String throwObject(int degreeToThrowAt, double speed){
        int openDegrees = 140;
        String comma = ",";
        String out = "";
        
        out += "j1:"+ degreeToThrowAt + comma;
        out += "j2:"+ 50 + comma;
        out += "j3:"+ 45 + comma;
        out += "j4:"+ 145 + comma;
        out += "j5:"+ 90 + comma;
        out += "j6:"+ 90 + comma;
        
        out += "s:" + .3/speed + comma;
        
        out += "j1:"+ degreeToThrowAt + comma;
        out += "j2:"+ 135 + comma;
        out += "j3:"+ 0 + comma;
        out += "j4:"+ 130 + comma;
        out += "j5:"+ 90 + comma;
        out += "j6:"+ 90 + comma;
        
        out += "s:" + .25/speed + comma;
        
        out += "j7:"+ openDegrees + comma;
        
        
        return out;
    }
    
    public static String restPosition(){
        String comma = ",";
        String out = "";
        
        out += "s:" + .2 + comma;
        
        out += "j1:"+ 100 + comma;
        out += "j2:"+ 80 + comma;
        out += "j3:"+ 55 + comma;
        out += "j4:"+ 110 + comma;
        out += "j5:"+ 90 + comma;
        out += "j6:"+ 90 + comma;
    
        return out;
    }
}
