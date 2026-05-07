package view;

import java.io.*;

public class ThemeManager {

    private static final String FILE_NAME =
            "theme.txt";
    public static void saveTheme(String themeName) {

        try {

            BufferedWriter bw =
                    new BufferedWriter(
                            new FileWriter(FILE_NAME)
                    );

            bw.write(themeName);

            bw.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static String loadTheme() {

        try {

            BufferedReader br =
                    new BufferedReader(
                            new FileReader(FILE_NAME)
                    );

            String theme =
                    br.readLine();

            br.close();

            return theme;

        } catch (Exception e) {

            return "Brown";
        }
    }
}