package controller;

import model.Board;
import model.Piece;
import model.Position;
import view.GameWindow;

import javax.swing.JOptionPane;
import java.awt.Color;

public class GameController {
    private Board board;
    private GameWindow view;
    private Color currentTurn = Color.WHITE;
    private Position selectedPosition = null;

    public GameController(Board board, GameWindow view) {
        this.board = board;
        this.view = view;
    }

    public void handleSquareClick(int row, int col) {
        Position clicked = new Position(row, col);
        if (selectedPosition == null) {
            handleSelection(clicked);
        } else {
            handleMoveOrReSelection(clicked);
        }
    }

    private void handleSelection(Position clicked) {
        Piece piece = board.get(clicked);
        if (piece != null) {
            if (piece.getColor() != currentTurn) {
                JOptionPane.showMessageDialog(view, "Chưa tới lượt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedPosition = clicked;
            view.resetBoardColors();
            view.highlightValidMoves(clicked, board);
        }
    }

    private void handleMoveOrReSelection(Position clicked) {
        Piece pieceAtClicked = board.get(clicked);
        if (selectedPosition.equals(clicked)) {
            selectedPosition = null;
            view.resetBoardColors();
        } else if (pieceAtClicked != null && pieceAtClicked.getColor() == currentTurn) {
            selectedPosition = clicked;
            view.resetBoardColors();
            view.highlightValidMoves(clicked, board);
        } else {
            processMove(clicked);
        }
    }

    private void processMove(Position destination) {
        boolean moved = board.move(selectedPosition, destination);
        if (moved) {
            view.updateBoardGUI();
            checkGameState();
            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
            SaveLoadController.autoSave(currentTurn, board);
            selectedPosition = null;
            view.resetBoardColors();
        } else {
            String msg = board.isInCheck(currentTurn)
                    ? "Bạn đang bị chiếu! Hãy chọn nước đi bảo vệ Vua."
                    : "Nước đi không hợp lệ!";
            JOptionPane.showMessageDialog(view, msg, "Lỗi di chuyển", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkGameState() {
        Color opponentColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean inCheck = board.isInCheck(opponentColor);
        boolean canMove = board.hasValidMoves(opponentColor);
        if (inCheck && !canMove) {
            JOptionPane.showMessageDialog(view, "CHIẾU HẾT! " + (currentTurn == Color.WHITE ? "Trắng" : "Đen") + " thắng!");
        } else if (!inCheck && !canMove) {
            JOptionPane.showMessageDialog(view, "HÒA CỜ (Stalemate)!");
        } else if (inCheck) {
            JOptionPane.showMessageDialog(view, "Đang bị CHIẾU!");
        }
    }

    public void setCurrentTurn(Color turn) {
        this.currentTurn = turn;
    }
}