package model;

import java.awt.Color;

public class Board {
    public static final String DEFAULT_PROMOTION_CHOICE = "Queen";
    public static final String[] PROMOTION_CHOICES = {"Queen", "Rook", "Bishop", "Knight"};

    private Piece[][] grid = new Piece[8][8];
    private Position enPassantTarget = null;

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
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }

    public void init() {
        clear();
        enPassantTarget = null;

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
    /**
     * CHỨC NĂNG: UC-02.5: Apply Move (Áp dụng nước đi)
     * Mô tả: Xác thực toàn bộ luật đi, xử lý các trường hợp di chuyển đặc biệt
     * và cập nhật trực tiếp vị trí mới của quân cờ lên ma trận dữ liệu grid[8][8].
     */
    public boolean move(Position from, Position to) {
        return move(from, to, null);
    }

    public boolean move(Position from, Position to, String promotionChoice) {
        if (from == null || to == null || !from.isValid() || !to.isValid()) {
            return false;
        }

        Piece piece = get(from);
        if (piece == null) return false;
        if (!isLegalMove(from, to, piece)) return false;
        boolean castlingMove = isCastlingMove(piece, from, to);
        /**
         * CHỨC NĂNG CHI TIẾT: UC-02.1.4: En passant (Bắt tốt qua đường)
         * Nhánh xử lý: Ăn quân Tốt địch đứng ngang hàng khi quân đó vừa nhảy bước đôi từ vị trí xuất phát.
         */
        if (isEnPassantMove(from, to, piece)) {
            Position capturedPawnPosition = getEnPassantCapturedPawnPosition(to, piece.getColor());
            grid[capturedPawnPosition.getR()][capturedPawnPosition.getC()] = null;
        }
        /**
         * CHỨC NĂNG CHI TIẾT: UC-02.1.2: Castling (Nhập thành)
         * Nhánh xử lý: Vua di chuyển 2 ô sang ngang và hoán đổi vị trí phòng thủ an toàn cùng quân Xe.
         */
        if (castlingMove) {
            moveCastlingRook(from, to);
        }
        /**
         * CHỨC NĂNG CHI TIẾT: UC-02.1.1: Capture (Ăn quân thông thường) & Di chuyển ô trống
         * Ghi đè quân đi vào ô đích (Nếu có quân địch ở đó, quân địch tự động bị loại bỏ khỏi grid).
         */
        grid[to.getR()][to.getC()] = piece;
        grid[from.getR()][from.getC()] = null;
        piece.setMoved(true);
        enPassantTarget = null;
        if (piece instanceof Pawn && Math.abs(to.getR() - from.getR()) == 2) {
            int middleRow = (from.getR() + to.getR()) / 2;
            enPassantTarget = new Position(middleRow, from.getC());
        }
        /**
         * CHỨC NĂNG CHI TIẾT: UC-02.1.3: Pawn promotion (Phong cấp tốt)
         */
        if (piece instanceof Pawn) {
            if (isPromotionRank(piece, to)) {
                grid[to.getR()][to.getC()] = createPromotedPiece(piece.getColor(), promotionChoice);
            }
        }
        return true;
    }

    public boolean isPromotionMove(Position from, Position to) {
        if (from == null || to == null || !from.isValid() || !to.isValid()) {
            return false;
        }

        Piece piece = get(from);
        return piece instanceof Pawn && isPromotionRank(piece, to) && isLegalMove(from, to, piece);
    }

    public Piece createPromotedPiece(Color color, String promotionChoice) {
        Piece promotedPiece;
        switch (normalizePromotionChoice(promotionChoice)) {
            case "Rook":
                promotedPiece = new Rook(color);
                break;
            case "Bishop":
                promotedPiece = new Bishop(color);
                break;
            case "Knight":
                promotedPiece = new Knight(color);
                break;
            default:
                promotedPiece = new Queen(color);
                break;
        }
        promotedPiece.setMoved(true);
        return promotedPiece;
    }

    public static String normalizePromotionChoice(String promotionChoice) {
        if (promotionChoice == null) {
            return DEFAULT_PROMOTION_CHOICE;
        }

        for (String choice : PROMOTION_CHOICES) {
            if (choice.equalsIgnoreCase(promotionChoice.trim())) {
                return choice;
            }
        }
        return DEFAULT_PROMOTION_CHOICE;
    }

    private boolean isPromotionRank(Piece piece, Position to) {
        return (piece.getColor() == Color.WHITE && to.getR() == 7)
                || (piece.getColor() == Color.BLACK && to.getR() == 0);
    }

    public boolean isLegalMove(Position from, Position to) {
        if (from == null || to == null || !from.isValid() || !to.isValid()) {
            return false;
        }

        Piece piece = get(from);
        return isLegalMove(from, to, piece);
    }

    private boolean isLegalMove(Position from, Position to, Piece piece) {
        if (piece == null || !piece.isValidMove(from, to, this)) {
            return false;
        }

        Piece target = get(to);
        if (target instanceof King) {
            return false;
        }

        return isCastlingMove(piece, from, to) || !simulateMoveAndCheck(from, to, piece.getColor());
    }

    private boolean isCastlingMove(Piece piece, Position from, Position to) {
        return piece instanceof King
                && from.getR() == to.getR()
                && Math.abs(to.getC() - from.getC()) == 2;
    }

    private void moveCastlingRook(Position from, Position to) {
        int rookFromCol = to.getC() > from.getC() ? 7 : 0;
        int rookToCol = to.getC() > from.getC() ? 5 : 3;
        Piece rook = grid[from.getR()][rookFromCol];
        grid[from.getR()][rookToCol] = rook;
        grid[from.getR()][rookFromCol] = null;
        rook.setMoved(true);
    }

    public boolean canCastle(Position from, Position to, Color color) {
        Piece king = get(from);
        if (!(king instanceof King) || king.getColor() != color || king.hasMoved()) {
            return false;
        }
        if (from.getR() != to.getR() || Math.abs(to.getC() - from.getC()) != 2) {
            return false;
        }

        int rookCol = to.getC() > from.getC() ? 7 : 0;
        int step = to.getC() > from.getC() ? 1 : -1;
        Piece rook = grid[from.getR()][rookCol];
        if (!(rook instanceof Rook) || rook.getColor() != color || rook.hasMoved()) {
            return false;
        }

        for (int c = from.getC() + step; c != rookCol; c += step) {
            if (grid[from.getR()][c] != null) {
                return false;
            }
        }

        Color opponentColor = color == Color.WHITE ? Color.BLACK : Color.WHITE;
        if (isInCheck(color)) {
            return false;
        }
        for (int c = from.getC() + step; c != to.getC() + step; c += step) {
            if (isSquareAttacked(new Position(from.getR(), c), opponentColor)) {
                return false;
            }
        }

        return true;
    }

    public boolean canCaptureEnPassant(Position from, Position to, Color color) {
        if (from == null || to == null || color == null || !from.isValid() || !to.isValid()) {
            return false;
        }

        if (enPassantTarget == null || !enPassantTarget.equals(to) || get(to) != null) {
            return false;
        }

        int direction = color == Color.WHITE ? 1 : -1;
        if (to.getR() - from.getR() != direction || Math.abs(to.getC() - from.getC()) != 1) {
            return false;
        }

        Position capturedPawnPosition = getEnPassantCapturedPawnPosition(to, color);
        if (capturedPawnPosition == null || !capturedPawnPosition.isValid()) {
            return false;
        }

        Piece capturedPawn = get(capturedPawnPosition);
        return capturedPawn instanceof Pawn && capturedPawn.getColor() != color;
    }

    public Piece getCapturedPiece(Position from, Position to) {
        if (from == null || to == null || !from.isValid() || !to.isValid()) {
            return null;
        }

        Piece target = get(to);
        if (target != null) {
            return target;
        }

        Piece movingPiece = get(from);
        if (!isEnPassantMove(from, to, movingPiece)) {
            return null;
        }

        Position capturedPawnPosition = getEnPassantCapturedPawnPosition(to, movingPiece.getColor());
        return get(capturedPawnPosition);
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
        Color opponentColor = color == Color.WHITE ? Color.BLACK : Color.WHITE;
        return isSquareAttacked(kingPos, opponentColor);
    }

    public boolean isSquareAttacked(Position square, Color attackingColor) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getColor().equals(attackingColor)) {
                    if (attacksSquare(p, new Position(r, c), square)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean attacksSquare(Piece piece, Position from, Position square) {
        if (piece instanceof Pawn) {
            int direction = piece.getColor() == Color.WHITE ? 1 : -1;
            return square.getR() - from.getR() == direction
                    && Math.abs(square.getC() - from.getC()) == 1;
        }
        if (piece instanceof King) {
            int rowDiff = Math.abs(from.getR() - square.getR());
            int colDiff = Math.abs(from.getC() - square.getC());
            return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0);
        }

        return piece.isValidMove(from, square, this);
    }
    /**
     * Thuộc bộ chức năng: UC-02.4: Check King Safety (Kiểm tra an toàn của Vua)
     * Mô tả: Giả lập nước đi toán học trên grid để thử nghiệm độ an toàn của Vua.
     */
    public boolean simulateMoveAndCheck(Position from, Position to, Color color) {
        Piece originalFrom = grid[from.getR()][from.getC()];
        Piece originalTo = grid[to.getR()][to.getC()];
        Position enPassantCapturedPawnPosition = null;
        Piece enPassantCapturedPawn = null;
        if (isEnPassantMove(from, to, originalFrom)) {
            enPassantCapturedPawnPosition = getEnPassantCapturedPawnPosition(to, originalFrom.getColor());
            enPassantCapturedPawn = grid[enPassantCapturedPawnPosition.getR()][enPassantCapturedPawnPosition.getC()];
            grid[enPassantCapturedPawnPosition.getR()][enPassantCapturedPawnPosition.getC()] = null;
        }

        grid[to.getR()][to.getC()] = originalFrom;
        grid[from.getR()][from.getC()] = null;
        boolean check = isInCheck(color);
        grid[from.getR()][from.getC()] = originalFrom;
        grid[to.getR()][to.getC()] = originalTo;
        if (enPassantCapturedPawnPosition != null) {
            grid[enPassantCapturedPawnPosition.getR()][enPassantCapturedPawnPosition.getC()] = enPassantCapturedPawn;
        }
        return check;
    }

    private boolean isEnPassantMove(Position from, Position to, Piece piece) {
        return piece instanceof Pawn && canCaptureEnPassant(from, to, piece.getColor());
    }

    private Position getEnPassantCapturedPawnPosition(Position to, Color movingColor) {
        int capturedPawnRow = movingColor == Color.WHITE ? to.getR() - 1 : to.getR() + 1;
        return new Position(capturedPawnRow, to.getC());
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
                            if (isLegalMove(from, to)) {
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
        clear();
        enPassantTarget = null;

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
    public void reset() {
        init();
    }

    private void clear() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = null;
            }
        }
    }
}
