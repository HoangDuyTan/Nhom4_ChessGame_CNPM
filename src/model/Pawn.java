package model;

import java.awt.Color;

public class Pawn extends Piece {
    public Pawn(Color color) {
        super(color, (color == Color.WHITE) ? 'P' : 'p');
    }

}