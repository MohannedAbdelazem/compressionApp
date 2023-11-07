public class tag {
    public int getDestination() {
        return destination;
    }

    public int getLength() {
        return length;
    }

    public char getNextCharacter() {
        return nextCharacter;
    }

    private int destination;
    private int length;
    private char nextCharacter;

    public tag(int destination, int length, char nextCharacter) {
        this.destination = destination;
        this.length = length;
        this.nextCharacter = nextCharacter;
    }

    @Override
    public String toString() {
        return "<" + destination + "," + length + "," + nextCharacter + ">";
    }

}
