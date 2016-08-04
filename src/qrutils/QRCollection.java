package qrutils;

import java.util.ArrayList;
import java.util.List;

public class QRCollection {
    private final List<QRElement> collection;
    
    public QRCollection(List<QRElement> collection){
        this.collection = new ArrayList<>(collection);
    }
    
    public List<QRElement> getCollection(){
        return new ArrayList<>(collection);
    }
    
    @Override
    public String toString(){
        return collection.toString();
    }
}
