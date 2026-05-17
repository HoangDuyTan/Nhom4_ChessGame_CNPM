package controller;

import model.Board;
import view.SaveManager;

import java.awt.*;

public class SaveLoadController {
    public static void loadGame(Board board, GameController controller) {
        String[] data = SaveManager.loadGameData();

        if (data != null) {
            /* * MÃ USE CASE: UC-04.2.2 (Khôi phục Lượt & Giờ)
             * Chức năng: Trích xuất dòng 0 (Lượt đi) và dòng 1 (Thời gian đếm giây)
             * để cập nhật lại trạng thái trận đấu vào GameController.
             */
            Color loadedTurn = data[0].equals("W") ? Color.WHITE : Color.BLACK;
            controller.setCurrentTurn(loadedTurn);
            int loadedSeconds = Integer.parseInt(data[1]);
            controller.setSecondsElapsed(loadedSeconds);

            /* * MÃ USE CASE: UC-04.2.3 (Khởi tạo lại bàn cờ) - Bước trung gian
             * Chức năng: Cắt 8 dòng còn lại từ mảng dữ liệu và truyền vào Model Board xử lý.
             */
            String[] boardRows = new String[8];
            System.arraycopy(data, 2, boardRows, 0, 8);

            board.loadGame(boardRows);
        }
    }

    /**
     * Chức năng: Điều phối luồng dữ liệu trung gian phục vụ lưu trữ tự động.
     * Tương ứng mã Use Case gốc: UC-04.1 (Tự động lưu ván đấu).
     */
    public static void autoSave(Color colorTurn, Board board, int secondsElapsed) {
        SaveManager.saveGameData(colorTurn, board, secondsElapsed);
    }
}
