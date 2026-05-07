package model;

import java.awt.Color;

public class Bishop extends Piece {
	public Bishop(Color color) {
		super(color, (color == Color.WHITE) ? 'B' : 'b');
	}
    @Override
    public boolean isValidMove(Position from, Position to, Board board) {
        int rowDiff = Math.abs(from.getR() - to.getR());
        int colDiff = Math.abs(from.getC() - to.getC());
        if (rowDiff != colDiff || rowDiff == 0) {
            return false;
        }
        int rowStep = (to.getR() > from.getR()) ? 1 : -1;
        int colStep = (to.getC() > from.getC()) ? 1 : -1;
        int r = from.getR() + rowStep;
        int c = from.getC() + colStep;
        while (r != to.getR()) {
            if (r < 0 || r > 7 || c < 0 || c > 7) return false;
            if (board.get(new Position(r, c)) != null) {
                return false;
            }
            r += rowStep;
            c += colStep;
        }
        Piece target = board.get(to);
        return target == null || target.getColor() != this.color;
    }
}
