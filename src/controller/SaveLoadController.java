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

            int loadedSeconds = Integer.parseInt(data[1]);

            controller.setSecondsElapsed(loadedSeconds);

            String[] boardRows = new String[8];

            System.arraycopy(data, 2, boardRows, 0, 8);

            board.loadGame(boardRows);
        }
    }

    public static void autoSave(Color colorTurn, Board board, int secondsElapsed) {
        SaveManager.saveGameData(colorTurn, board, secondsElapsed);
    }
}
