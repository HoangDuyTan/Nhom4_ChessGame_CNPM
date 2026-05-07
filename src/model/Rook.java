package model;

import java.awt.Color;

public class Rook extends Piece {
    public Rook(Color color) {
        super(color, (color == Color.WHITE) ? 'R' : 'r');
    }
   
}