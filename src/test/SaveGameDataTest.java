package test;

import model.SaveGameData;
import org.junit.jupiter.api.Test;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SaveGameDataTest {
    /**
     * Mục tiêu:
     * Kiểm tra lượt chơi hiện tại
     * được lưu chính xác.
     */
    @Test
    public void testSaveTurn() {
        SaveGameData data = new SaveGameData();
        // Gán lượt hiện tại là quân trắng
        data.setTurn(Color.WHITE);
        // Kiểm tra giá trị lấy ra có giống giá trị đã lưu hay không
        assertEquals(Color.WHITE, data.getTurn()
        );
    }
    /**
     * Mục tiêu:
     * Kiểm tra thời gian trận đấu
     * được lưu đúng.
     */
    @Test
    public void testSaveTime() {
        SaveGameData data = new SaveGameData();
        // Lưu thời gian giả lập
        data.setSecondsElapsed(500);
        // Kiểm tra thời gian đọc lại có đúng 500 giây không
        assertEquals(500, data.getSecondsElapsed());
    }
    /**
     * Mục tiêu:
     * Kiểm tra danh sách lịch sử
     * nước đi được khởi tạo thành công.
     */
    @Test
    public void testMoveListInitialization() {
        SaveGameData data = new SaveGameData();
        // Danh sách nước đi không được null
        assertNotNull(data.getMoves());
        // Ban đầu chưa có nước đi nào
        assertEquals(0, data.getMoves().size());
    }
}
