package model;

import java.awt.Color;

public abstract class Piece {
	protected Color color;
	private char shortName;
    protected boolean hasMoved = false;
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
    public boolean hasMoved() {
        return hasMoved;
    }
    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }
    /**
     * Phương thức kiểm tra luật đi cơ bản (Hình học di chuyển) của riêng từng quân cờ.
     * Phương thức này phục vụ trực tiếp cho việc xác thực luật trong hàm `board.move()`.
     */
    public abstract boolean isValidMove(Position from, Position to, Board board);
	
}
