package test;

import org.junit.jupiter.api.Test;
import view.SoundConfig;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class SoundConfigTest {
    /**
     * [Sound Config]
     * Kiểm tra trạng thái âm thanh được lưu xuống file.
     */
    @Test
    void testSaveSoundConfig() {

        SoundConfig.save(false);

        boolean value = SoundConfig.load();

        assertFalse(
                value,
                "Giá trị lưu trong file không chính xác!"
        );
    }
    /**
     * [Sound Config]
     * Kiểm tra trạng thái âm thanh được khôi phục chính xác.
     */
    @Test
    void testLoadSoundConfig() {

        SoundConfig.save(true);

        boolean value = SoundConfig.load();

        assertTrue(
                value,
                "Không đọc đúng giá trị đã lưu!"
        );
    }
    /**
     * [Sound Config]
     * Kiểm tra khi file cấu hình không tồn tại.
     */
    @Test
    void testLoadWithoutConfigFile() {

        File file = new File("sound.config");

        if(file.exists()) {
            file.delete();
        }

        boolean value = SoundConfig.load();

        assertTrue(
                value,
                "Mặc định phải bật âm thanh khi không có file config!"
        );
    }
}
