package ballutils;

import java.util.ArrayList;
import java.util.List;

public class BallCollection {
    private final List<BallElement> collection;

    public BallCollection(List<BallElement> collection) {
        this.collection = new ArrayList<>(collection);
    }

    public List<BallElement> getCollection() {
        return new ArrayList<>(collection);
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
