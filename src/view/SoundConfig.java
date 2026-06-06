package view;

import java.io.*;

public class SoundConfig {

    private static final String FILE_NAME = "sound.config";

    public static void save(boolean enabled) {
        try (PrintWriter writer = new PrintWriter(FILE_NAME)) {
            writer.println(enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            return Boolean.parseBoolean(reader.readLine());
        } catch (Exception e) {
            return true;
        }
    }
}