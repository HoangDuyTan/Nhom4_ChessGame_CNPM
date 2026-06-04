package view;

import model.*;

import java.awt.*;
import java.io.*;
import java.util.List;

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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSaveFile(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        return new File(saveFile).exists();
    }

    /**
     * [SR3 của UC-07]: Tính toàn vẹn dữ liệu
     * Chức năng: Xóa tệp dữ liệu lưu ván đấu (savegame.txt hoặc savegame_ai.txt).
     * Được kích hoạt khi có người chơi Đầu hàng hoặc ván đấu kết thúc
     * để đảm bảo không thể khôi phục lại trận đấu đã hạ màn.
     */
    public static void deleteSaveFile(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        File file = new File(saveFile);
        if (file.exists()) file.delete();
    }
}
