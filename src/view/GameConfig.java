package view;

import java.io.*;

public class GameConfig {

    private static final String FILE_NAME = "game.config";

    private static boolean advancedMode = load();

    public static boolean isAdvancedMode() {
        return advancedMode;
    }

    public static void setAdvancedMode(boolean enabled) {
        advancedMode = enabled;
        save(enabled);
    }

    private static void save(boolean enabled) {
        try (PrintWriter writer = new PrintWriter(FILE_NAME)) {
            writer.println(enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            return Boolean.parseBoolean(reader.readLine());
        } catch (Exception e) {
            return false;
        }
    }
}