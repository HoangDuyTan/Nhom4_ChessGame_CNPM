package model;

import java.awt.Color;

public class Knight extends Piece {
    public Knight(Color color) {
        super(color, (color == Color.WHITE) ? 'N' : 'n');
    }

}