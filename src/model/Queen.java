package model;

import java.awt.Color;

public class Queen extends Piece {
    public Queen(Color color) {
        super(color, (color == Color.WHITE) ? 'Q' : 'q');
    }


}