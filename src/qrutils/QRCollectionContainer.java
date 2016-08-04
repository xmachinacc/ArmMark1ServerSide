package qrutils;

public class QRCollectionContainer {
    
    private QRCollection element;
    
    public QRCollectionContainer(QRCollection element){
        this.element = element;
    }
    
    synchronized public void update(QRCollection element){
        this.element = element;
    }
    
    synchronized public QRCollection look(){
        return element;
    }
    
    @Override
    synchronized public String toString(){
        return element.toString();
    }
}
