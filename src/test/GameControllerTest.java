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
     * [UC-UNDO][Test]
     * Kiểm tra chức năng Undo cơ bản có hoạt động chính xác khi có nước đi.
     * Xác minh lượt chơi được trả về đúng phe trước đó và áp dụng hình phạt trừ 10 giây.
     */
    @Test
    void testUndoBasic() {
        // Thiết lập trạng thái và thời gian ban đầu để kiểm tra
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(180);
        gameController.setBlackTimeLeft(180);

        // Giả lập thực hiện 1 nước đi bằng cách click chuột: Tốt trắng từ (1, 0) tiến lên (2, 0)
        // Hàm processMove sẽ lưu trạng thái TRƯỚC KHI ĐI (Trắng, 180s) vào undoStack, sau đó đổi lượt sang ĐEN
        try {
            gameController.handleSquareClick(1, 0); // Chọn quân
            gameController.handleSquareClick(2, 0); // Di chuyển quân
        } catch (Exception e) {
            // Nuốt NullPointerException do view = null
        }

        // Kích hoạt hàm hoàn tác
        try {
            gameController.undo();
        } catch (Exception e) {}

        // 1. Kiểm tra lượt chơi phải quay lại cho phe TRẮNG
        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Lượt chơi chưa chuyển về đúng phe Trắng sau khi Undo!");

        /* * 2. Kiểm tra thời gian:
         * Trạng thái trước khi đi lưu 180s. Theo logic hàm undo() của bạn:
         * Sau khi khôi phục lượt Trắng -> Trắng bị phạt trừ 10s -> 180 - 10 = 170s.
         */
        assertEquals(170, gameController.getWhiteTimeLeft(), "Thời gian của phe Trắng sau khi Undo và phạt 10s chưa chính xác!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra tính an toàn (Robustness): Không thực hiện Undo và không gây lỗi hệ thống khi lịch sử rỗng (Stack rỗng).
     */
    @Test
    void testUndoEmptyStack() {
        // Khi vừa vào game chưa đi nước nào, undoStack trống, gọi undo không được văng lỗi
        assertDoesNotThrow(() -> gameController.undo(), "Hệ thống bị crash khi cố tình Undo lúc Stack rỗng!");

        // Trạng thái mặc định ban đầu không bị thay đổi
        assertEquals(Color.WHITE, gameController.getCurrentTurn());
    }

    /**
     * [UC-REDO][Test]
     * Kiểm tra chức năng Redo (Làm lại): Đảm bảo sau khi Undo, người chơi có thể bấm Redo để áp dụng lại nước đi đó.
     */
    @Test
    void testRedoAfterUndo() {
        // Đi 1 nước (Lượt Trắng đi xong -> chuyển sang lượt Đen)
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
        } catch (Exception e) {}

        // Undo nước đi vừa rồi -> chuyển ngược về lượt Trắng
        try {
            gameController.undo();
        } catch (Exception e) {}
        assertEquals(Color.WHITE, gameController.getCurrentTurn());

        // Bấm Redo để thực hiện lại nước đi đó -> Lượt chơi phải chuyển lại sang phe ĐEN đúng như tương lai
        try {
            gameController.redo();
        } catch (Exception e) {}

        assertEquals(Color.BLACK, gameController.getCurrentTurn(), "Redo xong lượt chơi phải thuộc về phe Đen!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra cơ chế quản lý dữ liệu: Đảm bảo khi gọi hàm Undo, trạng thái hiện tại phải được lưu đúng vào redoStack.
     */
    @Test
    void testUndoPushToRedoStack() {
        // Đi 1 nước cờ
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
        } catch (Exception e) {}

        // Bấm Undo
        try {
            gameController.undo();
        } catch (Exception e) {}

        // Nếu trạng thái được đẩy vào redoStack đúng, lệnh redo() tiếp theo phải thực hiện được ngon lành mà không bị chặn
        assertDoesNotThrow(() -> gameController.redo(), "RedoStack bị rỗng hoặc lỗi khiến không thể gọi hàm redo() sau khi Undo!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra giới hạn biên thời gian phạt: Đảm bảo thời gian sau khi phạt Undo không bao giờ xuống dưới mốc 0 giây (bị âm).
     */
    @Test
    void testUndoTimeDeductionLowerBound() {
        // Giả lập người chơi chỉ còn 5 giây trước khi thực hiện hành động
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(5);

        // Đi quân
        try {
            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
        } catch (Exception e) {}

        // Bấm Undo -> Thời gian khôi phục 5s, trừ đi 10s phạt = -5s.
        // Logic chặn `if (this.whiteTimeLeft < 0) this.whiteTimeLeft = 0;` phải đưa về mốc 0.
        try {
            gameController.undo();
        } catch (Exception e) {}

        assertEquals(0, gameController.getWhiteTimeLeft(), "Thời gian của người chơi bị hiển thị số âm sau khi phạt Undo!");
    }
    /**
     * [UC-UNDO][Test]
     * Kiểm tra giới hạn tối đa số lần Undo của quân TRẮNG.
     * Đảm bảo hệ thống chặn không cho phép hoàn tác khi vượt quá 3 lần quy định.
     */
    @Test
    void testWhiteMaxThreeUndos() {
        // Thiết lập ban đầu: Lượt của TRẮNG
        gameController.setCurrentTurn(Color.WHITE);
        gameController.setWhiteTimeLeft(180);
        gameController.setBlackTimeLeft(180);

        // --- Giả lập TRẮNG thực hiện nước đi hợp lệ đầu tiên ---
        // Lượt TRẮNG đi -> Đổi sang lượt ĐEN -> Undo lúc này tính cho TRẮNG
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        // Thực hiện Undo lần 1 (Hợp lệ: còn 2 lượt)
        gameController.undo();
        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Undo lần 1 thất bại!");

        // --- Giả lập TRẮNG đi nước thứ 2 ---
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        // Thực hiện Undo lần 2 (Hợp lệ: còn 1 lượt)
        gameController.undo();

        // --- Giả lập TRẮNG đi nước thứ 3 ---
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        // Thực hiện Undo lần 3 (Hợp lệ: còn 0 lượt)
        gameController.undo();

        // --- Giả lập TRẮNG đi nước thứ 4 ---
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        // Ghi lại trạng thái trước khi cố tình Undo lần 4
        Color turnBeforeFailedUndo = gameController.getCurrentTurn(); // Hiện tại phải là BLACK

        // Thực hiện Undo lần 4 (Không hợp lệ: Hệ thống phải chặn lại và giữ nguyên trạng thái)
        gameController.undo();

        // Kiểm tra xem lượt chơi có bị hoàn tác hay không. Nếu bị chặn, lượt vẫn phải giữ nguyên là BLACK
        assertEquals(turnBeforeFailedUndo, gameController.getCurrentTurn(),
                "Quân TRẮNG đã dùng quá 3 lần Undo nhưng hệ thống không chặn lại!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra giới hạn tối đa số lần Undo của quân ĐEN.
     * Đảm bảo hệ thống chặn không cho phép hoàn tác khi vượt quá 3 lần quy định.
     */
    @Test
    void testBlackMaxThreeUndos() {
        // Thiết lập ban đầu: Để ĐEN đi thì TRẮNG phải đi trước 1 nước
        gameController.setCurrentTurn(Color.WHITE);
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0); // Bàn cờ chuyển sang lượt ĐEN

        // Giả lập ĐEN đi quân (ví dụ Tốt đen từ ô 6,0 sang 5,0)
        // Lượt ĐEN đi -> Đổi sang lượt TRẮNG -> Undo lúc này tính cho ĐEN

        // Lần 1
        gameController.handleSquareClick(6, 0);
        gameController.handleSquareClick(5, 0);
        gameController.undo(); // Undo hợp lệ (ĐEN còn 2 lần)

        // Lần 2
        gameController.handleSquareClick(6, 0);
        gameController.handleSquareClick(5, 0);
        gameController.undo(); // Undo hợp lệ (ĐEN còn 1 lượt)

        // Lần 3
        gameController.handleSquareClick(6, 0);
        gameController.handleSquareClick(5, 0);
        gameController.undo(); // Undo hợp lệ (ĐEN còn 0 lượt)

        // Lần 4 (Quá giới hạn)
        gameController.handleSquareClick(6, 0);
        gameController.handleSquareClick(5, 0); // Đi quân lần nữa, lượt chuyển sang WHITE

        Color turnBeforeFailedUndo = gameController.getCurrentTurn(); // Phải là WHITE

        gameController.undo(); // Cố tình gọi Undo lần 4, hệ thống phải từ chối hành động

        assertEquals(turnBeforeFailedUndo, gameController.getCurrentTurn(),
                "Quân ĐEN đã dùng quá 3 lần Undo nhưng hệ thống không chặn lại!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra khôi phục số lượt Undo khi làm mới bàn đấu (Chơi lại ván mới).
     * Đảm bảo số lần Undo của cả hai bên được làm mới đầy đủ về mốc 3 lần sau khi hệ thống Khởi động lại.
     */
    @Test
    void testUndoCountResetOnRestart() {
        // Thiết lập ban đầu: Lượt của TRẮNG
        gameController.setCurrentTurn(Color.WHITE);

        // TRẮNG thực hiện đi quân rồi dùng hết 1 lần Undo
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);
        gameController.undo();

        // Kích hoạt tính năng chơi lại ván mới
        gameController.restartGame();

        // Sau khi restart, đi quân lại lần nữa
        gameController.handleSquareClick(1, 0);
        gameController.handleSquareClick(2, 0);

        // Thử thực hiện Undo liên tiếp 3 lần xem có được phục hồi đầy đủ hay không
        assertDoesNotThrow(() -> {
            gameController.undo(); // Lần 1 sau khi restart

            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo(); // Lần 2 sau khi restart

            gameController.handleSquareClick(1, 0);
            gameController.handleSquareClick(2, 0);
            gameController.undo(); // Lần 3 sau khi restart
        }, "Lượt dùng Undo chưa được reset về 3 sau khi bấm Chơi Mới (Restart)!");
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
     * [UC-UNDO - AI Mode][Test]
     * Kiểm tra Undo thành công khi chơi với AI: Hệ thống phải bốc đồng thời 2 trạng thái
     * (Nước của AI và nước của Người) khỏi undoStack, đồng thời xóa 2 bản ghi lịch sử.
     */
    @Test
    void testUndoWithAISuccess() throws Exception {
        // Khởi tạo một Controller riêng biệt chạy chế độ AI (playWithAI = true)
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);

        // Truy xuất các thuộc tính private qua Reflection để nạp dữ liệu giả lập
        Field undoStackField = GameController.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testUndoStack = (java.util.Stack<model.GameState>) undoStackField.get(aiController);

        Field moveHistoryField = GameController.class.getDeclaredField("moveHistory");
        moveHistoryField.setAccessible(true);
        java.util.List<model.MoveLog> testMoveHistory = (java.util.List<model.MoveLog>) moveHistoryField.get(aiController);

        Field redoStackField = GameController.class.getDeclaredField("redoStack");
        redoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testRedoStack = (java.util.Stack<model.GameState>) redoStackField.get(aiController);

        // Tạo 2 trạng thái giả lập đại diện cho nước đi của Người (Player) và Máy (AI)
        model.GameState playerState = new model.GameState(aiBoard, Color.WHITE, 180, 180);
        model.GameState aiState = new model.GameState(aiBoard, Color.BLACK, 180, 185);

        // Đẩy vào stack theo đúng thứ tự vận hành: nước của Người nằm dưới, nước của AI nằm trên cùng
        testUndoStack.push(playerState);
        testUndoStack.push(aiState);

        // Giả lập danh sách lịch sử text đã ghi nhận 2 nước đi này
        testMoveHistory.add(new model.MoveLog(new Position(1,0), new Position(2,0), null, null, Color.WHITE));
        testMoveHistory.add(new model.MoveLog(new Position(6,0), new Position(5,0), null, null, Color.BLACK));

        // Thiết lập lượt hiện tại của game đang ở tương lai (sau khi AI đi xong thì quay về lượt WHITE)
        aiController.setCurrentTurn(Color.WHITE);
        aiController.setWhiteTimeLeft(180);

        // Kích hoạt lệnh hoàn tác
        assertDoesNotThrow(() -> aiController.undo(), "Hàm undo() với AI bị văng lỗi!");

        // XÁC MINH 1: undoStack phải giải phóng hoàn toàn cả 2 phần tử
        assertTrue(testUndoStack.isEmpty(), "Chế độ AI phải lùi liền lúc 2 bước cờ khỏi undoStack!");

        // XÁC MINH 2: Đảm bảo các trạng thái cờ hoàn tác đã được bốc thành công sang redoStack
        assertTrue(testRedoStack.size() >= 2, "redoStack không nhận đủ các trạng thái cờ hoàn tác từ đối thủ!");

        // XÁC MINH 3: moveHistory phải được dọn dẹp sạch sẽ cả 2 dòng ghi chú nước đi
        assertTrue(testMoveHistory.isEmpty(), "Màn hình lịch sử nước đi không xóa bỏ 2 nước cờ của Người và AI sau khi Undo!");

        // XÁC MINH 4: Lượt chơi phải được trả về chuẩn xác cho phe TRẮNG (Người chơi)
        assertEquals(Color.WHITE, aiController.getCurrentTurn(), "Lượt chơi không trả về đúng cho Người chơi sau khi Undo với AI!");
    }
    /**
     * [UC-UNDO - AI Mode][Test Trạng Thái Biên]
     * Kiểm tra tính an toàn khi Undo với AI: Nếu số lượng phần tử trong undoStack < 2,
     * hệ thống phải lập tức từ chối và hủy bỏ thao tác để ngăn chặn crash lỗi rỗng Stack.
     */
    @Test
    void testUndoWithAIInsufficientStackSize() throws Exception {
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);

        Field undoStackField = GameController.class.getDeclaredField("undoStack");
        undoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testUndoStack = (java.util.Stack<model.GameState>) undoStackField.get(aiController);

        // Tình huống: Mới chỉ có 1 phần tử duy nhất trong stack
        testUndoStack.push(new model.GameState(aiBoard, Color.WHITE, 180, 180));

        // Thực thi lệnh và đảm bảo Guard Clause hoạt động chặn đứng lỗi văng ra
        assertDoesNotThrow(() -> aiController.undo(), "Hệ thống bị crash lỗi EmptyStackException do không check size < 2!");
        assertEquals(1, testUndoStack.size(), "Stack dữ liệu bị sai lệch sau khi lệnh Undo bị từ chối!");
    }

    /**
     * [UC-REDO - AI Mode][Test]
     * Kiểm tra Redo thành công khi chơi với AI: Hệ thống phải tiến liền 2 bước cờ
     * từ redoStack ngược trở lại bàn đấu để khớp với tiến trình phản hồi của AI.
     */
    @Test
    void testRedoWithAISuccess() throws Exception {
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);

        Field redoStackField = GameController.class.getDeclaredField("redoStack");
        redoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testRedoStack = (java.util.Stack<model.GameState>) redoStackField.get(aiController);

        // Tạo trạng thái giả lập (Nước của Người và nước phản hồi của AI)
        model.GameState playerState = new model.GameState(aiBoard, Color.WHITE, 180, 180);
        model.GameState aiState = new model.GameState(aiBoard, Color.BLACK, 185, 185);

        // Đẩy vào redoStack theo thứ tự sẵn sàng tiến lên: Nước của Người đi trước (trên), nước AI đi sau (dưới)
        testRedoStack.push(aiState);
        testRedoStack.push(playerState);

        // Bấm nút Redo (Làm lại)
        assertDoesNotThrow(() -> aiController.redo(), "Hàm redo() với AI bị văng lỗi!");

        // XÁC MINH: redoStack phải trống rỗng vì đã bốc cả 2 nước cờ tiến lên phía trước
        assertTrue(testRedoStack.isEmpty(), "Chế độ AI phải giải phóng liền lúc 2 bước cờ khỏi redoStack!");

        // Trạng thái lượt chơi cuối cùng phải đồng bộ khớp với lượt của AI (BLACK)
        assertEquals(Color.BLACK, aiController.getCurrentTurn(), "Redo trong chế độ AI không đưa lượt đấu tiến về đúng trạng thái tương lai!");
    }

    /**
     * [UC-REDO - AI Mode][Test Trạng Thái Biên]
     * Kiểm tra tính an toàn khi Redo với AI: Nếu redoStack < 2 phần tử,
     * hệ thống phải hủy lệnh an toàn mà không làm mất mát dữ liệu hiện tại.
     */
    @Test
    void testRedoWithAIInsufficientStackSize() throws Exception {
        Board aiBoard = new Board();
        GameController aiController = new GameController(aiBoard, null, true);

        Field redoStackField = GameController.class.getDeclaredField("redoStack");
        redoStackField.setAccessible(true);
        java.util.Stack<model.GameState> testRedoStack = (java.util.Stack<model.GameState>) redoStackField.get(aiController);

        // Chỉ có 1 phần tử đơn lẻ trong Redo
        testRedoStack.push(new model.GameState(aiBoard, Color.WHITE, 180, 180));

        // Thực thi lệnh và đảm bảo chặn lỗi thành công
        assertDoesNotThrow(() -> aiController.redo(), "Hệ thống bị sập lỗi khi gọi Redo với AI lúc dữ liệu RedoStack thiếu hụt!");
        assertEquals(1, testRedoStack.size(), "Dữ liệu trong redoStack bị thay đổi bất thường!");
    }
}
