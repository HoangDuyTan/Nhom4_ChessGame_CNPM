package model;

import java.awt.Color;

public class GameState {
    private Piece[][] grid;
    private Color turn;

    public GameState(Board board, Color turn) {
        this.grid = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                this.grid[r][c] = board.get(new Position(r, c));
            }
        }
        this.turn = turn;
    }

    public void restore(Board board) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.set(new Position(r, c), grid[r][c]);
            }
        }
    }

    public Color getTurn() {
        return turn;
    }
}
