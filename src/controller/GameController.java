package controller;

import model.*;
import model.GameState;
import model.Piece;
import model.Position;
import view.*;

import javax.swing.JOptionPane;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import javax.swing.Timer;

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
    private int whiteUndoLeft = 3;
    private int blackUndoLeft = 3;
    private boolean playWithAI = false;
    private final Color aiColor = Color.BLACK;
    private boolean aiThinking = false;
    private final Random random = new Random();

    public GameController(Board board, GameWindow view) {
        this(board, view, false);
    }

    public GameController(Board board, GameWindow view, boolean playWithAI) {
        this.board = board;
        this.view = view;
        this.playWithAI = playWithAI;

        if (!playWithAI) {
            startTimer();
        }
    }
    public List<MoveLog> getMoveHistory() {
        return moveHistory;
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
            if (view != null) {
                view.resetBoardColors();
            }
            /**
             * CHỨC NĂNG: UC-02.3: Check Available Move (Kiểm tra nước đi có sẵn)
             * Mô tả: Hệ thống tự động tính toán luật đi của quân cờ để highlight các ô đích khả dụng.
             */
            if (!GameConfig.isAdvancedMode()) {
                if (view != null) {
                    view.highlightValidMoves(clicked, board);
                }
            }
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
            if (!GameConfig.isAdvancedMode()) {
                view.highlightValidMoves(clicked, board);
            }
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
        Piece movingPiece = board.get(selectedPosition);
        Piece targetPiece = board.get(destination);

        boolean moved = board.move(selectedPosition, destination);
        SoundManager.playMove();
        if (moved) {
            MoveLog log = new MoveLog(selectedPosition, destination, movingPiece, targetPiece, currentTurn);
            moveHistory.add(log);
            System.out.println("[LỊCH SỬ NƯỚC ĐI] " + log.getStandardNotation());

            undoStack.push(stateBefore);
            redoStack.clear();

            // AN TOÀN CHO TEST: Chỉ update giao diện nếu view khác null
            if (view != null) {
                view.updateBoardGUI();
            }

            checkGameState();

            // --- BẮT ĐẦU: CỘNG GIỜ FISCHER VÀ ĐÓNG GÓI BIT ---
            if (currentTurn == Color.WHITE) {
                whiteTimeLeft += INCREMENT;
            } else {
                blackTimeLeft += INCREMENT;
            }

            this.secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
            // --- KẾT THÚC ---

            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;

            if (view != null) {
                view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
                view.resetBoardColors();
            }
            /* * [TRIGGER AUTO-SAVE]: Kích hoạt UC-04.1 (Tự động lưu ván đấu)
             * Chức năng: Đảm bảo tính bền vững dữ liệu ngay sau khi một nước đi hợp lệ được thực hiện xong.
             */
            SaveLoadController.autoSave(currentTurn, secondsElapsed,moveHistory);
            selectedPosition = null;
            triggerAIMoveIfNeeded();
        } else {
            if (view != null) {
                String msg = board.isInCheck(currentTurn)
                        ? "Bạn đang bị chiếu! Hãy chọn nước đi bảo vệ Vua."
                        : "Nước đi không hợp lệ!";
                JOptionPane.showMessageDialog(view, msg, "Lỗi di chuyển", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void processMoveFrom(Position from, Position destination, boolean showInvalidMessage) {
        processMoveFrom(from, destination, null, showInvalidMessage);
    }

    private void processMoveFrom(Position from, Position destination, String promotionChoice, boolean showInvalidMessage) {
        if (from == null || destination == null || !from.isValid() || !destination.isValid()) {
            return;
        }

        GameState stateBefore = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        Piece movingPiece = board.get(from);
        Piece targetPiece = board.get(destination);

        boolean moved = board.move(from, destination, promotionChoice);
        if (moved) {
            SoundManager.playMove();
            MoveLog log = new MoveLog(from, destination, movingPiece, targetPiece, currentTurn);
            moveHistory.add(log);
            System.out.println("[Lá»ŠCH Sá»¬ NÆ¯á»šC ÄI] " + log.getStandardNotation());

            undoStack.push(stateBefore);
            redoStack.clear();

            if (view != null) {
                view.updateBoardGUI();
            }

            checkGameState();
            if (gameEnded) {
                selectedPosition = null;
                if (view != null) {
                    view.resetBoardColors();
                }
                return;
            }

            if (currentTurn == Color.WHITE) {
                whiteTimeLeft += INCREMENT;
            } else {
                blackTimeLeft += INCREMENT;
            }

            this.secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
            currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;

            if (view != null) {
                view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
                view.resetBoardColors();
            }

            SaveLoadController.autoSave(currentTurn, secondsElapsed, moveHistory);
            selectedPosition = null;
            triggerAIMoveIfNeeded();
        } else if (showInvalidMessage && view != null) {
            String msg = board.isInCheck(currentTurn)
                    ? "Báº¡n Ä‘ang bá»‹ chiáº¿u! HÃ£y chá»n nÆ°á»›c Ä‘i báº£o vá»‡ Vua."
                    : "NÆ°á»›c Ä‘i khÃ´ng há»£p lá»‡!";
            JOptionPane.showMessageDialog(view, msg, "Lá»—i di chuyá»ƒn", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkGameState() {
        Color opponentColor = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        boolean inCheck = board.isInCheck(opponentColor);
        boolean canMove = board.hasValidMoves(opponentColor);
        if (inCheck && !canMove) {
            gameEnded = true;
            gameTimer.stop();
            SaveManager.deleteSaveFile();
            String winner = (currentTurn == Color.WHITE) ? "Trắng" : "Đen";
            showGameOverDialog( "CHIẾU HẾT!\n" + winner + " thắng!");
        }
        else if (!inCheck && !canMove) {
            gameEnded = true;
            gameTimer.stop();
            SaveManager.deleteSaveFile();
            showGameOverDialog( "HÒA CỜ (Stalemate)!");
        }
        else if (inCheck) {
            if (!playWithAI || opponentColor != aiColor) {
                JOptionPane.showMessageDialog(view, "Đang bị CHIẾU!");
            }
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
            gameTimer.stop();
            SaveManager.deleteSaveFile();
            return;
        }

        processMoveFrom(bestMove.getFrom(), bestMove.getTo(), "Queen", false);
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
                        if (piece.isValidMove(from, to, board)
                                && !board.simulateMoveAndCheck(from, to, color)) {
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
        Piece capturedPiece = board.get(to);
        int score = random.nextInt(7);

        if (capturedPiece != null) {
            score += pieceValue(capturedPiece) * 10 - pieceValue(movingPiece);
        } else if (movingPiece instanceof Pawn
                && board.getEnPassantTarget() != null
                && board.getEnPassantTarget().equals(to)
                && from.getC() != to.getC()) {
            score += pieceValue(new Pawn(currentTurn)) * 10;
        }

        if (movingPiece instanceof Pawn && (to.getR() == 0 || to.getR() == 7)) {
            score += pieceValue(new Queen(movingPiece.getColor())) - pieceValue(movingPiece);
        }

        score += centerBonus(to);

        GameState snapshot = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        if (board.move(from, to, "Queen")) {
            Color opponentColor = aiColor == Color.WHITE ? Color.BLACK : Color.WHITE;
            score += evaluateBoardFor(aiColor);
            if (board.isInCheck(opponentColor)) {
                score += 35;
            }
            if (!board.hasValidMoves(opponentColor)) {
                score += board.isInCheck(opponentColor) ? 100000 : 0;
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

                // Đóng gói dữ liệu truyền đi cho Save Game
                this.secondsElapsed = (whiteTimeLeft << 16) | (blackTimeLeft & 0xFFFF);
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
        gameTimer.stop();

        SaveManager.deleteSaveFile();

        String winner = (loser == Color.WHITE) ? "Quân Đen" : "Quân Trắng";
        showGameOverDialog( "Hết giờ!\n" + winner + " giành chiến thắng.");
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
            gameTimer.stop();

            selectedPosition = null;
            if (view != null) {
                view.resetBoardColors();
            }
        } else {
            /* * MÃ USE CASE: UC-05.2.1 & UC-05.2.3 (Luồng Resume)
             * Mô tả: Kích hoạt lại updateStateToPlaying. Tiếp tục luồng chạy của Timer.
             */
            try {
                gameTimer.start();
            } catch (Exception e) {
                e.printStackTrace(); // Xử lý lỗi A1
            }
            gameTimer.start();
        }
        if (view != null) {
            view.updatePauseButton(isPaused);
        }
        triggerAIMoveIfNeeded();
    }

    public void handleSquareClick(int row, int col) {
        /*
         * MÃ USE CASE: UC-05.1.3 (Vô hiệu hóa tương tác khi Pause)
         * Mô tả: Kiểm tra cờ isPaused. Nếu true (đang tạm dừng), mọi sự kiện click
         * chuột vào ô cờ sẽ bị bỏ qua để ngăn chặn đi quân gian lận.
         */
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

    public boolean canStartDrag(int row, int col) {
        if (isPaused || gameEnded || isAITurn() || aiThinking) {
            return false;
        }

        Position position = new Position(row, col);
        if (!position.isValid()) {
            return false;
        }

        Piece piece = board.get(position);
        return piece != null && piece.getColor() == currentTurn;
    }

    public void previewDragFrom(int row, int col) {
        if (!canStartDrag(row, col) || view == null) {
            return;
        }

        Position from = new Position(row, col);
        view.resetBoardColors();
        view.highlightValidMoves(from, board);
    }

    public void handleDragDrop(int fromRow, int fromCol, int toRow, int toCol) {
        if (isPaused || gameEnded || isAITurn() || aiThinking) {
            return;
        }

        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);
        if (!from.isValid() || !to.isValid()) {
            selectedPosition = null;
            if (view != null) {
                view.resetBoardColors();
                view.updateBoardGUI();
            }
            return;
        }

        Piece movingPiece = board.get(from);
        if (movingPiece == null || movingPiece.getColor() != currentTurn) {
            selectedPosition = null;
            if (view != null) {
                view.resetBoardColors();
                view.updateBoardGUI();
            }
            return;
        }

        selectedPosition = from;
        processMoveFrom(from, to, true);
        selectedPosition = null;
        if (view != null) {
            view.resetBoardColors();
            view.updateBoardGUI();
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
            showGameOverDialog( winner + " thắng do đối thủ đầu hàng!" );
            /*
             * Luồng thay thế A1 (UC-07.7) & A2:
             * Nếu chọn "No" hoặc tắt cửa sổ (choice != YES_OPTION), hàm sẽ thoát tại đây.
             * Hệ thống coi như hủy lệnh đầu hàng, không can thiệp vào gameEnded hay gameTimer. Trận đấu tiếp diễn.
             */
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
        }    }

    public void undo() {
        // [UC-UNDO - Pre-Conditions & Alternate Flow A1] Kiểm tra điều kiện hoặc stack rỗng
        if (isPaused || gameEnded || isAITurn() || aiThinking || undoStack.isEmpty()) return;

        // [UC-UNDO - Alternate Flow A2] Kiểm tra giới hạn số lần Undo của từng hệ màu (Tối đa 3 lần/ván)
        if (currentTurn == Color.BLACK) {
            if (whiteUndoLeft <= 0) {
                JOptionPane.showMessageDialog(view, "Quân TRẮNG đã hết lượt Đi Lại (Tối đa 3 lần)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            whiteUndoLeft--;
            System.out.println("[SYSTEM] Trắng vừa dùng 1 lần Undo. Còn lại: " + whiteUndoLeft);
        } else { // Lượt hiện tại là Trắng -> nước cờ trước đó của Đen, trừ lượt Đen
            if (blackUndoLeft <= 0) {
                JOptionPane.showMessageDialog(view, "Quân ĐEN đã hết lượt Đi Lại (Tối đa 3 lần)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            blackUndoLeft--;
            System.out.println("[SYSTEM] Đen vừa dùng 1 lần Undo. Còn lại: " + blackUndoLeft);
        }

        // [UC-UNDO - Basic Flow - Bước 1] Khởi tạo lại trạng thái lựa chọn trên UI
        this.selectedPosition = null;
        if (view != null) {
            view.resetBoardColors();
        }

        // [UC-UNDO - Basic Flow - Bước 9] Đưa nước đi hiện tại vào redo stack trước khi lùi lại
        GameState currentState = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        redoStack.push(currentState);

        // [UC-UNDO - Basic Flow - Bước 4] Lấy trạng thái gần nhất từ history stack (undoStack)
        GameState previousState = undoStack.pop();

        // [UC-UNDO - Basic Flow - Bước 5 & 6] Hoàn tác vị trí quân cờ về trạng thái cũ
        previousState.restore(board);

        // [UC-UNDO - Basic Flow - Bước 7] Chuyển lượt chơi về người đi trước
        this.currentTurn = previousState.getTurn();

        // [UC-UNDO - Post-Conditions] Khôi phục lại chính xác thời gian của trạng thái cũ trước khi di chuyển
        this.whiteTimeLeft = previousState.getWhiteTimeLeft();
        this.blackTimeLeft = previousState.getBlackTimeLeft();

        // --- BẮT ĐẦU: [UC-UNDO - Basic Flow - Bước 8] Trừ 10 giây thời gian của người yêu cầu Undo ---
        if (this.currentTurn == Color.WHITE) {
            this.whiteTimeLeft -= 10;
            if (this.whiteTimeLeft < 0) this.whiteTimeLeft = 0;
        } else {
            this.blackTimeLeft -= 10;
            if (this.blackTimeLeft < 0) this.blackTimeLeft = 0;
        }
        // Đóng gói lại dữ liệu Bit của bộ đếm thời gian
        this.secondsElapsed = (this.whiteTimeLeft << 16) | (this.blackTimeLeft & 0xFFFF);
        // --- KẾT THÚC ---

        // [UC-UNDO - Basic Flow - Bước 10] Cập nhật giao diện bộ đếm thời gian và bàn cờ
        if (view != null) {
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
            view.updateBoardGUI();
        }

        // [TRIGGER AUTO-SAVE]: Đồng bộ tệp tự động lưu sau khi tiến hành Undo
        SaveLoadController.autoSave(currentTurn,secondsElapsed,moveHistory);
    }
    public void redo() {
        // [UC-REDO - Pre-Conditions & Alternate Flow A1] Kiểm tra điều kiện hoặc redo stack rỗng
        if (isPaused || gameEnded || isAITurn() || aiThinking || redoStack.isEmpty()) return;

        // [UC-REDO - Basic Flow - Bước 1] Reset trạng thái click chọn cũ trên UI
        this.selectedPosition = null;
        if (view != null) {
            view.resetBoardColors();
        }

        // [UC-REDO - Basic Flow - Bước 8] Đưa trạng thái hiện tại ngược vào history stack (undoStack)
        GameState currentState = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        undoStack.push(currentState);

        // [UC-REDO - Basic Flow - Bước 3] Lấy nước đi kế tiếp từ trong redo stack
        GameState nextState = redoStack.pop();

        // [UC-REDO - Basic Flow - Bước 4, 5, 6] Cập nhật lại vị trí các quân cờ lên bàn cờ
        nextState.restore(board);

        // [UC-REDO - Basic Flow - Bước 7] Chuyển lượt chơi sang người chơi tiếp theo
        this.currentTurn = nextState.getTurn();

        // [UC-REDO - Basic Flow - Bước 9] Khôi phục lại mạch thời gian chuẩn xác của nước đi kế tiếp
        this.whiteTimeLeft = nextState.getWhiteTimeLeft();
        this.blackTimeLeft = nextState.getBlackTimeLeft();
        this.secondsElapsed = (this.whiteTimeLeft << 16) | (this.blackTimeLeft & 0xFFFF);

        // [UC-REDO - Basic Flow - Bước 10] Cập nhật lại giao diện hiển thị bàn cờ và đồng hồ
        if (view != null) {
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
            view.updateBoardGUI();
        }

        // [TRIGGER AUTO-SAVE]: Đồng bộ dữ liệu tệp lưu tự động sau khi Redo thành công
        SaveLoadController.autoSave(currentTurn, secondsElapsed,moveHistory);

    }
    public void replayMoveForLoad(Position from, Position to) {
        GameState stateBefore = new GameState(board, currentTurn, whiteTimeLeft, blackTimeLeft);
        undoStack.push(stateBefore);
        board.move(from, to);
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        moveHistory.clear();
    }
    public void restartGame() {
        this.board.reset();

        this.currentTurn = Color.WHITE;
        this.selectedPosition = null;
        this.gameEnded = false;
        this.isPaused = false;
        this.aiThinking = false;
        this.whiteUndoLeft = 3;
        this.blackUndoLeft = 3;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        this.whiteTimeLeft = BASE_TIME;
        this.blackTimeLeft = BASE_TIME;

        startTimer();

        undoStack.clear();
        redoStack.clear();
        if (view != null) {
            view.resetBoardColors();
            view.updateBoardGUI();
            view.updateTimer(whiteTimeLeft, blackTimeLeft, currentTurn);
            view.updatePauseButton(false);
        }
        triggerAIMoveIfNeeded();
    }
    private void showGameOverDialog(String message) {

        String[] options = {
                "Chơi Ván Mới",
                "Quay Lại Menu"
        };

        int choice = JOptionPane.showOptionDialog(
                view,
                message,
                "Kết thúc ván đấu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            restartGame();
        } else if (choice == 1) {
            view.dispose();
            new StartWindow();
        }
    }
}
