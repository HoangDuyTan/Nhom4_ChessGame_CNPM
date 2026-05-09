package controller;

import model.Board;
import model.GameState;
import model.Piece;
import model.Position;
import view.GameWindow;
import view.SaveManager;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.util.Stack;
import javax.swing.Timer;

public class GameController {
    private Board board;
    private GameWindow view;
    private Color currentTurn = Color.WHITE;
    private Position selectedPosition = null;
    private Timer gameTimer;
    private int secondsElapsed = 0;
    private boolean isPaused = false;
    private boolean gameEnded = false;
    private Stack<GameState> undoStack = new Stack<>();
    private Stack<GameState> redoStack = new Stack<>();

    public GameController(Board board, GameWindow view) {
        this.board = board;
        this.view = view;

        startTimer();
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
        GameState stateBefore = new GameState(board, currentTurn);
        boolean moved = board.move(selectedPosition, destination);
        if (moved) {
            undoStack.push(stateBefore);
            redoStack.clear();
            view.updateBoardGUI();
            checkGameState();
            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
            SaveLoadController.autoSave(currentTurn, board, secondsElapsed);
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

    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            secondsElapsed++;
            view.updateTimer(secondsElapsed);
        });

        gameTimer.start();
    }

    public void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            gameTimer.stop();

            selectedPosition = null;
            view.resetBoardColors();

        } else {
            gameTimer.start();
        }

        view.updatePauseButton(isPaused);
    }

    public void handleSquareClick(int row, int col) {

        if (isPaused || gameEnded) {
            return;
        }

        Position clicked = new Position(row, col);

        if (selectedPosition == null) {
            handleSelection(clicked);
        } else {
            handleMoveOrReSelection(clicked);
        }
    }

    public void resignGame() {

        if (gameEnded) {
            return;
        }

        String loser =
                (currentTurn == Color.WHITE)
                        ? "Trắng"
                        : "Đen";

        String winner =
                (currentTurn == Color.WHITE)
                        ? "Đen"
                        : "Trắng";

        int choice = JOptionPane.showConfirmDialog(
                view,
                loser + " muốn đầu hàng?",
                "Xác nhận đầu hàng",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {

            gameEnded = true;

            gameTimer.stop();

            SaveManager.deleteSaveFile();

            JOptionPane.showMessageDialog(
                    view,
                    winner + " thắng do đối thủ đầu hàng!"
            );
        }
    }

    public void setCurrentTurn(Color turn) {
        this.currentTurn = turn;
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    public void setSecondsElapsed(int secondsElapsed) {
        this.secondsElapsed = secondsElapsed;
        view.updateTimer(secondsElapsed);
    }

    public void undo() {
        if (isPaused || gameEnded || undoStack.isEmpty()) return;
        GameState currentState = new GameState(board, currentTurn);
        redoStack.push(currentState);
        
        GameState previousState = undoStack.pop();
        previousState.restore(board);
        this.currentTurn = previousState.getTurn();
        
        view.updateBoardGUI();
        selectedPosition = null;
        view.resetBoardColors();
        SaveLoadController.autoSave(currentTurn, board, secondsElapsed);
    }

    public void redo() {
        if (isPaused || gameEnded || redoStack.isEmpty()) return;
        GameState currentState = new GameState(board, currentTurn);
        undoStack.push(currentState);

        GameState nextState = redoStack.pop();
        nextState.restore(board);
        this.currentTurn = nextState.getTurn();

        view.updateBoardGUI();
        selectedPosition = null;
        view.resetBoardColors();
        SaveLoadController.autoSave(currentTurn, board, secondsElapsed);
    }
}