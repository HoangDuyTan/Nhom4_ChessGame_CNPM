package test;

import controller.GameController;
import model.Board;
import model.Position;
import java.awt.Color;
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

    /**
     * [UC-UNDO][Test]
     * Kiểm tra chức năng Undo cơ bản có hoạt động chính xác khi có nước đi.
     * Xác minh lượt chơi và thời gian được trả về đúng trạng thái trước đó.
     */
    @Test
    void testUndoBasic() {
        // Thiết lập thời gian ban đầu để kiểm tra
        gameController.setWhiteTimeLeft(180);
        gameController.setBlackTimeLeft(180);

        // Giả lập thực hiện 1 nước đi từ ô (6,0) đến (5,0)
        gameController.handleMove(new Position(6, 0), new Position(5, 0));

        // Lưu lại trạng thái thời gian ngay trước khi bấm Undo
        int beforeUndoTimeWhite = gameController.getWhiteTimeLeft();

        // Kích hoạt hàm hoàn tác
        gameController.undo();

        // 1. Kiểm tra lượt chơi phải quay lại cho phe TRẮNG
        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Lượt chơi chưa chuyển về đúng phe Trắng!");

        // 2. Kiểm tra thời gian của phe Trắng phải được restore nguyên vẹn
        assertEquals(beforeUndoTimeWhite, gameController.getWhiteTimeLeft(), "Thời gian của phe Trắng khôi phục chưa đúng!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra tính an toàn (Robustness): Không thực hiện Undo và không gây lỗi hệ thống khi lịch sử rỗng (Stack rỗng).
     */
    @Test
    void testUndoEmptyStack() {
        // Khi vừa vào game chưa đi nước nào, undoStack trống, gọi undo không được văng lỗi
        assertDoesNotThrow(() -> gameController.undo(), "Hệ thống bị crash khi cố tình Undo lúc Stack rỗng!");
    }

    /**
     * [UC-REDO][Test]
     * Kiểm tra chức năng Redo (Làm lại): Đảm bảo sau khi Undo, người chơi có thể bấm Redo để áp dụng lại nước đi đó.
     */
    @Test
    void testRedoAfterUndo() {
        // Đi 1 nước (Lượt Trắng đi xong -> chuyển sang lượt Đen)
        gameController.handleMove(new Position(6, 0), new Position(5, 0));

        // Undo nước đi vừa rồi -> chuyển ngược về lượt Trắng
        gameController.undo();

        // Bấm Redo để thực hiện lại nước đi đó -> Lượt chơi phải chuyển lại sang phe ĐEN
        gameController.redo();

        // Xác minh sau khi redo thì quyền đi cờ thuộc về phe ĐEN
        assertEquals(Color.BLACK, gameController.getCurrentTurn(), "Redo xong lượt chơi phải thuộc về phe Đen!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra cơ chế quản lý dữ liệu: Đảm bảo khi gọi hàm Undo, trạng thái hiện tại phải được lưu đúng vào redoStack.
     */
    @Test
    void testUndoPushToRedoStack() {
        // Đi 1 nước cờ
        gameController.handleMove(new Position(6, 0), new Position(5, 0));

        // Bấm Undo
        gameController.undo();

        // Nếu trạng thái được đẩy vào redoStack đúng, lệnh redo() tiếp theo phải thực hiện được ngon lành
        assertDoesNotThrow(() -> gameController.redo(), "RedoStack bị rỗng hoặc lỗi khiến không thể gọi hàm redo() sau khi Undo!");
    }

    /**
     * [UC-UNDO][Test]
     * Kiểm tra chuyên sâu về đồng bộ thời gian trong GameState:
     * Xác minh dữ liệu thời gian của cả 2 bên (White và Black) được khôi phục chính xác về quá khứ
     * bất kể thời gian hiện tại có bị thay đổi/trôi qua bao nhiêu đi nữa.
     */
    @Test
    void testUndoRestoreTime() {
        // Bước 1: Thiết lập quỹ thời gian ban đầu cho 2 phe trước khi đi cờ
        gameController.setWhiteTimeLeft(100);
        gameController.setBlackTimeLeft(120);

        // Bước 2: Đi 1 nước cờ (Lúc này GameState lưu cặp thời gian 100s - 120s vào undoStack)
        gameController.handleMove(new Position(6, 0), new Position(5, 0));

        // Bước 3: Giả lập thời gian của ván đấu thực tế bị thay đổi (giảm xuống còn 50s do đồng hồ chạy tiếp)
        gameController.setWhiteTimeLeft(50);

        // Bước 4: Người chơi bấm nút Undo
        gameController.undo();

        // Bước 5: Xác minh thời gian của cả 2 phe phải quay về đúng mốc lịch sử (100s và 120s) ban đầu
        assertEquals(100, gameController.getWhiteTimeLeft(), "Thời gian Trắng khôi phục từ GameState bị sai!");
        assertEquals(120, gameController.getBlackTimeLeft(), "Thời gian Đen khôi phục từ GameState bị sai!");
    }
}