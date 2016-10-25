
public class BooleanContainer {
    private boolean element;
    
    public BooleanContainer(boolean element){
        this.element = element;
    }
    
    public synchronized boolean getBoolean(){
        return element;
    }
    
    public synchronized void updateBoolean(boolean element){
        this.element = element;
    }
}
