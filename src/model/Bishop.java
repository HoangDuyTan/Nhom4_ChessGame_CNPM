package model;

import java.awt.Color;

public class Bishop extends Piece {
	public Bishop(Color color) {
		super(color, (color == Color.WHITE) ? 'B' : 'b');
	}

}
