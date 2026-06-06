package model;

import java.awt.Color;

public class GameState {
    private Piece[][] grid;
    private Color turn;
    private int whiteTimeLeft;
    private int blackTimeLeft;

    public GameState(Board board, Color turn, int whiteTimeLeft, int blackTimeLeft) {
        this.grid = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece originalPiece = board.get(new Position(r, c));
                if (originalPiece != null) {
                    Color color = originalPiece.getColor();
                    char symbol = Character.toLowerCase(originalPiece.getShortName());
                    Piece copiedPiece;
                    switch (symbol) {
                        case 'p': copiedPiece = new Pawn(color); break;
                        case 'r': copiedPiece = new Rook(color); break;
                        case 'n': copiedPiece = new Knight(color); break;
                        case 'b': copiedPiece = new Bishop(color); break;
                        case 'q': copiedPiece = new Queen(color); break;
                        case 'k': copiedPiece = new King(color); break;
                        default:  copiedPiece = null; break;
                    }
                    this.grid[r][c] = copiedPiece;
                } else {
                    this.grid[r][c] = null;
                }
            }
        }
        this.turn = turn;
        this.whiteTimeLeft = whiteTimeLeft;
        this.blackTimeLeft = blackTimeLeft;
    }

    public void restore(Board board) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.set(new Position(r, c), grid[r][c]);
            }
        }
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
