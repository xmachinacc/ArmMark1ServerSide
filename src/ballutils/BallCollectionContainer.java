package ballutils;

public class BallCollectionContainer {
    
    private BallCollection element;
    
    public BallCollectionContainer(BallCollection element){
        this.element = element;
    }
    
    synchronized public void update(BallCollection element){
        this.element = element;
    }
    
    synchronized public BallCollection look(){
        return element;
    }
    
    @Override
    synchronized public String toString(){
        return element.toString();
    }
}
