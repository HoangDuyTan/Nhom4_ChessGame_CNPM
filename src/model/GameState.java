package model;

import java.awt.Color;

public class GameState {
    private Piece[][] grid;
    private Color turn;
    private int whiteTimeLeft;
    private int blackTimeLeft;
    private Position enPassantTarget;

    public GameState(Board board, Color turn) {
        this.grid = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece originalPiece = board.get(new Position(r, c));
                this.grid[r][c] = copyPiece(originalPiece);
            }
        }
        this.turn = turn;
        this.enPassantTarget = copyPosition(board.getEnPassantTarget());
    }
    public GameState(Board board, Color turn, int whiteTimeLeft, int blackTimeLeft) {
        this.grid = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece originalPiece = board.get(new Position(r, c));
                this.grid[r][c] = copyPiece(originalPiece);
            }
        }
        this.turn = turn;
        this.whiteTimeLeft = whiteTimeLeft;
        this.blackTimeLeft = blackTimeLeft;
        this.enPassantTarget = copyPosition(board.getEnPassantTarget());
    }

    public void restore(Board board) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.set(new Position(r, c), grid[r][c]);
            }
        }
        board.setEnPassantTarget(copyPosition(enPassantTarget));
    }

    private Piece copyPiece(Piece originalPiece) {
        if (originalPiece == null) {
            return null;
        }

        Color color = originalPiece.getColor();
        char symbol = Character.toLowerCase(originalPiece.getShortName());
        Piece copy;
        switch (symbol) {
            case 'p': copy = new Pawn(color); break;
            case 'r': copy = new Rook(color); break;
            case 'n': copy = new Knight(color); break;
            case 'b': copy = new Bishop(color); break;
            case 'q': copy = new Queen(color); break;
            case 'k': copy = new King(color); break;
            default:  copy = null; break;
        }

        if (copy != null) {
            copy.setMoved(originalPiece.hasMoved());
        }
        return copy;
    }

    private Position copyPosition(Position position) {
        if (position == null) {
            return null;
        }
        return new Position(position.getR(), position.getC());
    }
    public int getWhiteTimeLeft() {
        return whiteTimeLeft;
    }

    public int getBlackTimeLeft() {
        return blackTimeLeft;
    }
    public Color getTurn() {
        return turn;
    }
    public Piece[][] getGrid() {
        return grid;
    }
}
