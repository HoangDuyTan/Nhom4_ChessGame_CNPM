package model;

import java.awt.Color;

public class Queen extends Piece {
    public Queen(Color color) {
        super(color, (color == Color.WHITE) ? 'Q' : 'q');
    }
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(from.getR() - to.getR());
        int colDiff = Math.abs(from.getC() - to.getC());
        boolean diagonal = rowDiff == colDiff;
        boolean straight =
                from.getR() == to.getR() ||
                        from.getC() == to.getC();
        if (!diagonal && !straight) {
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
        return target == null ||
                target.getColor() != this.color;
    }
}