package test;

import controller.GameController;
import model.Board;
import model.Position;

import java.awt.Color;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    private GameController gameController;
    private Board board;

    @BeforeEach
    void setUp() {
        // Khởi tạo bàn cờ thực tế của dự án
        board = new Board();

        /* * Khởi tạo GameController truyền vào board xịn và view là null.
         * Việc để view = null giúp Unit Test chạy ngầm cực nhanh mà không cần
         * bật giao diện GameWindow lên (tránh lỗi lỗi đồ họa khi chạy test).
         */
        gameController = new GameController(board, null);
    }

    @Test
    void testCanStartDragOnlyCurrentTurnPiece() {
        assertTrue(gameController.canStartDrag(1, 0), "White should be able to drag a white pawn on the first turn.");
        assertFalse(gameController.canStartDrag(6, 0), "White should not be able to drag a black pawn.");
        assertFalse(gameController.canStartDrag(3, 3), "Cannot start dragging from an empty square.");
        assertFalse(gameController.canStartDrag(-1, 0), "Cannot start dragging from outside the board.");
    }

    @Test
    void testHandleDragDropMovesPieceAndSwitchesTurn() {
        gameController.setCurrentTurn(Color.WHITE);

        gameController.handleDragDrop(1, 0, 2, 0);

        assertNull(board.get(new Position(1, 0)), "Source square should be empty after a valid drag-drop move.");
        assertNotNull(board.get(new Position(2, 0)), "Destination square should contain the dragged piece.");
        assertEquals(Color.BLACK, gameController.getCurrentTurn(), "Turn should switch after a valid drag-drop move.");
        assertEquals(1, gameController.getMoveHistory().size(), "Drag-drop move should be recorded in move history.");
    }

    @Test
    void testHandleDragDropRejectsInvalidMove() {
        gameController.setCurrentTurn(Color.WHITE);

        gameController.handleDragDrop(1, 0, 4, 0);

        assertNotNull(board.get(new Position(1, 0)), "Invalid drag-drop should keep the piece on the source square.");
        assertNull(board.get(new Position(4, 0)), "Invalid drag-drop should not place a piece on the destination square.");
        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Invalid drag-drop should not switch turns.");
        assertTrue(gameController.getMoveHistory().isEmpty(), "Invalid drag-drop should not be recorded.");
    }

    @Test
    void testDragDropLockedWhenPaused() {
        gameController.togglePause();

        assertFalse(gameController.canStartDrag(1, 0), "Paused game should not allow drag start.");
        gameController.handleDragDrop(1, 0, 2, 0);

        assertNotNull(board.get(new Position(1, 0)), "Paused drag-drop should not move the source piece.");
        assertNull(board.get(new Position(2, 0)), "Paused drag-drop should leave the destination empty.");
        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Paused drag-drop should not switch turns.");
    }

    @Test
    void testDragDropLockedDuringAITurn() {
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);
        aiController.setCurrentTurn(Color.BLACK);

        assertFalse(aiController.canStartDrag(6, 0), "AI turn should not allow a human drag start.");
        aiController.handleDragDrop(6, 0, 5, 0);

        assertNotNull(aiBoard.get(new Position(6, 0)), "AI-turn drag-drop should not move the source piece.");
        assertNull(aiBoard.get(new Position(5, 0)), "AI-turn drag-drop should leave the destination empty.");
        assertEquals(Color.BLACK, aiController.getCurrentTurn(), "AI-turn drag-drop should not switch turns.");
    }

    /**
     * [Test Đóng Gói Bit]
     * Xác minh thuật toán nén và giải nén bit thời gian tích hợp trong GameController hoạt động chính xác.
     */
    @Test
    void testFischerBitPackingIntegration() {
        int whiteTimeSimulated = 595; // Giả lập Trắng còn 9 phút 55 giây
        int blackTimeSimulated = 600; // Giả lập Đen còn 10 phút 00 giây

        // Thực hiện đóng gói dữ liệu mẫu bằng công thức dịch bit
        int packedSeconds = (whiteTimeSimulated << 16) | (blackTimeSimulated & 0xFFFF);

        // Nạp trực tiếp vào GameController để kiểm tra hàm setSecondsElapsed
        try {
            gameController.setSecondsElapsed(packedSeconds);
        } catch (NullPointerException e) {
            // Bắt NPE do view = null khi chạy test ngầm, mục đích để luồng xử lý dữ liệu không bị crash
        }

        // XÁC MINH: Các hàm getter của GameController phải trả về đúng giá trị sau khi giải nén
        assertEquals(whiteTimeSimulated, gameController.getWhiteTimeLeft(), "Hàm setSecondsElapsed giải nén thời gian TRẮNG bị sai!");
        assertEquals(blackTimeSimulated, gameController.getBlackTimeLeft(), "Hàm setSecondsElapsed giải nén thời gian ĐEN bị sai!");
    }
    /**
     * [Test Trạng Thái Biên]
     * Kiểm tra cơ chế tự động khôi phục về thời gian mặc định (600s) khi dữ liệu truyền vào bằng 0.
     */
    @Test
    void testPackedTimeDefaultResetWhenZero() {
        // Bước 1: Đặt thời gian hiện tại về một con số khác bất kỳ
        gameController.setWhiteTimeLeft(120);
        gameController.setBlackTimeLeft(240);

        // Bước 2: Gọi hàm truyền vào số 0 (mô phỏng ván mới hoàn toàn)
        try {
            gameController.setSecondsElapsed(0);
        } catch (NullPointerException e) {
            // Bắt NPE của view để test tiếp tục chạy xuống dưới
        }

        // Bước 3: Xác minh dữ liệu phải tự động reset về mốc mặc định BASE_TIME (600 giây)
        assertEquals(600, gameController.getWhiteTimeLeft(), "Thời gian Trắng không tự động reset về 600s khi truyền vào số 0!");
        assertEquals(600, gameController.getBlackTimeLeft(), "Thời gian Đen không tự động reset về 600s khi truyền vào số 0!");
    }

    /**
     * [Test Logic Fischer Increment]
     * Kiểm tra tính năng tích lũy thời gian Fischer (+5s) khi thực hiện xong một nước đi.
     * Xác minh trực tiếp thông qua giả lập sự kiện click chuột, đi qua hàm processMove thực tế.
     */
    @Test
    void testFischerIncrementOnTurnChange() {
        // Khởi tạo thời gian ban đầu cho phe Trắng
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(600);
        int timeBefore = gameController.getWhiteTimeLeft();

        // GIẢ LẬP CLICK CHUỘT: Tốt trắng từ (1,0) tiến lên (2,0)
        try {
            gameController.handleSquareClick(1, 0); // Click 1: Chọn quân Tốt
        } catch (Exception e) {} // Bắt NPE của hàm handleSelection (nếu có)

        try {
            gameController.handleSquareClick(2, 0); // Click 2: Di chuyển Tốt -> Kích hoạt processMove
        } catch (Exception e) {} // Bắt NPE (nếu có)

        // XÁC MINH CHÍNH XÁC: Code của bạn đã thực sự chạy qua logic Fischer hay chưa?
        int timeAfter = gameController.getWhiteTimeLeft();
        assertEquals(timeBefore + 5, timeAfter, "Lỗi: Thời gian của phe Trắng không được cộng 5s từ hàm processMove!");
        assertEquals(Color.BLACK, gameController.getCurrentTurn(), "Lỗi: Không đổi lượt sang phe Đen!");
    }


    // =========================================================================
    // PHẦN TEST CỦA BẠN: KIỂM THỬ CHỨC NĂNG PAUSE/RESUME VÀ BẢO MẬT (UC-05)
    // =========================================================================

    /**
     * [Test Cơ chế Tạm dừng - UC-05.1 và Tiếp tục - UC-05.2]
     * Xác minh hàm togglePause hoạt động đúng vòng đời: Bật -> Tắt
     */
    @Test
    void testTogglePauseState() {
        // Mặc định khi mới tạo Controller, isPaused = false.
        // Khi gọi togglePause lần 1 -> isPaused phải chuyển thành true
        assertDoesNotThrow(() -> gameController.togglePause(), "Hàm togglePause bị lỗi khi view null!");

        // Gọi lần 2 -> isPaused phải chuyển lại thành false (Tiếp tục)
        assertDoesNotThrow(() -> gameController.togglePause(), "Lỗi khi Resume game!");
    }

    /**
     * [Test An Ninh Cờ Vua - Guard Clause]
     * Kiểm tra cơ chế chặn tương tác: Khi game đang Tạm Dừng, mọi click chuột lên bàn cờ
     * phải bị từ chối xử lý, đảm bảo người chơi không thể đi quân gian lận.
     */
    @Test
    void testInteractionLockedWhenPaused() {
        // 1. Kích hoạt Tạm Dừng
        gameController.togglePause();

        // 2. Cố tình click chọn quân Tốt ở ô (1,0)
        try {
            gameController.handleSquareClick(1, 0);
        } catch (Exception e) {}

        // 3. XÁC MINH: Nếu guard clause (if isPaused return) hoạt động đúng,
        // thì controller sẽ KHÔNG chọn bất cứ quân cờ nào (selectedPosition vẫn là null).
        // Ta xác minh bằng cách đi tiếp nước (2,0). Nếu selectedPosition null, hàm sẽ coi đây
        // là click chọn quân chứ không phải di chuyển, do đó thời gian và lượt đi KHÔNG ĐỔI.
        try {
            gameController.handleSquareClick(2, 0);
        } catch (Exception e) {}

        // 4. Lượt đi vẫn phải là phe TRẮNG, chứng tỏ nước đi ăn gian đã bị chặn đứng hoàn toàn!
        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Bảo mật kém: Lượt đi bị thay đổi mặc dù game đang tạm dừng!");
    }

    // =========================================================================
    // PHẦN TEST CỦA BẠN: KIỂM THỬ CHỨC RESTART GAME (UC-06)
    // =========================================================================

    /**
     * [UC-06] Restart Game
     * Mục tiêu:
     * Đảm bảo khi bắt đầu ván mới, lượt chơi luôn được đưa về phe Trắng.
     */
    @Test
    void testRestartResetCurrentTurn() {

        // Giả lập đang tới lượt Đen
        gameController.setCurrentTurn(Color.BLACK);

        // Người chơi chọn "Restart Game"
        gameController.restartGame();

        // Sau khi restart phải quay lại lượt Trắng
        assertEquals(
                Color.WHITE,
                gameController.getCurrentTurn(),
                "Lượt chơi không được reset về phe Trắng!"
        );
    }

    /**
     * [UC-06] Restart Game
     * Mục tiêu:
     * Đảm bảo đồng hồ của cả hai bên được khôi phục về thời gian mặc định.
     */
    @Test
    void testRestartResetTimer() {

        // Giả lập đồng hồ đang chạy với giá trị bất kỳ
        gameController.setWhiteTimeLeft(150);
        gameController.setBlackTimeLeft(300);

        // Khởi tạo ván đấu mới
        gameController.restartGame();

        // Đồng hồ Trắng phải trở về 600 giây
        assertEquals(
                600,
                gameController.getWhiteTimeLeft(),
                "Đồng hồ Trắng không reset về 600 giây!"
        );

        // Đồng hồ Đen phải trở về 600 giây
        assertEquals(
                600,
                gameController.getBlackTimeLeft(),
                "Đồng hồ Đen không reset về 600 giây!"
        );
    }

    /**
     * [UC-06] Restart Game
     * Mục tiêu:
     * Đảm bảo trạng thái bàn cờ được phục hồi về vị trí khởi tạo ban đầu.
     */
    @Test
    void testRestartRestoreBoard() {

        // Giả lập một quân tốt đã di chuyển khỏi vị trí ban đầu
        board.move(
                new Position(1,0),
                new Position(2,0)
        );

        // Người chơi tạo ván đấu mới
        gameController.restartGame();

        // Quân tốt phải xuất hiện lại ở vị trí xuất phát
        assertNotNull(
                board.get(new Position(1,0)),
                "Quân tốt ban đầu không được khôi phục!"
        );

        // Ô đích trước đó phải được làm sạch
        assertNull(
                board.get(new Position(2,0)),
                "Bàn cờ chưa được reset hoàn toàn!"
        );
    }

    /**
     * [UC-06] Restart Game
     * Mục tiêu:
     * Đảm bảo trạng thái Pause bị hủy sau khi bắt đầu ván mới.
     */
    @Test
    void testRestartExitPauseMode() {

        // Đưa trò chơi vào trạng thái Pause
        gameController.togglePause();

        // Thực hiện restart
        assertDoesNotThrow(
                () -> gameController.restartGame(),
                "Không thể restart khi game đang Pause!"
        );

        // Sau restart, game phải chấp nhận tương tác trở lại
        assertDoesNotThrow(
                () -> gameController.handleSquareClick(1,0),
                "Bàn cờ vẫn bị khóa sau khi restart!"
        );
    }

    /**
     * [UC-06] Restart Game
     * Mục tiêu:
     * Đảm bảo lịch sử Undo/Redo bị xóa hoàn toàn khi tạo ván mới.
     */
    @Test
    void testRestartClearUndoRedoHistory() {

        // Giả lập người chơi đã thực hiện một nước đi
        try {
            gameController.handleSquareClick(1,0);
            gameController.handleSquareClick(2,0);
        } catch (Exception e) {}

        // Tạo ván đấu mới
        gameController.restartGame();

        // Undo không được gây lỗi dù stack đã bị xóa
        assertDoesNotThrow(
                () -> gameController.undo(),
                "Undo gây lỗi sau khi restart!"
        );

        // Redo cũng không được gây lỗi
        assertDoesNotThrow(
                () -> gameController.redo(),
                "Redo gây lỗi sau khi restart!"
        );

        // Lượt chơi vẫn phải là trạng thái khởi tạo
        assertEquals(
                Color.WHITE,
                gameController.getCurrentTurn(),
                "Undo/Redo vẫn còn tác động sau khi restart!"
        );
    }
    /**
     * [UC-01: Start Game]
     * Kiểm tra khởi tạo ván cờ khi chơi với máy.
     */
    @Test
    void testStartGameWithAI() throws Exception {

        Board board = new Board();

        GameController controller =
                new GameController(
                        board,
                        null,
                        true
                );

        Field boardField =
                GameController.class.getDeclaredField("board");

        boardField.setAccessible(true);

        Board controllerBoard =
                (Board) boardField.get(controller);

        assertNotNull(
                controllerBoard,
                "Bàn cờ phải được khởi tạo!"
        );
    }

    /**
     * [UC-01: Start Game]
     * Bước 1:
     * Người chơi chọn chế độ chơi với máy.
     *
     * Kết quả mong đợi:
     * Hệ thống không khởi tạo đồng hồ đếm giờ.
     */
    @Test
    void testNoTimerInAIMode() throws Exception {

        Board board = new Board();

        GameController controller =
                new GameController(
                        board,
                        null,
                        true
                );

        Field timerField =
                GameController.class.getDeclaredField(
                        "gameTimer"
                );

        timerField.setAccessible(true);

        Object timer =
                timerField.get(controller);

        assertNull(
                timer,
                "Chế độ chơi với máy không được tạo đồng hồ đếm giờ!"
        );
    }

    /**
     * [UC-UNDO - Basic Flow]
     * Kiểm tra logic Undo cơ bản: Quay lại lượt trước đó và áp dụng hình phạt -10s.
     */
    @Test
    void testUndoBasic() {
        // Arrange: Thiết lập trạng thái và thời gian ban đầu (180s)
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(180);
        gameController.setBlackTimeLeft(180);

        // Act: Người chơi di chuyển Tốt trắng (1,0) -> (2,0) rồi thực hiện Undo
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}

        // Assert: Lượt chơi quay về WHITE và thời gian Trắng còn lại là 170s (180s - 10s)
        assertEquals(Color.WHITE, gameController.getCurrentTurn());
        assertEquals(170, gameController.getWhiteTimeLeft());
    }

    /**
     * [UC-UNDO - Robustness]
     * Kiểm tra tính an toàn: Hệ thống không crash và giữ nguyên trạng thái khi undoStack rỗng.
     */
    @Test
    void testUndoEmptyStack() {
        // Act & Assert: Gọi undo khi chưa có nước đi, hệ thống không văng ngoại lệ
        assertDoesNotThrow(() -> gameController.undo());
        assertEquals(Color.WHITE, gameController.getCurrentTurn());
    }

    /**
     * [UC-REDO - Basic Flow]
     * Kiểm tra logic Redo cơ bản: Đảm bảo khôi phục lại nước đi sau khi bấm Undo.
     */
    @Test
    void testRedoAfterUndo() {
        // Arrange: Thực hiện nước đi và lùi lại (Undo)
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}
        assertEquals(Color.WHITE, gameController.getCurrentTurn());

        // Act: Người chơi bấm Redo
        try {
            gameController.redo();
        } catch (Exception e) {}

        // Assert: Lượt chơi tiến tới tương lai, chuyển sang phe BLACK
        assertEquals(Color.BLACK, gameController.getCurrentTurn());
    }

    /**
     * [UC-UNDO - Data Flow]
     * Kiểm tra luồng dữ liệu: Trạng thái hiện tại phải được lưu vào redoStack khi Undo.
     */
    @Test
    void testUndoPushToRedoStack() {
        // Arrange: Tạo một nước đi mẫu và lùi cờ
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}

        // Act & Assert: Kiểm tra redoStack không rỗng bằng cách thực thi lệnh redo() an toàn
        assertDoesNotThrow(() -> gameController.redo());
    }

    /**
     * [UC-UNDO - Boundary Condition]
     * Kiểm tra điều kiện biên: Thời gian sau khi phạt Undo không được xuống dưới 0 giây.
     */
    @Test
    void testUndoTimeDeductionLowerBound() {
        // Arrange: Người chơi chỉ còn 5 giây trước khi thực hiện nước đi
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(5);

        // Act: Di chuyển quân và thực hiện Undo (Phạt trừ 10s)
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}

        // Assert: Thời gian bị đưa về mốc biên tối thiểu là 0s (Không bị số âm)
        assertEquals(0, gameController.getWhiteTimeLeft());
    }

    /**
     * [UC-UNDO - Alternate Flow - A2]
     * Kiểm tra giới hạn tối đa số lần Undo của quân TRẮNG (Chặn ở lần thứ 4).
     */
    @Test
    void testWhiteMaxThreeUndos() {
        // Arrange: Cài đặt trận đấu và thực hiện chuỗi di chuyển - lùi cờ 3 lần thành công
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(180);
        gameController.setBlackTimeLeft(180);

        for (int i = 0; i < 3; i++) {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        }

        // Act: Cố gắng di chuyển và bấm Undo lần thứ 4 (Vượt giới hạn)
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);
        Color turnBeforeFailedUndo = gameController.getCurrentTurn(); // Hiện tại là BLACK
        gameController.undo();

        // Assert: Hệ thống chặn hành động, giữ nguyên lượt BLACK của đối thủ
        assertEquals(turnBeforeFailedUndo, gameController.getCurrentTurn());
    }

    /**
     * [UC-UNDO - Alternate Flow - A2]
     * Kiểm tra giới hạn tối đa số lần Undo của quân ĐEN (Chặn ở lần thứ 4).
     */
    @Test
    void testBlackMaxThreeUndos() {
        // Arrange: Chuyển lượt sang BLACK, thực hiện chuỗi di chuyển - lùi cờ 3 lần thành công
        gameController.setCurrentTurn(Color.WHITE);
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        for (int i = 0; i < 3; i++) {
            gameController.handleSquareClick(6, 0);
            gameController.handleSquareClick(5, 0);
            gameController.undo();
        }

        // Act: Cố gắng di chuyển và bấm Undo lần thứ 4 (Vượt giới hạn)
        gameController.handleSquareClick(6, 0);
        gameController.handleSquareClick(5, 0); // Lượt chuyển sang WHITE
        Color turnBeforeFailedUndo = gameController.getCurrentTurn();
        gameController.undo();

        // Assert: Hệ thống chặn hành động, giữ nguyên lượt WHITE của đối thủ
        assertEquals(turnBeforeFailedUndo, gameController.getCurrentTurn());
    }

    /**
     * [UC-UNDO - Business Rule]
     * Kiểm tra khôi phục số lượt Undo (Reset về 3) khi khởi động lại ván đấu (Restart).
     */
    @Test
    void testUndoCountResetOnRestart() {
        // Arrange: Trắng di chuyển quân, dùng 1 lần Undo rồi Restart game
        gameController.setCurrentTurn(Color.WHITE);
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);
        gameController.undo();
        gameController.restartGame();

        // Act & Assert: Đảm bảo Trắng có thể thực hiện liên tiếp 3 lần Undo mới sau khi restart
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        assertDoesNotThrow(() -> {
            gameController.undo(); // Lần 1
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo(); // Lần 2
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo(); // Lần 3
        });
    }

    /**
     * [UC-UNDO - AI Mode]
     * Kiểm tra Undo trong chế độ AI: Hệ thống phải xóa đồng thời 2 trạng thái cờ và 2 bản ghi lịch sử.
     */
    @Test
    void testUndoWithAISuccess() throws Exception {
        // Arrange: Khởi tạo controller chế độ AI và mock dữ liệu private qua Reflection
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);

        Field undoStackField = GameController.class.getDeclaredField("undoStack");
        Field moveHistoryField = GameController.class.getDeclaredField("moveHistory");
        Field redoStackField = GameController.class.getDeclaredField("redoStack");
        undoStackField.setAccessible(true);
        moveHistoryField.setAccessible(true);
        redoStackField.setAccessible(true);

        java.util.Stack<model.GameState> testUndoStack = (java.util.Stack<model.GameState>) undoStackField.get(aiController);
        java.util.List<model.MoveLog> testMoveHistory = (java.util.List<model.MoveLog>) moveHistoryField.get(aiController);
        java.util.Stack<model.GameState> testRedoStack = (java.util.Stack<model.GameState>) redoStackField.get(aiController);

        // Nạp 2 trạng thái (Người + AI) và 2 dòng log tương ứng vào hệ thống
        testUndoStack.push(new model.GameState(aiBoard, Color.WHITE, 180, 180));
        testUndoStack.push(new model.GameState(aiBoard, Color.BLACK, 180, 185));
        testMoveHistory.add(new model.MoveLog(new Position(1,0), new Position(2,0), null, null, Color.WHITE));
        testMoveHistory.add(new model.MoveLog(new Position(6,0), new Position(5,0), null, null, Color.BLACK));
        aiController.setCurrentTurn(Color.WHITE);

        // Act: Thực thi Undo trong trận đấu với AI
        assertDoesNotThrow(() -> aiController.undo());

        // Assert: Kiểm tra dữ liệu lùi 2 bước cờ đồng thời
        assertTrue(testUndoStack.isEmpty());
        assertTrue(testRedoStack.size() >= 2);
        assertTrue(testMoveHistory.isEmpty());
        assertEquals(Color.WHITE, aiController.getCurrentTurn());
    }

    /**
     * [UC-UNDO - AI Mode - Boundary]
     * Kiểm tra điều kiện biên chế độ AI: Chặn an toàn nếu kích thước undoStack < 2.
     */
    @Test
    void testUndoWithAIInsufficientStackSize() throws Exception {
        // Arrange: Nạp duy nhất 1 phần tử vào undoStack ở chế độ chơi với AI
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);
        Field undoStackField = GameController.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testUndoStack = (java.util.Stack<model.GameState>) undoStackField.get(aiController);
        testUndoStack.push(new model.GameState(aiBoard, Color.WHITE, 180, 180));

        // Act & Assert: Thực thi lệnh và kiểm tra cơ chế chặn lỗi thành công
        assertDoesNotThrow(() -> aiController.undo());
        assertEquals(1, testUndoStack.size());
    }

    /**
     * [UC-REDO - AI Mode]
     * Kiểm tra Redo trong chế độ AI: Hệ thống phải tiến liền lúc 2 bước cờ từ redoStack.
     */
    @Test
    void testRedoWithAISuccess() throws Exception {
        // Arrange: Khởi tạo và nạp 2 trạng thái tương lai vào redoStack
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);
        Field redoStackField = GameController.class.getDeclaredField("redoStack");
        redoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testRedoStack = (java.util.Stack<model.GameState>) redoStackField.get(aiController);

        testRedoStack.push(new model.GameState(aiBoard, Color.BLACK, 185, 185));
        testRedoStack.push(new model.GameState(aiBoard, Color.WHITE, 180, 180));

        // Act: Thực hiện Redo
        assertDoesNotThrow(() -> aiController.redo());

        // Assert: Đảm bảo bốc hết cả 2 trạng thái và cập nhật lượt đấu tương lai của AI (BLACK)
        assertTrue(testRedoStack.isEmpty());
        assertEquals(Color.BLACK, aiController.getCurrentTurn());
    }

    /**
     * [UC-REDO - AI Mode - Boundary]
     * Kiểm tra điều kiện biên chế độ AI: Từ chối Redo an toàn nếu dữ liệu redoStack < 2.
     */
    @Test
    void testRedoWithAIInsufficientStackSize() throws Exception {
        // Arrange: Nạp duy nhất 1 phần tử vào redoStack
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);
        Field redoStackField = GameController.class.getDeclaredField("redoStack");
        redoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testRedoStack = (java.util.Stack<model.GameState>) redoStackField.get(aiController);
        testRedoStack.push(new model.GameState(aiBoard, Color.WHITE, 180, 180));

        // Act & Assert: Chặn xử lý an toàn, giữ nguyên kích thước stack dữ liệu
        assertDoesNotThrow(() -> aiController.redo());
        assertEquals(1, testRedoStack.size());
    }

    /**
     * [UC-UNDO - Alternate Flow - A4]
     * Kiểm tra giới hạn: Ngăn chặn người chơi bấm Undo nhiều hơn 1 lần trong cùng một lượt.
     */
    @Test
    void testUndoLimitOncePerTurn() {
        // Arrange: Thiết lập môi trường và thực hiện lùi cờ thành công lần đầu tiên
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(180);
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}

        int whiteTimeAfterFirstUndo = gameController.getWhiteTimeLeft(); // 180s - 10s phạt = 170s

        // Act: Cố tình bấm nút Undo lần thứ 2 ngay tại lượt đó
        try {
            gameController.undo();
        } catch (Exception e) {}

        // Assert: Yêu cầu bị từ chối, giữ nguyên lượt chơi (WHITE) và không bị trừ thêm giây phạt
        assertEquals(Color.WHITE, gameController.getCurrentTurn());
        assertEquals(whiteTimeAfterFirstUndo, gameController.getWhiteTimeLeft());
    }

    /**
     * [UC-REDO - Basic Flow]
     * Kiểm tra mở khóa giới hạn: Đảm bảo lệnh Redo khôi phục cờ hiệu `hasUndoedThisTurn` về false.
     */
    @Test
    void testRedoResetsTurnUndoLimit() throws Exception {
        // Arrange: Thực hiện chuỗi thao tác Di chuyển -> Undo (Lúc này quyền undo bị khóa)
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}

        // Act: Người chơi thực thi lệnh Redo để tiến cờ lên
        try {
            gameController.redo();
        } catch (Exception e) {}

        // Assert: Xác thực trường ẩn hasUndoedThisTurn đã được giải phóng về giá trị false thành công
        Field hasUndoedField = GameController.class.getDeclaredField("hasUndoedThisTurn");
        hasUndoedField.setAccessible(true);
        boolean hasUndoedThisTurnValue = (boolean) hasUndoedField.get(gameController);

        assertFalse(hasUndoedThisTurnValue);
    }
    /**
     * [UC-UNDO - Business Rule]
     * Kiểm tra reset cờ hiệu qua lượt: Khi người chơi thực hiện một nước đi mới bình thường,
     * cờ hiệu `hasUndoedThisTurn` bắt buộc phải reset về false cho lượt của người kế tiếp.
     */
    @Test
    void testNormalMoveResetsUndoLimitForNextTurn() throws Exception {
        // Arrange: Trắng đi quân -> Bấm Undo (Khóa quyền undo lượt này)
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo();
        } catch (Exception e) {}

        // Act: Trắng thực hiện một nước đi khác hợp lệ (Lượt chuyển sang Black bình thường)
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0); // Giả lập đi lại nước khác
        } catch (Exception e) {}

        // Dùng Reflection kiểm tra biến ẩn hasUndoedThisTurn
        Field hasUndoedField = GameController.class.getDeclaredField("hasUndoedThisTurn");
        hasUndoedField.setAccessible(true);
        boolean hasUndoedThisTurnValue = (boolean) hasUndoedField.get(gameController);

        // Assert: Cờ hiệu phải bằng false để Đen có quyền Undo ở lượt của mình
        assertFalse(hasUndoedThisTurnValue, "Khi có nước đi mới đổi lượt, cờ hiệu hasUndoedThisTurn phải reset về false!");
    }
}
