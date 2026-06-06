package controller;

import model.Board;
import model.MoveLog;
import model.Position;
import model.SaveGameData;
import view.SaveManager;
import java.util.List;
import java.awt.*;

public class SaveLoadController {
    public static void loadGame(Board board, GameController controller) {
        SaveGameData data = SaveManager.loadGameData();
        if(data == null) return;
        board.reset();
        controller.clearHistory();
        for(String moveLine : data.getMoves()) {
            String[] p = moveLine.split(",");
            Position from = new Position(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
            Position to = new Position(Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            controller.replayMoveForLoad(from, to);
        }
        controller.setCurrentTurn(data.getTurn());
        controller.setSecondsElapsed(data.getSecondsElapsed());
    }

    /**
     * Chức năng: Điều phối luồng dữ liệu trung gian phục vụ lưu trữ tự động.
     * Tương ứng mã Use Case gốc: UC-04.1 (Tự động lưu ván đấu).
     */
    public static void autoSave(Color turn, int secondsElapsed, List<MoveLog> moves) {
        SaveManager.saveGameData(turn, secondsElapsed, moves);
    }
}
