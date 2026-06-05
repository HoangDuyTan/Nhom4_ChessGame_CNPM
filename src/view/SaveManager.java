package view;

import model.Board;
import model.Piece;
import model.Position;

import java.awt.*;
import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.txt";

    /**
     * Chức năng: Thực thi tuần tự hóa (Serialization) trạng thái game ra tệp tin cấu hình.
     * Ánh xạ các Use Case phân rã thành phần:
     */
    public static void saveGameData(Color currentTurn, Board board, int secondsElapsed) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            /* * MÃ USE CASE: UC-04.1.1 (Trích xuất lượt đi)
             * Chức năng: Đọc màu của lượt đi hiện tại từ Controller và chuyển đổi thành ký tự W hoặc B.
             */
            bw.write(currentTurn == Color.WHITE ? "W" : "B");
            bw.newLine();

            /* * MÃ USE CASE: UC-04.1.2 (Trích xuất thời gian)
             * Chức năng: Lấy giá trị biến đếm giây secondsElapsed của hệ thống để đồng bộ thời gian thi đấu.
             */
            bw.write(String.valueOf(secondsElapsed));
            bw.newLine();

            /* * MÃ USE CASE: UC-04.1.3 (Trích xuất trạng thái bàn cờ)
             * Chức năng: Chạy vòng lặp quét qua mảng 2 chiều kích thước 8x8 của Model Board,
             * mã hóa ngắn tên các thực thể quân cờ (K, Q, R, B, N, P) hoặc dấu '-' nếu ô trống.
             */
            for (int r = 0; r < 8; r++) {
                StringBuilder rowStr = new StringBuilder();

                for (int c = 0; c < 8; c++) {
                    Piece p = board.get(new Position(r, c));

                    if (p == null) {
                        rowStr.append("-");
                    } else {
                        rowStr.append(p.getShortName());
                    }
                }

                /* * MÃ USE CASE: UC-04.1.4 (Ghi dữ liệu vào file)
                 * Chức năng: Đẩy chuỗi dữ liệu đã được định dạng và mã hóa xuống tệp vật lý savegame.txt.
                 */
                bw.write(rowStr.toString());
                bw.newLine();
            }
        } catch (Exception e) {
            /* * MÃ USE CASE: UC-04.1.5 (Ghi nhận lỗi hệ thống - Luồng thay thế A1)
             * Chức năng: Khi xảy ra lỗi I/O (không có quyền truy cập, ổ cứng đầy), hệ thống thực hiện
             * ghi vết log lỗi ra console để lập trình viên theo dõi, đồng thời ngăn chặn crash ứng dụng,
             * giúp trận đấu của người chơi vẫn tiếp tục diễn ra bình thường.
             */
            e.printStackTrace();
        }
    }

    public static String[] loadGameData() {
        String[] data = new String[10];

        /* * MÃ USE CASE: UC-04.2.1 (Đọc dữ liệu từ file)
         * Chức năng: Thực hiện mở file savegame.txt và đọc luồng dữ liệu đúng 10 dòng
         * (1 dòng lượt đi, 1 dòng thời gian, 8 dòng bàn cờ).
         */
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE))) {
            for (int i = 0; i < 10; i++) {
                data[i] = br.readLine();
            }

            return data;
        } catch (Exception e) {
            /* * MÃ USE CASE: UC-04.2.4 (Xử lý lỗi tải file)
             * Chức năng: Bắt ngoại lệ khi không thể đọc file hoặc dữ liệu bị hỏng.
             * Ghi log lỗi và trả về null để luồng chính rẽ nhánh tạo ván đấu mới an toàn.
             */
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    public static void deleteSaveFile() {
        File file = new File(SAVE_FILE);
        if (file.exists()) file.delete();
    }
}
