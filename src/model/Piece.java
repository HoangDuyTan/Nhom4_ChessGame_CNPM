package model;

import java.awt.Color;

public abstract class Piece {
	protected Color color;
	private char shortName; 
	public Piece(Color color, char shortName){ 
		this.color=color; 
		this.shortName=shortName; 
		}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public char getShortName() {
		return shortName;
	}
	public void setShortName(char shortName) {
		this.shortName = shortName;
	}
    public abstract boolean isValidMove(Position from, Position to, Board board);
	
}
