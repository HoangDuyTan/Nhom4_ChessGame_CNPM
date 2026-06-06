package view;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {

    private static boolean soundEnabled = true;

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    private static void play(String path) {

        if (!soundEnabled) {
            return;
        }

        try {
            File file = new File(path);

            AudioInputStream audio = AudioSystem.getAudioInputStream(file);

            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();

        } catch (Exception e) {
            System.out.println("Không thể phát âm thanh: " + path);
            e.printStackTrace();
        }
    }

    public static void playMove() {
        play("assets/sounds/move.wav");
    }

    public static void playWin() {
        play("assets/sounds/win.ogg");
    }

    public static void playLose() {
        play("assets/sounds/game_over.wav");
    }

    public static void playButton() {
        play("assets/sounds/button_click.wav");
    }

}