package ballutils;

//TODO: specs!!!
public class BallElement {
    // Al Balls shall have radius of .75". This way, the arm will be able to
    // determine distance to the Ball based on relative size
    private final int centerX;
    private final int centerY;

    private final int radius;

    private final double distance;

    private final int imageWidth;
    private final int imageHeight;

    public BallElement(int centerX, int centerY, int radius, double distance, int imageWidth, int imageHeight) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.distance = distance;

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public int x() {
        return centerX;
    }

    public int y() {
        return centerY;
    }

    public int radius() {
        return radius;
    }

    public double area() {
        return Math.PI * radius * radius;
    }

    public double distance() {
        return distance;
    }

    public int imageHeight() {
        return imageHeight;
    }

    public int imageWidth() {
        return imageWidth;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof BallElement)) {
            return false;
        }
        final BallElement thatElement = (BallElement) that;
        return this.radius() == thatElement.radius() && this.x() == thatElement.x() && this.y() == thatElement.y()
                && this.distance() == thatElement.distance();
    }

    @Override
    public int hashCode() {
        return 17; // TODO: do better
    }

    @Override
    public String toString() {
        return "(" + x() + "," + y() + "," + radius() + "," + area() + "," + distance() + ")";
    }
}
