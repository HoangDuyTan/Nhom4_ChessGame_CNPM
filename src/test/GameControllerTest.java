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

//    /**
//     * [UC-UNDO][Test]
//     * Kiểm tra chức năng Undo cơ bản có hoạt động chính xác khi có nước đi.
//     * Xác minh lượt chơi và thời gian được trả về đúng trạng thái trước đó.
//     */
//    @Test
//    void testUndoBasic() {
//        // Thiết lập thời gian ban đầu để kiểm tra
//        gameController.setWhiteTimeLeft(180);
//        gameController.setBlackTimeLeft(180);
//
//        // Giả lập thực hiện 1 nước đi từ ô (6,0) đến (5,0)
//        gameController.handleMove(new Position(6, 0), new Position(5, 0));
//
//        // Lưu lại trạng thái thời gian ngay trước khi bấm Undo
//        int beforeUndoTimeWhite = gameController.getWhiteTimeLeft();
//
//        // Kích hoạt hàm hoàn tác
//        gameController.undo();
//
//        // 1. Kiểm tra lượt chơi phải quay lại cho phe TRẮNG
//        assertEquals(Color.WHITE, gameController.getCurrentTurn(), "Lượt chơi chưa chuyển về đúng phe Trắng!");
//
//        // 2. Kiểm tra thời gian của phe Trắng phải được restore nguyên vẹn
//        assertEquals(beforeUndoTimeWhite, gameController.getWhiteTimeLeft(), "Thời gian của phe Trắng khôi phục chưa đúng!");
//    }
//
//    /**
//     * [UC-UNDO][Test]
//     * Kiểm tra tính an toàn (Robustness): Không thực hiện Undo và không gây lỗi hệ thống khi lịch sử rỗng (Stack rỗng).
//     */
//    @Test
//    void testUndoEmptyStack() {
//        // Khi vừa vào game chưa đi nước nào, undoStack trống, gọi undo không được văng lỗi
//        assertDoesNotThrow(() -> gameController.undo(), "Hệ thống bị crash khi cố tình Undo lúc Stack rỗng!");
//    }
//
//    /**
//     * [UC-REDO][Test]
//     * Kiểm tra chức năng Redo (Làm lại): Đảm bảo sau khi Undo, người chơi có thể bấm Redo để áp dụng lại nước đi đó.
//     */
//    @Test
//    void testRedoAfterUndo() {
//        // Đi 1 nước (Lượt Trắng đi xong -> chuyển sang lượt Đen)
//        gameController.handleMove(new Position(6, 0), new Position(5, 0));
//
//        // Undo nước đi vừa rồi -> chuyển ngược về lượt Trắng
//        gameController.undo();
//
//        // Bấm Redo để thực hiện lại nước đi đó -> Lượt chơi phải chuyển lại sang phe ĐEN
//        gameController.redo();
//
//        // Xác minh sau khi redo thì quyền đi cờ thuộc về phe ĐEN
//        assertEquals(Color.BLACK, gameController.getCurrentTurn(), "Redo xong lượt chơi phải thuộc về phe Đen!");
//    }
//
//    /**
//     * [UC-UNDO][Test]
//     * Kiểm tra cơ chế quản lý dữ liệu: Đảm bảo khi gọi hàm Undo, trạng thái hiện tại phải được lưu đúng vào redoStack.
//     */
//    @Test
//    void testUndoPushToRedoStack() {
//        // Đi 1 nước cờ
//        gameController.handleMove(new Position(6, 0), new Position(5, 0));
//
//        // Bấm Undo
//        gameController.undo();
//
//        // Nếu trạng thái được đẩy vào redoStack đúng, lệnh redo() tiếp theo phải thực hiện được ngon lành
//        assertDoesNotThrow(() -> gameController.redo(), "RedoStack bị rỗng hoặc lỗi khiến không thể gọi hàm redo() sau khi Undo!");
//    }
//
//    /**
//     * [UC-UNDO][Test]
//     * Kiểm tra chuyên sâu về đồng bộ thời gian trong GameState:
//     * Xác minh dữ liệu thời gian của cả 2 bên (White và Black) được khôi phục chính xác về quá khứ
//     * bất kể thời gian hiện tại có bị thay đổi/trôi qua bao nhiêu đi nữa.
//     */
//    @Test
//    void testUndoRestoreTime() {
//        // Bước 1: Thiết lập quỹ thời gian ban đầu cho 2 phe trước khi đi cờ
//        gameController.setWhiteTimeLeft(100);
//        gameController.setBlackTimeLeft(120);
//
//        // Bước 2: Đi 1 nước cờ (Lúc này GameState lưu cặp thời gian 100s - 120s vào undoStack)
//        gameController.handleMove(new Position(6, 0), new Position(5, 0));
//
//        // Bước 3: Giả lập thời gian của ván đấu thực tế bị thay đổi (giảm xuống còn 50s do đồng hồ chạy tiếp)
//        gameController.setWhiteTimeLeft(50);
//
//        // Bước 4: Người chơi bấm nút Undo
//        gameController.undo();
//
//        // Bước 5: Xác minh thời gian của cả 2 phe phải quay về đúng mốc lịch sử (100s và 120s) ban đầu
//        assertEquals(100, gameController.getWhiteTimeLeft(), "Thời gian Trắng khôi phục từ GameState bị sai!");
//        assertEquals(120, gameController.getBlackTimeLeft(), "Thời gian Đen khôi phục từ GameState bị sai!");
//    }

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
}