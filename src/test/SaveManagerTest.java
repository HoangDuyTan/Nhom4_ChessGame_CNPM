package test;

import model.SaveGameData;
import org.junit.jupiter.api.Test;
import view.SaveManager;

import java.awt.Color;
import java.util.ArrayList;

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
}