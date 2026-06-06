package controller;

import model.*;
import view.GameWindow;
import view.SaveManager;
import view.SoundManager;

import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GameController {
    private Board board;
    private GameWindow view;
    private Color currentTurn = Color.WHITE;
    private Position selectedPosition = null;
    private List<MoveLog> moveHistory = new ArrayList<>();
    private Timer gameTimer;
    private int secondsElapsed = 0;
    private final int BASE_TIME = 600;
    private final int INCREMENT = 5;
    private int whiteTimeLeft = BASE_TIME;
    private int blackTimeLeft = BASE_TIME;
    private boolean isPaused = false;
    private boolean gameEnded = false;
    private Stack<GameState> undoStack = new Stack<>();
    private Stack<GameState> redoStack = new Stack<>();
    private Stack<MoveLog> redoMoveHistory = new Stack<>();
    private int whiteUndoLeft = 3;
    private int blackUndoLeft = 3;
    private boolean playWithAI = false;
    private final Color aiColor = Color.BLACK;
    private boolean aiThinking = false;
    private final Random random = new Random();
    private int undoCount = 0;

    public GameController(Board board, GameWindow view) {
        this(board, view, false);
    }

    public GameController(Board board, GameWindow view, boolean playWithAI) {
        this.board = board;
        this.view = view;
        this.playWithAI = playWithAI;
        startTimer();
    }

    public List<MoveLog> getMoveHistory() {
        return moveHistory;
    }

    private void handleSelection(Position clicked) {
        Piece piece = board.get(clicked);
        if (piece == null) {
            return;
        }

        if (piece.getColor() != currentTurn) {
            if (view != null) {
                JOptionPane.showMessageDialog(view, "Chưa tới lượt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        selectedPosition = clicked;
        if (view != null) {
            view.resetBoardColors();
            view.highlightValidMoves(clicked, board);
        }
    }

    private void handleMoveOrReSelection(Position clicked) {
        Piece pieceAtClicked = board.get(clicked);
        if (selectedPosition.equals(clicked)) {
            selectedPosition = null;
            if (view != null) {
                view.resetBoardColors();
            }
        } else if (pieceAtClicked != null && pieceAtClicked.getColor() == currentTurn) {
            selectedPosition = clicked;
            if (view != null) {
                view.resetBoardColors();
                view.highlightValidMoves(clicked, board);
            }
        } else {
            processMove(clicked);
        }
    }

    private void processMove(Position destination) {
        processMoveFrom(selectedPosition, destination);
    }

    private boolean processMoveFrom(Position from, Position destination) {
        return processMoveFrom(from, destination, null, true);
    }

    private boolean processMoveFrom(Position from, Position destination, String promotionChoice, boolean showInvalidMessage) {
        if (from == null || destination == null || !from.isValid() || !destination.isValid()) {
            return false;
        }

        GameState stateBefore = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        Piece movingPiece = board.get(from);
        Piece targetPiece = board.getCapturedPiece(from, destination);
        boolean promotionMove = board.isPromotionMove(from, destination);
        String resolvedPromotionChoice = promotionMove ? resolvePromotionChoice(promotionChoice) : null;
        boolean moved = board.move(from, destination, resolvedPromotionChoice);

        if (moved) {
            SoundManager.playMove();
            MoveLog log = new MoveLog(from, destination, movingPiece, targetPiece, currentTurn, resolvedPromotionChoice);
            moveHistory.add(log);
            System.out.println("[LICH SU NUOC DI] " + log.getStandardNotation());

            undoStack.push(stateBefore);
            redoStack.clear();
            redoMoveHistory.clear();
            undoCount = 0;

            if (view != null) {
                view.updateBoardGUI();
            }

            checkGameState();
            if (gameEnded) {
                selectedPosition = null;
                if (view != null) {
                    view.resetBoardColors();
                }
                return true;
            }

            if (currentTurn == Color.WHITE) {
                whiteTimeLeft += INCREMENT;
            } else {
                blackTimeLeft += INCREMENT;
            }

            secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;

            if (view != null) {
                view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
                view.resetBoardColors();
            }

            SaveLoadController.autoSave(currentTurn, secondsElapsed, undoCount, moveHistory, playWithAI);
            selectedPosition = null;
            triggerAIMoveIfNeeded();
            return true;
        } else if (showInvalidMessage && view != null) {
            String msg = board.isInCheck(currentTurn)
                    ? "Bạn đang bị chiếu! Hãy chọn nước đi bảo vệ Vua."
                    : "Nước đi không hợp lệ!";
            JOptionPane.showMessageDialog(view, msg, "Lỗi di chuyển", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private String resolvePromotionChoice(String promotionChoice) {
        if (promotionChoice != null || view == null || GraphicsEnvironment.isHeadless()) {
            return Board.normalizePromotionChoice(promotionChoice);
        }

        String choice = (String) JOptionPane.showInputDialog(
                view,
                "Chọn quân để phong cấp:",
                "Pawn Promotion",
                JOptionPane.QUESTION_MESSAGE,
                null,
                Board.PROMOTION_CHOICES,
                Board.DEFAULT_PROMOTION_CHOICE
        );
        return Board.normalizePromotionChoice(choice);
    }

    private void checkGameState() {
        Color opponentColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean inCheck = board.isInCheck(opponentColor);
        boolean canMove = board.hasValidMoves(opponentColor);

        if (inCheck && !canMove) {
            endGame("CHIẾU HẾT! " + (currentTurn == Color.WHITE ? "Trắng" : "Đen") + " thắng!");
        } else if (!inCheck && !canMove) {
            endGame("HÒA CỜ (Stalemate)!");
        } else if (inCheck && view != null && (!playWithAI || opponentColor != aiColor)) {
            JOptionPane.showMessageDialog(view, "Đang bị CHIẾU!");
        }
    }

    private void endGame(String message) {
        gameEnded = true;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        SaveManager.deleteSaveFile(playWithAI);
        if (view != null) {
            JOptionPane.showMessageDialog(view, message);
        }
    }

    private boolean isAITurn() {
        return playWithAI && currentTurn == aiColor;
    }

    private void triggerAIMoveIfNeeded() {
        if (!isAITurn() || aiThinking || gameEnded || isPaused) {
            return;
        }

        aiThinking = true;
        selectedPosition = null;
        if (view != null) {
            view.resetBoardColors();
        }

        Timer aiTimer = new Timer(450, e -> {
            aiThinking = false;
            if (isAITurn() && !gameEnded && !isPaused) {
                makeAIMove();
            }
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    private void makeAIMove() {
        AIMove bestMove = chooseAIMove();
        if (bestMove == null) {
            gameEnded = true;
            if (gameTimer != null) {
                gameTimer.stop();
            }
            SaveManager.deleteSaveFile(playWithAI);
            return;
        }

        processMoveFrom(bestMove.getFrom(), bestMove.getTo(), Board.DEFAULT_PROMOTION_CHOICE, false);
    }

    private AIMove chooseAIMove() {
        List<AIMove> legalMoves = getLegalMoves(aiColor);
        AIMove bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        for (AIMove move : legalMoves) {
            int score = scoreAIMove(move);
            if (score > bestScore || (score == bestScore && random.nextBoolean())) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private List<AIMove> getLegalMoves(Color color) {
        List<AIMove> moves = new ArrayList<>();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Position from = new Position(fromRow, fromCol);
                Piece piece = board.get(from);
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        Position to = new Position(toRow, toCol);
                        if (board.isLegalMove(from, to)) {
                            moves.add(new AIMove(from, to));
                        }
                    }
                }
            }
        }
        return moves;
    }

    private int scoreAIMove(AIMove move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece movingPiece = board.get(from);
        Piece capturedPiece = board.getCapturedPiece(from, to);
        int score = random.nextInt(7);

        if (capturedPiece != null) {
            score += pieceValue(capturedPiece) * 10 - pieceValue(movingPiece);
        }

        if (movingPiece instanceof Pawn && (to.getR() == 0 || to.getR() == 7)) {
            score += pieceValue(new Queen(movingPiece.getColor())) - pieceValue(movingPiece);
        }

        score += centerBonus(to);

        GameState snapshot = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        if (board.move(from, to, Board.DEFAULT_PROMOTION_CHOICE)) {
            Color opponentColor = aiColor == Color.WHITE ? Color.BLACK : Color.WHITE;
            score += evaluateBoardFor(aiColor);
            if (board.isInCheck(opponentColor)) {
                score += 35;
            }
            if (!board.hasValidMoves(opponentColor) && board.isInCheck(opponentColor)) {
                score += 100000;
            }
        } else {
            score = Integer.MIN_VALUE;
        }
        snapshot.restore(board);

        return score;
    }

    private int evaluateBoardFor(Color color) {
        int score = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.get(new Position(row, col));
                if (piece == null) {
                    continue;
                }

                int value = pieceValue(piece);
                int direction = piece.getColor() == Color.WHITE ? row : 7 - row;
                if (piece instanceof Pawn) {
                    value += direction * 5;
                }
                value += centerBonus(new Position(row, col)) / 2;
                score += piece.getColor() == color ? value : -value;
            }
        }
        return score;
    }

    private int centerBonus(Position position) {
        int rowDistance = Math.abs(position.getR() - 3) + Math.abs(position.getR() - 4);
        int colDistance = Math.abs(position.getC() - 3) + Math.abs(position.getC() - 4);
        return 14 - rowDistance - colDistance;
    }

    private int pieceValue(Piece piece) {
        if (piece instanceof Pawn) return 100;
        if (piece instanceof Knight) return 320;
        if (piece instanceof Bishop) return 330;
        if (piece instanceof Rook) return 500;
        if (piece instanceof Queen) return 900;
        if (piece instanceof King) return 20000;
        return 0;
    }

    private void startTimer() {
        if (gameTimer != null) gameTimer.stop();

        gameTimer = new Timer(1000, e -> {
            if (!isPaused && !gameEnded) {
                if (currentTurn == Color.WHITE) whiteTimeLeft--;
                else blackTimeLeft--;

                secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
                if (view != null) {
                    view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
                }
                if (whiteTimeLeft <= 0) handleTimeOut(Color.WHITE);
                else if (blackTimeLeft <= 0) handleTimeOut(Color.BLACK);
            }
        });

        gameTimer.start();
    }

    private void handleTimeOut(Color loser) {
        gameEnded = true;
        if (gameTimer != null) {
            gameTimer.stop();
        }

        SaveManager.deleteSaveFile(playWithAI);

        String winner = (loser == Color.WHITE) ? "Quân Đen" : "Quân Trắng";
        if (view != null) {
            JOptionPane.showMessageDialog(view, "Hết giờ! " + winner + " giành chiến thắng.",
                    "Kết thúc ván đấu", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            selectedPosition = null;
            if (view != null) {
                view.resetBoardColors();
            }
        } else if (gameTimer != null) {
            gameTimer.start();
        }

        if (view != null) {
            view.updatePauseButton(isPaused);
        }
        triggerAIMoveIfNeeded();
    }

    public void handleSquareClick(int row, int col) {
        if (isPaused || gameEnded || isAITurn() || aiThinking) {
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

        String loser = (currentTurn == Color.WHITE) ? "Trắng" : "Đen";
        String winner = (currentTurn == Color.WHITE) ? "Đen" : "Trắng";
        int choice = JOptionPane.showConfirmDialog(
                view,
                loser + " muốn đầu hàng?",
                "Xác nhận đầu hàng",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            gameEnded = true;
            if (gameTimer != null) {
                gameTimer.stop();
            }
            SaveManager.deleteSaveFile(playWithAI);
            JOptionPane.showMessageDialog(view, winner + " thắng do đối thủ đầu hàng!");
        }
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Color turn) {
        this.currentTurn = turn;
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    public int getWhiteTimeLeft() {
        return whiteTimeLeft;
    }

    public void setWhiteTimeLeft(int whiteTimeLeft) {
        this.whiteTimeLeft = whiteTimeLeft;
    }

    public int getBlackTimeLeft() {
        return blackTimeLeft;
    }

    public void setBlackTimeLeft(int blackTimeLeft) {
        this.blackTimeLeft = blackTimeLeft;
    }

    public void setSecondsElapsed(int packedSeconds) {
        this.secondsElapsed = packedSeconds;
        this.whiteTimeLeft = (packedSeconds >> 16) & 0xFFFF;
        this.blackTimeLeft = packedSeconds & 0xFFFF;

        if (this.whiteTimeLeft == 0 && this.blackTimeLeft == 0) {
            this.whiteTimeLeft = BASE_TIME;
            this.blackTimeLeft = BASE_TIME;
        }

        if (view != null) {
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
        }
    }

    public void undo() {
        if (isPaused || gameEnded || undoStack.isEmpty() || aiThinking) return;
        if (playWithAI && undoStack.size() < 2) return;

        Color undoColor = playWithAI ? Color.WHITE : (currentTurn == Color.BLACK ? Color.WHITE : Color.BLACK);
        if (!consumeUndo(undoColor)) {
            return;
        }

        selectedPosition = null;
        if (view != null) {
            view.resetBoardColors();
        }

        GameState currentState = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        redoStack.push(currentState);

        GameState previousState;
        if (playWithAI) {
            GameState aiState = undoStack.pop();
            redoStack.push(aiState);
            previousState = undoStack.pop();
            moveLastHistoryToRedo();
            moveLastHistoryToRedo();
        } else {
            previousState = undoStack.pop();
            moveLastHistoryToRedo();
        }

        previousState.restore(board);
        currentTurn = previousState.getTurn();
        whiteTimeLeft = previousState.getWhiteTimeLeft();
        blackTimeLeft = previousState.getBlackTimeLeft();

        if (undoColor == Color.WHITE) {
            whiteTimeLeft = Math.max(0, whiteTimeLeft - 10);
        } else {
            blackTimeLeft = Math.max(0, blackTimeLeft - 10);
        }

        secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
        undoCount++;

        if (view != null) {
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
            view.updateBoardGUI();
        }
        SaveLoadController.autoSave(currentTurn, secondsElapsed, undoCount, moveHistory, playWithAI);
    }

    private boolean consumeUndo(Color color) {
        if (color == Color.WHITE) {
            if (whiteUndoLeft <= 0) {
                if (view != null) {
                    JOptionPane.showMessageDialog(view, "Quân TRẮNG đã hết lượt Đi Lại (tối đa 3 lần)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
                return false;
            }
            whiteUndoLeft--;
            return true;
        }

        if (blackUndoLeft <= 0) {
            if (view != null) {
                JOptionPane.showMessageDialog(view, "Quân ĐEN đã hết lượt Đi Lại (tối đa 3 lần)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }
        blackUndoLeft--;
        return true;
    }

    private void moveLastHistoryToRedo() {
        if (!moveHistory.isEmpty()) {
            redoMoveHistory.push(moveHistory.remove(moveHistory.size() - 1));
        }
    }

    public void redo() {
        if (isPaused || gameEnded || redoStack.isEmpty() || aiThinking) return;
        if (playWithAI && redoStack.size() < 2) return;

        selectedPosition = null;
        if (view != null) {
            view.resetBoardColors();
        }

        GameState currentState = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        undoStack.push(currentState);

        GameState nextState;
        if (playWithAI) {
            undoStack.push(redoStack.pop());
            nextState = redoStack.pop();
            restoreRedoHistory();
            restoreRedoHistory();
        } else {
            nextState = redoStack.pop();
            restoreRedoHistory();
        }

        nextState.restore(board);
        currentTurn = nextState.getTurn();
        whiteTimeLeft = nextState.getWhiteTimeLeft();
        blackTimeLeft = nextState.getBlackTimeLeft();
        secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
        undoCount++;

        if (view != null) {
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
            view.updateBoardGUI();
        }
        SaveLoadController.autoSave(currentTurn, secondsElapsed, undoCount, moveHistory, playWithAI);
    }

    private void restoreRedoHistory() {
        if (!redoMoveHistory.isEmpty()) {
            moveHistory.add(redoMoveHistory.pop());
        }
    }

    public void replayMoveForLoad(Position from, Position to) {
        replayMoveForLoad(from, to, null);
    }

    public void replayMoveForLoad(Position from, Position to, String promotionChoice) {
        GameState stateBefore = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        undoStack.push(stateBefore);
        board.move(from, to, promotionChoice);
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        redoMoveHistory.clear();
        moveHistory.clear();
    }

    public void restartGame() {
        board.reset();
        currentTurn = Color.WHITE;
        selectedPosition = null;
        gameEnded = false;
        isPaused = false;
        aiThinking = false;
        whiteUndoLeft = 3;
        blackUndoLeft = 3;
        undoCount = 0;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        whiteTimeLeft = BASE_TIME;
        blackTimeLeft = BASE_TIME;
        secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);

        startTimer();
        clearHistory();

        if (view != null) {
            view.resetBoardColors();
            view.updateBoardGUI();
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
            view.updatePauseButton(false);
        }
    }

    public int getUndoCount() {
        return undoCount;
    }

    public void setUndoCount(int undoCount) {
        this.undoCount = undoCount;
    }
}
