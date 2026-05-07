package model;

import java.awt.Color;

public class King extends Piece {
    public King(Color color) {
        super(color, (color == Color.WHITE) ? 'K' : 'k');

    }
}