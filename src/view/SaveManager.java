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
    public static void saveGameData(Color currentTurn, int secondsElapsed,int undoCount,List<MoveLog> moves,boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        /* UC-04.1.5: Write Save File
         * Ghi lần lượt Turn, Time, Undo Count và Move History xuống tệp savegame nhằm lưu trạng thái hiện tại của ván đấu.
         */
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            /* UC-04.1.1: Extract Turn
             * Lấy thông tin lượt chơi hiện tại từ GameController và mã hóa thành W (White) hoặc B (Black)
             * để lưu vào dòng đầu tiên của file save.
             */
            bw.write(currentTurn == Color.WHITE ? "W" : "B");
            bw.newLine();

            /* UC-04.1.2: Extract Time
             * Trích xuất dữ liệu thời gian trận đấu đã được đóng gói trong biến secondsElapsed để phục vụ khôi phục sau này.
             */
            bw.write(String.valueOf(secondsElapsed));
            bw.newLine();
            /* UC-04.1.3: Extract Undo Count
             * Lấy số lần Undo đã sử dụng trong lượt hiện tại để duy trì đúng giới hạn Undo khi tải lại ván đấu.
             */
            bw.write(String.valueOf(undoCount));
            bw.newLine();
            /* UC-04.1.4: Extract Move History
             * Duyệt toàn bộ lịch sử nước đi và chuyển đổi mỗi nước đi thành chuỗi tọa độ from-to
             * để lưu xuống file save.
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
        System.out.println(new File(saveFile).getAbsolutePath());
        /* UC-04.2.1: Read Save Game File
         * Đọc dữ liệu đã lưu từ tệp savegame
         * và ánh xạ thành đối tượng SaveGameData.
         */
        try (BufferedReader br = new BufferedReader(new FileReader(saveFile)))
        {
            SaveGameData data = new SaveGameData();
            String turn = br.readLine();
            data.setTurn(turn.equals("W") ? Color.WHITE : Color.BLACK);
            data.setSecondsElapsed(Integer.parseInt(br.readLine()));
            data.setUndoCount(Integer.parseInt(br.readLine()));
            String line;
            while ((line = br.readLine()) != null) {
                data.getMoves().add(line);
            }

            return data;
        } catch (Exception e) {
            /* UC-04.2.1.5: Handle Load Error
             * Bắt ngoại lệ khi file không tồn tại hoặc dữ liệu lỗi.
             * Trả về null để luồng gọi xử lý an toàn.
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
