package part2.RMI.puzzleboard;

import java.io.Serializable;

public class Tile implements Comparable<Tile>, Serializable {

    private final byte[] image;
    private final int originalPosition;
    private int currentPosition;

    public Tile(final byte[] image, final int originalPosition, final int currentPosition) {
        this.image = image;
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    public byte[] getImage() {
        return image;
    }

    public boolean isInRightPlace() {
        return currentPosition == originalPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(final int newPosition) {
        currentPosition = newPosition;
    }

    @Override
    public int compareTo(Tile other) {
        return Integer.compare(this.currentPosition, other.currentPosition);
    }

}
