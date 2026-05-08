package view;

import model.Board;
import model.Piece;
import model.Position;

import java.awt.*;
import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "savegame.txt";

    public static void saveGameData(Color currentTurn, Board board) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            bw.write(currentTurn == Color.WHITE ? "W" : "B");
            bw.newLine();

            for (int r = 0; r < 8; r++) {
                StringBuilder rowStr = new StringBuilder();
                for (int c = 0; c < 8; c++) {
                    Piece p = board.get(new Position(r, c));
                    if (p == null) {
                        rowStr.append("-");
                    } else {
                        rowStr.append(p.getShortName());
                    }
                }
                bw.write(rowStr.toString());
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] loadGameData() {
        String[] data = new String[9];
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE))) {
            for (int i = 0; i < 9; i++) {
                data[i] = br.readLine();
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean hasSaveFile() {
        return new File(SAVE_FILE).exists();
    }

    public static void deleteSaveFile() {
        File file = new File(SAVE_FILE);
        if (file.exists()) file.delete();
    }
}
