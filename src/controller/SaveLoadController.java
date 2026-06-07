package controller;

import model.Board;
import model.MoveLog;
import model.Position;
import model.SaveGameData;
import view.SaveManager;
import java.util.List;
import java.awt.*;

public class SaveLoadController {
    public static void loadGame(Board board, GameController controller,boolean playWithAI) {
        /* UC-04.2.1: Read Save Game File
         * Đọc dữ liệu đã lưu từ tệp savegame và ánh xạ thành đối tượng SaveGameData.
         */
        SaveGameData data = SaveManager.loadGameData(playWithAI);
        /* UC-04.2.1.1: Restore Board
         * Khởi tạo bàn cờ về trạng thái ban đầu, sau đó phát lại toàn bộ lịch sử nước đi
         * để tái tạo thế cờ đã lưu.
         */
        if(data == null) return;
        controller.clearHistory();
        board.reset();
        for(String moveLine : data.getMoves()) {
            String[] p = moveLine.split(",");
            Position from = new Position(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
            Position to = new Position(Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            controller.replayMoveForLoad(from, to);
            controller.getMoveHistory().add(new MoveLog(from, to, null, null, Color.WHITE));
        }
        /* UC-04.2.1.2: Restore Turn
         * Khôi phục lượt chơi hiện tại từ dữ liệu đã lưu.
         */
        controller.setCurrentTurn(data.getTurn());
        /* UC-04.2.1.3: Restore Time
         * Khôi phục thời gian của hai bên từ dữ liệu đã lưu.
         */
        controller.setSecondsElapsed(data.getSecondsElapsed());
        /* UC-04.2.1.4: Restore Undo Count
         * Khôi phục số lần Undo đã được lưu trước đó để đảm bảo đúng luật chơi sau khi tải game.
         */
        controller.setUndoCount(data.getUndoCount());
    }

    /**
     * Chức năng: Điều phối luồng dữ liệu trung gian phục vụ lưu trữ tự động.
     * Tương ứng mã Use Case gốc: UC-04.1 (Tự động lưu ván đấu).
     */
    public static void autoSave(Color turn, int secondsElapsed,int undoCount, List<MoveLog> moves,boolean playWithAI) {
        SaveManager.saveGameData(turn, secondsElapsed,undoCount, moves,playWithAI);
    }
}
