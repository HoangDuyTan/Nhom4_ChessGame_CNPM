package model;

public class AIMove {
    private final Position from;
    private final Position to;

    public AIMove(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }
}
