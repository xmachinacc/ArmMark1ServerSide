package qrutils;


//TODO: specs!!!
public class QRElement {
    //Al QRElements shall be 1.5" by 1.5". This way, the arm will be able to determine distance to the QR Element based on relative size
    private final int centerX;
    private final int centerY;
   
    private final int width;
    private final int height;
    
    private final double distance;
    
    private final QRTitle title;
    
    private final int imageWidth;
    private final int imageHeight;
    
    public QRElement(int centerX, int centerY, int width, int height, double distance, QRTitle title, int imageWidth, int imageHeight){
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.distance = distance;
        this.title = title;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
    
    public int x(){
        return centerX;
    }
    
    public int y(){
        return centerY;
    }
    
    public int width(){
        return width;
    }
    
    public int height(){
        return height;
    }
    
    public QRTitle title(){
        return title;
    }
    
    public int area(){
        return width*height;
    }
    
    public double distance(){
        return distance;
    }
    
    public int imageHeight(){
        return imageHeight;
    }
    
    public int imageWidth(){
        return imageWidth;
    }
    
    @Override 
    public boolean equals(Object that){
            if (!(that instanceof QRElement)) {
                return false;
            }
            final QRElement thatElement = (QRElement) that;
            return this.title() == thatElement.title();
    }
    
    @Override 
    public int hashCode(){
        return 17; //TODO: do better
    }
    
    @Override 
    public String toString(){
        return "("+x()+","+y()+","+width()+","+height()+","+area()+","+distance()+","+title().toString()+")";
    }
}
