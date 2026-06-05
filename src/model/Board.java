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

    public boolean move(Position from, Position to) {
        Piece piece = get(from);
        if (piece == null || !piece.getColor().equals(piece.getColor())) return false;
        if (!piece.isValidMove(from, to, this)) return false;
        if (simulateMoveAndCheck(from, to, piece.getColor())) {
            return false;
        }
        grid[to.getR()][to.getC()] = piece;
        grid[from.getR()][from.getC()] = null;
        return true;
    }

    public Position findKing(Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p instanceof King && p.getColor().equals(color)) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    public boolean isInCheck(Color color) {
        Position kingPos = findKing(color);
        if (kingPos == null) return false;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && !p.getColor().equals(color)) {
                    if (p.isValidMove(new Position(r, c), kingPos, this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean simulateMoveAndCheck(Position from, Position to, Color color) {
        Piece originalFrom = grid[from.getR()][from.getC()];
        Piece originalTo = grid[to.getR()][to.getC()];
        grid[to.getR()][to.getC()] = originalFrom;
        grid[from.getR()][from.getC()] = null;
        boolean check = isInCheck(color);
        grid[from.getR()][from.getC()] = originalFrom;
        grid[to.getR()][to.getC()] = originalTo;
        return check;
    }

    public boolean hasValidMoves(Color color) {
        for (int r1 = 0; r1 < 8; r1++) {
            for (int c1 = 0; c1 < 8; c1++) {
                Position from = new Position(r1, c1);
                Piece p = get(from);
                if (p != null && p.getColor().equals(color)) {
                    for (int r2 = 0; r2 < 8; r2++) {
                        for (int c2 = 0; c2 < 8; c2++) {
                            Position to = new Position(r2, c2);
                            if (p.isValidMove(from, to, this) && !simulateMoveAndCheck(from, to, color)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /* * MÃ USE CASE: UC-04.2.3 (Khởi tạo lại bàn cờ)
     * Chức năng: Duyệt qua mảng chuỗi 8 dòng, phân tích từng ký tự (K, Q, R...)
     * và phân biệt chữ hoa/thường để khởi tạo lại chính xác các đối tượng Piece (King, Queen, Rook...).
     */
    public void loadGame(String[] rows) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = null;
            }
        }

        for (int r = 0; r < 8; r++) {
            String row = rows[r];
            for (int c = 0; c < 8; c++) {
                char symbol = row.charAt(c);
                if (symbol == '-') {
                    continue;
                }

                Color color = Character.isUpperCase(symbol) ? Color.WHITE : Color.BLACK;
                char lowerSymbol = Character.toLowerCase(symbol);

                Piece piece = null;
                switch (lowerSymbol) {
                    case 'k':
                        piece = new King(color);
                        break;
                    case 'q':
                        piece = new Queen(color);
                        break;
                    case 'r':
                        piece = new Rook(color);
                        break;
                    case 'b':
                        piece = new Bishop(color);
                        break;
                    case 'n':
                        piece = new Knight(color);
                        break;
                    case 'p':
                        piece = new Pawn(color);
                        break;
                }
                grid[r][c] = piece;
            }
        }
    }
}