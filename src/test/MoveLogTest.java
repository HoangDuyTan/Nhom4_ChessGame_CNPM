package test;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

    private Board board;

    @BeforeEach
    void setUp() {
        // Khởi tạo một bàn cờ mới để làm môi trường test
        board = new Board();
        board.reset(); // Đưa về trạng thái xếp quân chuẩn
    }

    /**
     * [UC-SNAPSHOT][Test]
     * Kiểm tra cơ chế "Chụp ảnh bàn cờ" (Board Snapshot Deep Copy).
     * Đảm bảo GameState tạo ra các thực thể quân cờ mới hoàn toàn độc lập,
     * chứ không chỉ tham chiếu (reference) tới quân cờ trên bàn cờ thật.
     */
    @Test
    @DisplayName("Test GameState chụp ảnh sâu (Deep Copy) trạng thái bàn cờ")
    void testGameStateDeepCopySnapshot() {
        Position e2 = new Position(1, 4); // Vị trí Tốt trắng ban đầu
        Piece originalPawn = board.get(e2);
        assertNotNull(originalPawn, "Vị trí e2 phải có quân cờ ban đầu");

        // 1. Tạo bản chụp snapshot GameState tại thời điểm này
        GameState snapshot = new GameState(board, Color.WHITE, 600, 600);

        // 2. Lấy quân cờ đã được lưu trong bản chụp snapshot
        Piece snapshottedPawn = snapshot.getGrid()[1][4];

        // 3. XÁC MINH: Quân cờ được copy đúng loại và đúng màu
        assertNotNull(snapshottedPawn);
        assertEquals('p', Character.toLowerCase(snapshottedPawn.getShortName()));
        assertEquals(Color.WHITE, snapshottedPawn.getColor());

        // 4. KIỂM TRA ĐỘC LẬP (Deep Copy):
        // Thực thể trong snapshot và thực thể trên board phải là 2 đối tượng khác nhau trong vùng nhớ
        assertNotSame(originalPawn, snapshottedPawn,
                "Thất bại: GameState chỉ sao chép tham chiếu nông (Shallow Copy), không phải Deep Copy!");
    }

    /**
     * [UC-RESTORE][Test]
     * Kiểm tra cơ chế Khôi phục trạng thái (Restoration).
     * Đảm bảo khi gọi hàm restore(board), bàn cờ sẽ quay về y hệt trạng thái đã chụp.
     */
    @Test
    @DisplayName("Test hàm restore khôi phục chính xác thế cờ cũ lên Board")
    void testGameStateRestoration() {
        Position a1 = new Position(0, 0); // Vị trí Xe trắng ban đầu
        Position a2 = new Position(1, 0); // Vị trí Tốt trắng ban đầu

        // 1. Chụp lại trạng thái lúc bàn cờ còn nguyên vẹn
        GameState savedState = new GameState(board, Color.WHITE, 500, 450);

        // 2. Giả lập làm thay đổi/phá hủy thế cờ trên bàn cờ thật (Xóa quân hoặc di chuyển quân)
        board.set(a1, null);
        board.set(a2, null);
        assertNull(board.get(a1), "Bàn cờ thật đã bị xóa quân Xe");
        assertNull(board.get(a2), "Bàn cờ thật đã bị xóa quân Tốt");

        // 3. Thực hiện khôi phục (Restore) từ bản chụp cũ
        savedState.restore(board);

        // 4. XÁC MINH: Các quân cờ bị mất phải được tái tạo lại hoàn hảo trên bàn cờ thật
        assertNotNull(board.get(a1), "Hàm restore chưa khôi phục được quân Xe tại a1!");
        assertNotNull(board.get(a2), "Hàm restore chưa khôi phục được quân Tốt tại a2!");
        assertEquals('r', Character.toLowerCase(board.get(a1).getShortName()));
        assertEquals('p', Character.toLowerCase(board.get(a2).getShortName()));
    }

    /**
     * [UC-DATA][Test]
     * Kiểm tra lưu trữ thông tin đi kèm (Turn & Time Log).
     */
    @Test
    @DisplayName("Test GameState lưu trữ chính xác lượt đi và quỹ thời gian còn lại")
    void testGameStateMetadata() {
        // Tạo snapshot với thông số cụ thể
        GameState state = new GameState(board, Color.BLACK, 120, 240);

        // Xác minh các thông tin metadata phải được giữ toàn vẹn
        assertEquals(Color.BLACK, state.getTurn(), "Lưu sai lượt đi!");
        assertEquals(120, state.getWhiteTimeLeft(), "Lưu sai thời gian Trắng!");
        assertEquals(240, state.getBlackTimeLeft(), "Lưu sai thời gian Đen!");
    }
}