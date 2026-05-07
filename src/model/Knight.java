package model;

import java.awt.Color;

public class Knight extends Piece {
    public Knight(Color color) {
        super(color, (color == Color.WHITE) ? 'N' : 'n');
    }
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(from.getR() - to.getR());
        int colDiff = Math.abs(from.getC() - to.getC());
        boolean valid =(rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        if (!valid) return false;
        Piece target = board.get(to);
        return target == null ||
                target.getColor() != this.color;
    }
}