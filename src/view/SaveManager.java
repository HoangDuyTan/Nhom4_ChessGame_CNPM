package view;

import model.*;

import java.awt.*;
import java.io.*;
import java.util.List;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.txt";

    /**
     * Chức năng: Thực thi tuần tự hóa (Serialization) trạng thái game ra tệp tin cấu hình.
     * Ánh xạ các Use Case phân rã thành phần:
     */
    public static void saveGameData(Color currentTurn, int secondsElapsed, List<MoveLog> moves) {
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
    public static SaveGameData loadGameData() {
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE)))
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    /**
     * Chức năng: Xóa tệp dữ liệu lưu ván đấu.
     * Ánh xạ (SR3 của UC-07): Được gọi khi có người chơi đầu hàng để xóa trạng thái lưu cũ.
     */
    public static void deleteSaveFile() {
        File file = new File(SAVE_FILE);
        if (file.exists()) file.delete();
    }
}
