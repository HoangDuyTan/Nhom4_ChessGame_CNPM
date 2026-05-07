package model;

import java.awt.Color;

public class Board {
    private Piece[][] grid = new Piece[8][8];
    public Board() {
        init();
    }
    public Piece get(Position pos) {
        return grid[pos.getR()][pos.getC()];
    }

    public void set(Position pos, Piece piece) {

        if (pos.isValid()) {
            grid[pos.getR()][pos.getC()] = piece;
        }
    }
    public void init() {
        grid[0][0] = new Rook(Color.WHITE);
        grid[0][1] = new Knight(Color.WHITE);
        grid[0][2] = new Bishop(Color.WHITE);
        grid[0][3] = new Queen(Color.WHITE);
        grid[0][4] = new King(Color.WHITE);
        grid[0][5] = new Bishop(Color.WHITE);
        grid[0][6] = new Knight(Color.WHITE);
        grid[0][7] = new Rook(Color.WHITE);

        for (int c = 0; c < 8; c++) {
            grid[1][c] = new Pawn(Color.WHITE);
        }
        grid[7][0] = new Rook(Color.BLACK);
        grid[7][1] = new Knight(Color.BLACK);
        grid[7][2] = new Bishop(Color.BLACK);
        grid[7][3] = new Queen(Color.BLACK);
        grid[7][4] = new King(Color.BLACK);
        grid[7][5] = new Bishop(Color.BLACK);
        grid[7][6] = new Knight(Color.BLACK);
        grid[7][7] = new Rook(Color.BLACK);

        for (int c = 0; c < 8; c++) {
            grid[6][c] = new Pawn(Color.BLACK);
        }
    }
}