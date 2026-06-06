package view;

import model.*;

import java.util.List;
import java.awt.*;
import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.txt";
    private static final String SAVE_FILE_AI = "savegame_ai.txt";
    /**
     * Chức năng: Thực thi tuần tự hóa (Serialization) trạng thái game ra tệp tin cấu hình.
     * Ánh xạ các Use Case phân rã thành phần:
     */
    public static void saveGameData(Color currentTurn, int secondsElapsed, List<MoveLog> moves,boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
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
            for (MoveLog move : moves) {
                Position from = move.getFrom();
                Position to = move.getTo();
                bw.write(from.getR() + "," + from.getC() + "," + to.getR() + "," + to.getC());
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
    public static SaveGameData loadGameData(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        try (BufferedReader br = new BufferedReader(new FileReader(saveFile)))
        {
            SaveGameData data = new SaveGameData();
            String turn = br.readLine();
            data.setTurn(turn.equals("W") ? Color.WHITE : Color.BLACK);
            data.setSecondsElapsed(Integer.parseInt(br.readLine()));
            String line;
            while ((line = br.readLine()) != null) {
                data.getMoves().add(line);
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

    public static boolean hasSaveFile(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        return new File(saveFile).exists();
    }

    /**
     * Chức năng: Xóa tệp dữ liệu lưu ván đấu.
     * Ánh xạ (SR3 của UC-07): Được gọi khi có người chơi đầu hàng để xóa trạng thái lưu cũ.
     */
    public static void deleteSaveFile(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        File file = new File(saveFile);
        if (file.exists()) file.delete();
    }
}
