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
        GameState stateBefore = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
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
     * MÃ USE CASE: UC-05.1 và UC-05.2 (Pause/Resume Game)
     * Chức năng: Xử lý thay đổi trạng thái Tạm dừng / Tiếp tục của trận đấu.
     * Thỏa mãn SR1: Thao tác chuyển đổi trạng thái phải diễn ra lập tức (<0.1 giây).
     */
    public void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            /* * MÃ USE CASE: UC-05.1.1 & UC-05.1.2 (Luồng Pause)
             * Mô tả: Gọi hàm đóng băng luồng đếm giờ và đổi trạng thái sang Paused.
             */
            try {
                gameTimer.stop();
            } catch (Exception e) {
                /* MÃ USE CASE: A1 (UC-05.1.5) - Luồng thay thế: Bắt lỗi bất định của Timer */
                e.printStackTrace();
            }

            selectedPosition = null;
            view.resetBoardColors();

        } else {
            /* * MÃ USE CASE: UC-05.2.1 & UC-05.2.3 (Luồng Resume)
             * Mô tả: Kích hoạt lại updateStateToPlaying. Tiếp tục luồng chạy của Timer.
             */
            try {
                gameTimer.start();
            } catch (Exception e) {
                e.printStackTrace(); // Xử lý lỗi A1
            }
        }
        view.updatePauseButton(isPaused);
    }

    public void handleSquareClick(int row, int col) {
        /*
         * MÃ USE CASE: UC-05.1.3 (Vô hiệu hóa tương tác khi Pause)
         * Mô tả: Kiểm tra cờ isPaused. Nếu true (đang tạm dừng), mọi sự kiện click
         * chuột vào ô cờ sẽ bị bỏ qua để ngăn chặn đi quân gian lận.
         */
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
     * UC-07: Resign Game (Đầu hàng)
     * Chức năng: Xử lý người chơi đầu hàng, xác nhận hộp thoại và kết thúc.
     */
    public void resignGame() {
        // [Pre-Conditions]: Trò chơi phải đang trong trạng thái diễn ra
        if (gameEnded) {
            return;
        }

        String loser =
                (currentTurn == Color.WHITE)
                        ? "Trắng"
                        : "Đen";

        // (UC-07.4): Hệ thống tự động tính toán xác lập trạng thái kết quả (Đối thủ được xử thắng)
        String winner =
                (currentTurn == Color.WHITE)
                        ? "Đen"
                        : "Trắng";

        /*
         * (UC-07.1) & SR1: Hệ thống hiển thị hộp thoại yêu cầu xác nhận.
         * Nút "Yes" và "No" tách biệt rõ ràng nhờ cấu trúc tiêu chuẩn của JOptionPane.
         */
        int choice = JOptionPane.showConfirmDialog(
                view,
                loser + " muốn đầu hàng?",
                "Xác nhận đầu hàng",
                JOptionPane.YES_NO_OPTION
        );

        // (UC-07.2): Người chơi nhấp chọn nút "Yes" để chính thức đầu hàng
        if (choice == JOptionPane.YES_OPTION) {

            /*
             * (UC-07.3) & SR2: Kích hoạt luồng "End game", khóa hoàn toàn bàn cờ.
             * Tước bỏ quyền đi quân nhằm ngăn chặn mọi hành vi thay đổi thế cờ.
             */
            gameEnded = true;

            // (UC-07.3) & SR2: Dừng tất cả các bộ đếm thời gian của hai bên.
            gameTimer.stop();

            /*
             * SR3: Đảm bảo tính toàn vẹn dữ liệu.
             * File savegame.txt bị xóa ngay lập tức để trận đấu kết thúc hoàn toàn.
             */
            SaveManager.deleteSaveFile();

            /*
             * (UC-07.5): Hệ thống bật pop-up thông báo tên người thắng cuộc kèm nguyên nhân kết thúc.
             * Ghi chú (UC-07.6): Sau khi bấm OK, bàn đấu giữ nguyên trạng thái đóng băng để người chơi nhìn lại, người chơi có thể tự thao tác "Quay lại Menu" hoặc "Chơi Game Mới" thông qua Menu điều khiển.
             */
            JOptionPane.showMessageDialog(
                    view,
                    winner + " thắng do đối thủ đầu hàng!"
            );

            /*
             * Luồng thay thế A1 (UC-07.7) & A2:
             * Nếu chọn "No" hoặc tắt cửa sổ (choice != YES_OPTION), hàm sẽ thoát tại đây.
             * Hệ thống coi như hủy lệnh đầu hàng, không can thiệp vào gameEnded hay gameTimer. Trận đấu tiếp diễn.
             */
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
        GameState currentState = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        redoStack.push(currentState);

        GameState previousState = undoStack.pop();
        previousState.restore(board);
        this.currentTurn = previousState.getTurn();
        this.whiteTimeLeft = previousState.getWhiteTimeLeft();
        this.blackTimeLeft = previousState.getBlackTimeLeft();

        this.secondsElapsed = (this.whiteTimeLeft << 16) | (this.blackTimeLeft & 0xFFFF);
        view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);

        view.updateBoardGUI();
        selectedPosition = null;
        view.resetBoardColors();
        SaveLoadController.autoSave(currentTurn, board, secondsElapsed);
    }

    public void redo() {
        if (isPaused || gameEnded || redoStack.isEmpty()) return;
        GameState currentState = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        undoStack.push(currentState);

        GameState nextState = redoStack.pop();
        nextState.restore(board);
        this.currentTurn = nextState.getTurn();
        this.whiteTimeLeft = nextState.getWhiteTimeLeft();
        this.blackTimeLeft = nextState.getBlackTimeLeft();
        this.secondsElapsed = (this.whiteTimeLeft << 16) | (this.blackTimeLeft & 0xFFFF);
        view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
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