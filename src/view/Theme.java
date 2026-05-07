package view;

import java.awt.Color;

public class Theme {

    public static Color DARK_SQUARE_COLOR;
    public static Color LIGHT_SQUARE_COLOR;
    public static Color BORDER_COLOR;
    public static Color CONTROL_PANEL_BG;
    public static Color BUTTON_COLOR;

    static {

        loadSavedTheme();
    }
    public static void setBrownTheme() {

        DARK_SQUARE_COLOR =
                new Color(181, 136, 99);

        LIGHT_SQUARE_COLOR =
                new Color(240, 217, 181);

        BORDER_COLOR =
                new Color(70, 70, 70);

        CONTROL_PANEL_BG =
                new Color(245, 245, 245);

        BUTTON_COLOR =
                new Color(60, 130, 200);
    }

    public static void setBlueTheme() {

        DARK_SQUARE_COLOR =
                new Color(112, 135, 163);

        LIGHT_SQUARE_COLOR =
                new Color(235, 236, 208);

        BORDER_COLOR =
                new Color(80, 95, 115);

        CONTROL_PANEL_BG =
                new Color(241, 241, 241);

        BUTTON_COLOR =
                new Color(112, 135, 163);
    }

    public static void setPinkTheme() {

        DARK_SQUARE_COLOR =
                new Color(214, 160, 173);

        LIGHT_SQUARE_COLOR =
                new Color(255, 240, 245);

        BORDER_COLOR =
                new Color(170, 120, 135);
        CONTROL_PANEL_BG =
                new Color(252, 244, 246); 
        BUTTON_COLOR =
                new Color(244, 182, 198);
    }

    public static void setGreenTheme() {

        DARK_SQUARE_COLOR =
                new Color(118, 150, 86);

        LIGHT_SQUARE_COLOR =
                new Color(238, 238, 210);

        BORDER_COLOR =
                new Color(60, 60, 60);

        CONTROL_PANEL_BG =
                new Color(235, 235, 235);

        BUTTON_COLOR =
                new Color(118, 150, 86);
    }
    public static void loadSavedTheme() {

        String theme =
                ThemeManager.loadTheme();

        switch (theme) {

            case "Pink":

                setPinkTheme();
                break;

            case "Blue":

                setBlueTheme();
                break;

            case "Green":

                setGreenTheme();
                break;

            default:

                setBrownTheme();
        }
    }
}