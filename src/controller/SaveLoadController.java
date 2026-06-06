package controller;

import model.Board;
import model.MoveLog;
import model.Position;
import model.SaveGameData;
import view.SaveManager;

import java.awt.Color;
import java.util.List;

public class SaveLoadController {
    public static void loadGame(Board board, GameController controller) {
        loadGame(board, controller, false);
    }

    public static void loadGame(Board board, GameController controller, boolean playWithAI) {
        SaveGameData data = SaveManager.loadGameData(playWithAI);
        if (data == null) return;

        board.reset();
        controller.clearHistory();
        for (String moveLine : data.getMoves()) {
            String[] p = moveLine.split(",");
            Position from = new Position(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
            Position to = new Position(Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            String promotionChoice = p.length > 4 ? Board.normalizePromotionChoice(p[4]) : null;
            controller.replayMoveForLoad(from, to, promotionChoice);
            controller.getMoveHistory().add(new MoveLog(from, to, null, null, Color.WHITE, promotionChoice));
        }
        controller.setCurrentTurn(data.getTurn());
        controller.setSecondsElapsed(data.getSecondsElapsed());
        controller.setUndoCount(data.getUndoCount());
    }

    public static void autoSave(Color turn, int secondsElapsed, List<MoveLog> moves) {
        autoSave(turn, secondsElapsed, 0, moves, false);
    }

    public static void autoSave(Color turn, int secondsElapsed, int undoCount, List<MoveLog> moves, boolean playWithAI) {
        SaveManager.saveGameData(turn, secondsElapsed, undoCount, moves, playWithAI);
    }
}
