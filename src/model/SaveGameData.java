package model;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SaveGameData {
    private Color turn;
    private int secondsElapsed;
    private List<String> moves = new ArrayList<>();
    public Color getTurn() {
        return turn;
    }

    public void setTurn(Color turn) {
        this.turn = turn;
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    public void setSecondsElapsed(int secondsElapsed) {
        this.secondsElapsed = secondsElapsed;
    }

    public List<String> getMoves() {
        return moves;
    }
}