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
        Piece target = board.get(to);
        if (rowDiff <= 1 && colDiff <= 1) {
            return target == null || target.getColor() != this.color;
        }
        if (!hasMoved && rowDiff == 0 && colDiff == 2) {
            return board.canCastle(from, to, color);
        }
        return false;
    }

}