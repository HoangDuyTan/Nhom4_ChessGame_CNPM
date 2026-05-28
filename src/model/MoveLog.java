package model;

import java.awt.Color;

public class MoveLog {
    private Position from;
    private Position to;
    private Piece movedPiece;
    private Piece capturedPiece;
    private Color playerColor;

    public MoveLog(Position from, Position to, Piece movedPiece, Piece capturedPiece, Color playerColor) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.playerColor = playerColor;
    }
    private String convertToNotation(Position pos) {
        if (pos == null) return "??";
        char file = (char) ('a' + pos.getC());
        int rank = pos.getR() + 1;
        return "" + file + rank;
    }
    public String getStandardNotation() {
        String colorText = (playerColor == Color.WHITE) ? "Trắng" : "Đen";
        String pieceName = movedPiece != null ? String.valueOf(movedPiece.getShortName()).toUpperCase() : "P";
        switch (pieceName) {
            case "P": pieceName = "Tốt"; break;
            case "R": pieceName = "Xe"; break;
            case "N": pieceName = "Mã"; break;
            case "B": pieceName = "Tượng"; break;
            case "Q": pieceName = "Hậu"; break;
            case "K": pieceName = "Vua"; break;
            default: pieceName = "Quân"; break;
        }
        String fromStr = convertToNotation(from);
        String toStr = convertToNotation(to);

        if (capturedPiece != null) {
            return String.format("%s: %s %s x %s (Ăn)", colorText, pieceName, fromStr, toStr);
        } else {
            return String.format("%s: %s %s -> %s", colorText, pieceName, fromStr, toStr);
        }
    }
    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public Color getPlayerColor() { return playerColor; }
}