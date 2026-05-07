package model;
	
public class Position {
	private final int r; 
    private final int c; 
    
    public Position(int r, int c) {
        this.r = r;
        this.c = c;
    }
    
    public int getR() { return r; }
    public int getC() { return c; }
    
    public boolean isValid() {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }
    @Override
    public String toString() {
        char colChar = (char) ('a' + c);
        return "" + colChar + (r + 1);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        return r == other.r && c == other.c; 
    }

    @Override
    public int hashCode() {
        return 31 * r + c; 
    }
}
