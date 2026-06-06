package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.SoundManager;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class SoundManagerTest {

    @BeforeEach
    void setup() {
        /*
         * UC-08: Change Setiting - Precondition
         * Thiết lập trạng thái mặc định trước mỗi lần kiểm thử.
         * Đảm bảo chức năng âm thanh được bật để các test
         * không ảnh hưởng lẫn nhau.
         */
        SoundManager.setSoundEnabled(true);
    }

    /**
     * UC-08: Change Setting (Bật âm thanh)
     *
     * Luồng chính:
     * 1. Người dùng mở cửa sổ Cài đặt.
     * 2. Người dùng bật tùy chọn Âm thanh.
     * 3. Hệ thống cập nhật trạng thái SoundEnabled = true.
     * 4. Hệ thống ghi nhận thay đổi thành công.
     *
     * Kết quả mong đợi:
     * Âm thanh được kích hoạt.
     */
    @Test
    void testEnableSound() {

        // Bước 2: Người dùng bật âm thanh
        SoundManager.setSoundEnabled(true);

        // Bước 3-4: Hệ thống lưu trạng thái bật
        assertTrue(
                SoundManager.isSoundEnabled(),
                "Âm thanh phải được bật!"
        );
    }
    /**
     * UC-08: Change Setting (Tắt âm thanh)
     *
     * Luồng chính:
     * 1. Người dùng mở cửa sổ Cài đặt.
     * 2. Người dùng tắt tùy chọn Âm thanh.
     * 3. Hệ thống cập nhật trạng thái SoundEnabled = false.
     * 4. Hệ thống ghi nhận thay đổi thành công.
     *
     * Kết quả mong đợi:
     * Âm thanh bị vô hiệu hóa.
     */
    @Test
    void testDisableSound() {

        // Bước 2: Người dùng tắt âm thanh
        SoundManager.setSoundEnabled(false);

        // Bước 3-4: Hệ thống lưu trạng thái tắt
        assertFalse(
                SoundManager.isSoundEnabled(),
                "Âm thanh phải được tắt!"
        );
    }
    /**
     * UC-08: Change Setting - Luồng ngoại lệ
     *
     * Tình huống:
     * Người dùng đã bật âm thanh nhưng file âm thanh
     * không tồn tại hoặc bị mất.
     *
     * Luồng:
     * 1. Hệ thống nhận yêu cầu phát âm thanh.
     * 2. Hệ thống tìm file âm thanh.
     * 3. File không tồn tại.
     * 4. Hệ thống xử lý lỗi nội bộ.
     * 5. Chương trình tiếp tục hoạt động.
     *
     * Kết quả mong đợi:
     * Không phát sinh Exception làm dừng chương trình.
     */
    @Test
    void testPlayMissingSoundFile() {

        assertDoesNotThrow(
                () -> {

                    // Giả lập người dùng đang bật âm thanh
                    SoundManager.setSoundEnabled(true);

                    // Truy cập hàm play() để kiểm thử tình huống lỗi
                    Method method =
                            SoundManager.class.getDeclaredMethod(
                                    "play",
                                    String.class
                            );

                    method.setAccessible(true);

                    // Cố tình truyền file không tồn tại
                    method.invoke(
                            null,
                            "abcxyz.wav"
                    );
                },
                "Hệ thống bị lỗi khi thiếu file âm thanh!"
        );
    }
}
