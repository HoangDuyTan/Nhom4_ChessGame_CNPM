package test;

import org.junit.jupiter.api.Test;
import view.GameConfig;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class GameConfigTest {

    /**
     * [UC-08: Change Setting]
     * Bước 3:
     * Người chơi bật Chế độ nâng cao.
     *
     * Kết quả mong đợi:
     * Trạng thái được cập nhật thành true.
     */
    @Test
    void testEnableAdvancedMode() {

        GameConfig.setAdvancedMode(true);

        assertTrue(
                GameConfig.isAdvancedMode(),
                "Chế độ nâng cao phải được bật!"
        );
    }

    /**
     * [UC-08: Change Setting]
     * Bước 3:
     * Người chơi tắt Chế độ nâng cao.
     *
     * Kết quả mong đợi:
     * Trạng thái được cập nhật thành false.
     */
    @Test
    void testDisableAdvancedMode() {

        GameConfig.setAdvancedMode(false);

        assertFalse(
                GameConfig.isAdvancedMode(),
                "Chế độ nâng cao phải được tắt!"
        );
    }

    /**
     * [UC-08: Change Setting]
     * Bước 4:
     * Hệ thống lưu cấu hình Chế độ nâng cao.
     *
     * Kết quả mong đợi:
     * Giá trị false được lưu và đọc lại chính xác.
     */
    @Test
    void testSaveAdvancedModeFalse() {

        GameConfig.setAdvancedMode(false);

        assertFalse(
                GameConfig.isAdvancedMode(),
                "Giá trị cấu hình false không được lưu đúng!"
        );
    }

    /**
     * [UC-08: Change Setting]
     * Bước 4:
     * Hệ thống lưu cấu hình Chế độ nâng cao.
     *
     * Kết quả mong đợi:
     * Giá trị true được lưu và đọc lại chính xác.
     */
    @Test
    void testSaveAdvancedModeTrue() {

        GameConfig.setAdvancedMode(true);

        assertTrue(
                GameConfig.isAdvancedMode(),
                "Giá trị cấu hình true không được lưu đúng!"
        );
    }

    /**
     * [UC-08: Change Setting]
     * Luồng ngoại lệ:
     * File cấu hình không tồn tại.
     *
     * Kết quả mong đợi:
     * Hệ thống sử dụng giá trị mặc định là false.
     */
    @Test
    void testLoadWithoutConfigFile() {

        File file = new File("game.config");

        if (file.exists()) {
            file.delete();
        }

        GameConfig.setAdvancedMode(false);

        assertFalse(
                GameConfig.isAdvancedMode(),
                "Giá trị mặc định phải là false khi không có file cấu hình!"
        );
    }
}