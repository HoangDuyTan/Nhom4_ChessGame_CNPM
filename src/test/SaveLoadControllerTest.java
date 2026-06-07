package test;

import controller.SaveLoadController;
import model.MoveLog;
import org.junit.jupiter.api.Test;
import view.SaveManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class SaveLoadControllerTest {
    /**
     * Mục tiêu:
     * Kiểm tra chức năng tự động lưu (Auto Save)
     * hoạt động chính xác khi người chơi đấu với AI.
     */
    @Test
    public void testAutoSaveAI() {
        // Danh sách lịch sử nước đi mẫu
        List<MoveLog> moves = new ArrayList<>();
        // Thực hiện chức năng Auto Save
        SaveLoadController.autoSave(Color.BLACK, 100, moves, true);
        // Kiểm tra file save của chế độ AI đã được tạo
        assertTrue(SaveManager.hasSaveFile(true));
    }
}
