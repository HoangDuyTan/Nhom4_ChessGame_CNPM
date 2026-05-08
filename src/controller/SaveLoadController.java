package controller;

import model.Board;
import view.SaveManager;

import java.awt.*;

public class SaveLoadController {
    public static void loadGame(Board board, GameController controller) {
        String[] data = SaveManager.loadGameData();
        if (data != null) {
            Color loadedTurn = data[0].equals("W") ? Color.WHITE : Color.BLACK;
            controller.setCurrentTurn(loadedTurn);

            String[] boardRows = new String[8];
            System.arraycopy(data, 1, boardRows, 0, 8);
            board.loadGame(boardRows);
        }
    }

    public static void autoSave(Color colorTurn, Board board) {
        SaveManager.saveGameData(colorTurn, board);
    }
}
