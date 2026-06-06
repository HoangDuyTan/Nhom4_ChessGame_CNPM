package test;

import model.MoveLog;
import model.Position;
import model.SaveGameData;
import org.junit.jupiter.api.Test;
import view.SaveManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SaveManagerTest {
    /**
     * Mục tiêu:
     * Kiểm tra hệ thống tạo file save thành công.
     */
    @Test
    public void testSaveFileCreated() {
        // Lưu dữ liệu game xuống file
        SaveManager.saveGameData(Color.WHITE, 100, new ArrayList<>(),false);
        // Kiểm tra file savegame.txt đã được tạo hay chưa
        assertTrue(SaveManager.hasSaveFile(false));
    }
    /**
     * Mục tiêu:
     * Kiểm tra dữ liệu save
     * được đọc lại chính xác.
     */
    @Test
    public void testLoadGameData() {
        // Ghi dữ liệu mẫu xuống file
        SaveManager.saveGameData(Color.WHITE, 200, new ArrayList<>(),false);
        // Đọc dữ liệu từ file
        SaveGameData data = SaveManager.loadGameData(false);
        // Kiểm tra lượt chơi

        assertNotNull(data);
        // Kiểm tra lượt chơi
        assertEquals(Color.WHITE, data.getTurn());
        // Kiểm tra thời gian
        assertEquals(200, data.getSecondsElapsed()
        );
    }
    /**
     * Mục tiêu:
     * Kiểm tra hệ thống tạo file save AI thành công.
     */
    @Test
    void testSaveAIFileCreated() {
        SaveManager.saveGameData(Color.WHITE, 100,new ArrayList<>(), true);
        assertTrue(SaveManager.hasSaveFile(true));
    }
    /**
     * Mục tiêu:
     * Kiểm tra dữ liệu lượt chơi và thời gian
     * được đọc lại chính xác từ file AI.
     */
    @Test
    public void testAILoadGameData() {
        SaveManager.saveGameData(Color.BLACK, 300, new ArrayList<>(), true);
        SaveGameData data = SaveManager.loadGameData(true);
        assertNotNull(data);
        assertEquals(Color.BLACK, data.getTurn());
        assertEquals(300, data.getSecondsElapsed());
    }
    /**
     * Mục tiêu:
     * Kiểm tra danh sách nước đi được lưu đúng
     * khi chơi với AI.
     *
     * Dữ liệu mẫu:
     * Trắng đi từ (6,4) -> (4,4)
     * AI đi từ (1,4) -> (3,4)
     */
    @Test
    public void testAISaveMoveHistory() {
        List<MoveLog> moves = new ArrayList<>();
        moves.add(new MoveLog(new Position(6, 4), new Position(4, 4), null, null, Color.WHITE));
        moves.add(new MoveLog(new Position(1, 4), new Position(3, 4), null, null, Color.BLACK));
        SaveManager.saveGameData(Color.WHITE, 500, moves, true);
        SaveGameData data = SaveManager.loadGameData(true);
        assertNotNull(data);
        assertEquals(2, data.getMoves().size());
        assertEquals("6,4,4,4", data.getMoves().get(0));
        assertEquals("1,4,3,4", data.getMoves().get(1));
    }

}