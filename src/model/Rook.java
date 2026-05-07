package model;

import java.awt.Color;

public class Rook extends Piece {
    public Rook(Color color) {
        super(color, (color == Color.WHITE) ? 'R' : 'r');
    }
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        if (from.getR() != to.getR() && from.getC() != to.getC()) {
            return false;
        }
        int rowStep = Integer.compare(to.getR(), from.getR());
        int colStep = Integer.compare(to.getC(), from.getC());
        int r = from.getR() + rowStep;
        int c = from.getC() + colStep;
        while (r != to.getR() || c != to.getC()) {
            if (board.get(new Position(r, c)) != null) {
                return false;
            }
            r += rowStep;
            c += colStep;
        }
        Piece target = board.get(to);
        return target == null || target.getColor() != this.getColor();
    }
}