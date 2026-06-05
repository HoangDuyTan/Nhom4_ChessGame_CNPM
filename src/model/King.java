package model;

import java.awt.Color;

public class King extends Piece {
    public King(Color color) {
        super(color, (color == Color.WHITE) ? 'K' : 'k');

    }
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(from.getR() - to.getR());
        int colDiff = Math.abs(from.getC() - to.getC());
        if (rowDiff > 1 || colDiff > 1) {
            return false;
        }
        Piece target = board.get(to);
        return target == null || target.getColor() != this.color;
    }
}