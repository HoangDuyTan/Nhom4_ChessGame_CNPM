package view;

import model.Board;
import model.MoveLog;
import model.Position;
import model.SaveGameData;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.txt";
    private static final String SAVE_FILE_AI = "savegame_ai.txt";

    public static void saveGameData(Color currentTurn, int secondsElapsed, List<MoveLog> moves) {
        saveGameData(currentTurn, secondsElapsed, 0, moves, false);
    }

    public static void saveGameData(Color currentTurn, int secondsElapsed, int undoCount, List<MoveLog> moves, boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile))) {
            bw.write(currentTurn == Color.WHITE ? "W" : "B");
            bw.newLine();
            bw.write(String.valueOf(secondsElapsed));
            bw.newLine();
            bw.write(String.valueOf(undoCount));
            bw.newLine();

            for (MoveLog move : moves) {
                Position from = move.getFrom();
                Position to = move.getTo();
                bw.write(from.getR() + "," + from.getC() + "," + to.getR() + "," + to.getC());
                if (move.getPromotionChoice() != null) {
                    bw.write("," + Board.normalizePromotionChoice(move.getPromotionChoice()));
                }
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SaveGameData loadGameData() {
        return loadGameData(false);
    }

    public static SaveGameData loadGameData(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            SaveGameData data = new SaveGameData();
            String turn = br.readLine();
            data.setTurn("W".equals(turn) ? Color.WHITE : Color.BLACK);
            data.setSecondsElapsed(Integer.parseInt(br.readLine()));

            List<String> remainingLines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                remainingLines.add(line);
            }

            int firstMoveIndex = 0;
            if (!remainingLines.isEmpty() && remainingLines.get(0).matches("\\d+")) {
                data.setUndoCount(Integer.parseInt(remainingLines.get(0)));
                firstMoveIndex = 1;
            }

            for (int i = firstMoveIndex; i < remainingLines.size(); i++) {
                data.getMoves().add(remainingLines.get(i));
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSaveFile() {
        return hasSaveFile(false);
    }

    public static boolean hasSaveFile(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        return new File(saveFile).exists();
    }

    public static void deleteSaveFile() {
        deleteSaveFile(false);
    }

    public static void deleteSaveFile(boolean playWithAI) {
        String saveFile = playWithAI ? SAVE_FILE_AI : SAVE_FILE;
        File file = new File(saveFile);
        if (file.exists()) file.delete();
    }
}
