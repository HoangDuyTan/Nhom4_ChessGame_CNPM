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
            if (to.getC() > from.getC()) {
                Piece rook = board.get(new Position(from.getR(), 7));
                if (rook instanceof Rook && !rook.hasMoved()) {
                    for (int c = from.getC() + 1; c < 7; c++) {
                        if (board.get(new Position(from.getR(), c)) != null) {
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                Piece rook = board.get(new Position(from.getR(), 0));
                if (rook instanceof Rook && !rook.hasMoved()) {
                    for (int c = 1; c < from.getC(); c++) {
                        if (board.get(new Position(from.getR(), c)) != null) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

}