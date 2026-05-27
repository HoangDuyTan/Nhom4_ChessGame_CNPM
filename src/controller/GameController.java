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
    private final int BASE_TIME = 600;
    private final int INCREMENT = 5;
    private int whiteTimeLeft = BASE_TIME;
    private int blackTimeLeft = BASE_TIME;
    private boolean isPaused = false;

    private boolean gameEnded = false;
    private Stack<GameState> undoStack = new Stack<>();
    private Stack<GameState> redoStack = new Stack<>();

    public GameController(Board board, GameWindow view) {
        this.board = board;
        this.view = view;

        startTimer();
    }
    /**
     * CHỨC NĂNG: UC-02.1: Select Piece (Chọn quân cờ)
     * Mô tả: Người chơi chọn một quân cờ của phe mình. Hệ thống kiểm tra lượt đi
     * và ghi nhận quân cờ hợp lệ để chuẩn bị cho bước di chuyển tiếp theo.
     */
    private void handleSelection(Position clicked) {
        Piece piece = board.get(clicked);
        if (piece != null) {
            if (piece.getColor() != currentTurn) {
                JOptionPane.showMessageDialog(view, "Chưa tới lượt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedPosition = clicked;
            view.resetBoardColors();
            /**
             * CHỨC NĂNG: UC-02.3: Check Available Move (Kiểm tra nước đi có sẵn)
             * Mô tả: Hệ thống tự động tính toán luật đi của quân cờ để highlight các ô đích khả dụng.
             */
            view.highlightValidMoves(clicked, board);
        }
    }
    /**
     * CHỨC NĂNG: Điều phối hành động nhấp chuột khi đã có quân cờ được chọn trước đó
     * Mô tả: Phân tách hành vi người chơi dựa trên ô click tiếp theo (Hủy chọn / Đổi quân / Đi quân).
     */
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
    /**
     * CHỨC NĂNG: UC-02.2: Select Destination (Chọn ô đích) & Xử lý di chuyển quân
     * Mô tả: Hạ quân cờ xuống vị trí mới, lưu lịch sử, cập nhật bàn cờ và đổi lượt chơi.
     */
    private void processMove(Position destination) {
        GameState stateBefore = new GameState(board, currentTurn);
        boolean moved = board.move(selectedPosition, destination);
        if (moved) {
            undoStack.push(stateBefore);
            redoStack.clear();
            view.updateBoardGUI();
            checkGameState();
            /**
             * CHỨC NĂNG: UC-02.7: Switch Turn (Đổi lượt chơi)
             * Mô tả: Đảo quyền kiểm soát bàn cờ từ Trắng sang Đen hoặc ngược lại.
             */
            // --- BẮT ĐẦU: CỘNG GIỜ FISCHER VÀ ĐÓNG GÓI BIT ---
            if (currentTurn == Color.WHITE) {
                whiteTimeLeft += INCREMENT;
            } else {
                blackTimeLeft += INCREMENT;
            }

            // Đóng gói ngay để hàm AutoSave lưu đúng dữ liệu mới nhất
            this.secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
            // --- KẾT THÚC ---

            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);

            /* * [TRIGGER AUTO-SAVE]: Kích hoạt UC-04.1 (Tự động lưu ván đấu)
             * Chức năng: Đảm bảo tính bền vững dữ liệu ngay sau khi một nước đi hợp lệ được thực hiện xong.
             */
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
    /**
     * CHỨC NĂNG: UC-02.6: Update Game State (Cập nhật trạng thái trận đấu)
     * Mô tả: Đánh giá cục diện bàn cờ để phát hiện kịp thời các điều kiện kết thúc game.
     */
    private void checkGameState() {
        Color opponentColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean inCheck = board.isInCheck(opponentColor);
        boolean canMove = board.hasValidMoves(opponentColor);
        // CHỨC NĂNG: UC-02.6.3: Checkmate (Chiếu bí) -> Đối phương bị chiếu và không còn nước thoát
        if (inCheck && !canMove) {
            JOptionPane.showMessageDialog(view, "CHIẾU HẾT! " + (currentTurn == Color.WHITE ? "Trắng" : "Đen") + " thắng!");
        }
        // CHỨC NĂNG: UC-02.6.2: Stalemate (Hòa cờ) -> Đối phương không bị chiếu nhưng hết nước đi hợp lệ
        else if (!inCheck && !canMove) {
            JOptionPane.showMessageDialog(view, "HÒA CỜ (Stalemate)!");
        }
        // CHỨC NĂNG: UC-02.6.1: Check (Chiếu tướng) -> Vua đối phương đang nằm trong tầm ngắm của địch
        else if (inCheck) {
            JOptionPane.showMessageDialog(view, "Đang bị CHIẾU!");
        }
    }

    private void startTimer() {
        if (gameTimer != null) gameTimer.stop();

        gameTimer = new Timer(1000, e -> {
            if (!isPaused && !gameEnded) {
                if (currentTurn == Color.WHITE) whiteTimeLeft--;
                else blackTimeLeft--;

                // Đóng gói dữ liệu truyền đi cho Save Game
                this.secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);

                view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);

                if (whiteTimeLeft <= 0) handleTimeOut(Color.WHITE);
                else if (blackTimeLeft <= 0) handleTimeOut(Color.BLACK);
            }
        });

        gameTimer.start();
    }

    private void handleTimeOut(Color loser) {
        gameEnded = true;
        gameTimer.stop();

        SaveManager.deleteSaveFile();

        String winner = (loser == Color.WHITE) ? "Quân Đen" : "Quân Trắng";
        JOptionPane.showMessageDialog(view, "Hết giờ! " + winner + " giành chiến thắng.",
                "Kết thúc ván đấu", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * UC05: Pause/Resume
     * Chức năng: Dừng đồng hồ, vô hiệu hóa bàn cờ và hiển thị màn hình che.
     */
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

    /**
     * UC07: Resign Game
     * Chức năng: Xử lý người chơi đầu hàng, xác nhận hộp thoại và kết thúc.
     */
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

    public void setSecondsElapsed(int packedSeconds) {
        this.secondsElapsed = packedSeconds;

        this.whiteTimeLeft = (packedSeconds >> 16) & 0xFFFF;
        this.blackTimeLeft = packedSeconds & 0xFFFF;

        if (this.whiteTimeLeft == 0 && this.blackTimeLeft == 0) {
            this.whiteTimeLeft = BASE_TIME;
            this.blackTimeLeft = BASE_TIME;
        }

        view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
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
    public void restartGame() {
        this.board.reset();

        this.currentTurn = Color.WHITE;
        this.selectedPosition = null;
        this.gameEnded = false;
        this.isPaused = false;

        if (gameTimer != null) {
            gameTimer.stop();
        }
        this.whiteTimeLeft = BASE_TIME;
        this.blackTimeLeft = BASE_TIME;

        startTimer();

        undoStack.clear();
        redoStack.clear();

        view.resetBoardColors();
        view.updateBoardGUI();
        view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
        view.updatePauseButton(false);
    }
}