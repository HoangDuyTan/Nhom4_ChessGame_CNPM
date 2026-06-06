package test;

import model.MoveLog;
import model.Position;
import model.Piece;
import model.Pawn;
import model.Rook;
import model.Knight;
import model.Bishop;
import model.Queen;
import model.King;

import java.awt.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// CHỈ DÙNG DUY NHẤT IMPORT CỦA JUNIT 5 ĐỂ TRÁNH XUNG ĐỘT CÚ PHÁP
import static org.junit.jupiter.api.Assertions.*;

public class MoveLogTest {

    /**
     * [Test]
     * Kiểm tra định dạng chuỗi ghi chú chuẩn cho nước đi di chuyển bình thường (không ăn quân).
     * Định dạng mong đợi: "[Phe]: [Tên Quân] [Ô đi] -> [Ô đến]"
     */
    @Test
    @DisplayName("Test định dạng lịch sử nước đi bình thường (Không ăn quân)")
    void testStandardMoveNotation() {
        // Thiết lập: Quân Mã Trắng di chuyển từ b1 (0,1) sang c3 (2,2)
        Position from = new Position(0, 1); // dòng 0, cột 1 tương ứng b1
        Position to = new Position(2, 2);   // dòng 2, cột 2 tương ứng c3
        Piece knight = new Knight(Color.WHITE);

        MoveLog log = new MoveLog(from, to, knight, null, Color.WHITE);

        // Chuỗi mong muốn kết xuất
        String expectedNotation = "Trắng: Mã b1 -> c3";

        // JUnit 5 nhận diện đúng: assertEquals(expected, actual, message)
        assertEquals(expectedNotation, log.getStandardNotation(),
                "Định dạng chuỗi lịch sử nước di chuyển thông thường của quân Mã chưa chính xác!");
    }

    /**
     * [Test]
     * Kiểm tra định dạng chuỗi ghi chú chuẩn cho nước đi ăn quân đối phương.
     * Định dạng mong đợi: "[Phe]: [Tên Quân] [Ô đi] x [Ô đến] (Ăn)"
     */
    @Test
    @DisplayName("Test định dạng lịch sử nước đi ăn quân đối phương")
    void testCaptureMoveNotation() {
        // Thiết lập: Quân Hậu Đen ở d8 (7,3) ăn một quân của đối phương ở h4 (3,7)
        Position from = new Position(7, 3); // dòng 7, cột 3 tương ứng d8
        Position to = new Position(3, 7);   // dòng 3, cột 7 tương ứng h4
        Piece blackQueen = new Queen(Color.BLACK);
        Piece capturedPawn = new Pawn(Color.WHITE); // Quân bị ăn là Tốt trắng

        MoveLog log = new MoveLog(from, to, blackQueen, capturedPawn, Color.BLACK);

        // Chuỗi mong muốn kết xuất
        String expectedNotation = "Đen: Hậu d8 x h4 (Ăn)";

        // JUnit 5 nhận diện đúng: assertEquals(expected, actual, message)
        assertEquals(expectedNotation, log.getStandardNotation(),
                "Định dạng chuỗi lịch sử ăn quân của quân Hậu chưa chính xác!");
    }

    /**
     * [Test]
     * Kiểm tra tính đúng đắn khi ánh xạ tên viết tắt của tất cả các quân cờ sang tiếng Việt.
     */
    @Test
    @DisplayName("Test ánh xạ chính xác tên tiếng Việt của toàn bộ các loại quân cờ")
    void testPieceNameMapping() {
        Position from = new Position(1, 0);
        Position to = new Position(2, 0);

        // 1. Kiểm tra quân Tốt (Pawn)
        MoveLog pawnLog = new MoveLog(from, to, new Pawn(Color.WHITE), null, Color.WHITE);
        assertTrue(pawnLog.getStandardNotation().contains("Tốt"), "Chưa ánh xạ đúng tên quân Tốt!");

        // 2. Kiểm tra quân Xe (Rook)
        MoveLog rookLog = new MoveLog(from, to, new Rook(Color.WHITE), null, Color.WHITE);
        assertTrue(rookLog.getStandardNotation().contains("Xe"), "Chưa ánh xạ đúng tên quân Xe!");

        // 3. Kiểm tra quân Tượng (Bishop)
        MoveLog bishopLog = new MoveLog(from, to, new Bishop(Color.WHITE), null, Color.WHITE);
        assertTrue(bishopLog.getStandardNotation().contains("Tượng"), "Chưa ánh xạ đúng tên quân Tượng!");

        // 4. Kiểm tra quân Vua (King)
        MoveLog kingLog = new MoveLog(from, to, new King(Color.WHITE), null, Color.WHITE);
        assertTrue(kingLog.getStandardNotation().contains("Vua"), "Chưa ánh xạ đúng tên quân Vua!");
    }

    /**
     * [Test Trạng Thái Biên]
     * Kiểm tra tính an toàn (Robustness): Tránh lỗi sập hệ thống (Crash/NPE) khi dữ liệu truyền vào bị null.
     */
    @Test
    @DisplayName("Test xử lý an toàn dữ liệu biên khi Position hoặc Piece bị null")
    void testNullDataHandling() {
        // Giả lập tình huống bất định dữ liệu truyền vào bị khuyết thiếu (Null)
        MoveLog edgeLog = new MoveLog(null, null, null, null, Color.WHITE);

        // Hệ thống phải xử lý mượt mà thay thế bằng ký tự an toàn thay vì tung NullPointerException
        assertDoesNotThrow(() -> edgeLog.getStandardNotation(),
                "Hệ thống bị crash tung lỗi tương tác khi log chứa dữ liệu null!");

        String result = edgeLog.getStandardNotation();
        assertTrue(result.contains("??"), "Khi vị trí null, hệ thống phải hiển thị ký hiệu thay thế '??' để bảo toàn luồng dữ liệu!");
    }
}