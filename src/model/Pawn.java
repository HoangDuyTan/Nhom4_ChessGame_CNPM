package model;

import java.awt.Color;

public class Pawn extends Piece {
    public Pawn(Color color) {
        super(color, (color == Color.WHITE) ? 'P' : 'p');
    }
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int direction = (color == Color.WHITE) ? 1 : -1;
        int startRow = (color == Color.WHITE) ? 1 : 6;
        int rowDiff = to.getR() - from.getR();
        int colDiff = Math.abs(to.getC() - from.getC());
        Piece target = board.get(to);
        if (colDiff == 0) {
            if (rowDiff == direction && target == null) {
                return true;
            }
            if (from.getR() == startRow && rowDiff == 2 * direction) {
                Position stepOver = new Position(from.getR() + direction, from.getC());
                if (board.get(stepOver) == null && target == null) {
                    return true;
                }
            }
        }
        if (colDiff == 1 && rowDiff == direction) {
            return target != null && target.getColor() != this.color;
        }
        return false;
    }
}