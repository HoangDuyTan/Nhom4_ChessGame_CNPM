package test;

import model.*;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private void clearBoard(Board board) {
        for(int r = 0; r < 8; r++) {
            for(int c = 0; c < 8; c++) {
                board.set(new Position(r,c), null);
            }
        }
    }
    @Test
    void testEnPassant() {
        Board board = new Board();
        clearBoard(board);
        Pawn whitePawn = new Pawn(Color.WHITE);
        Pawn blackPawn = new Pawn(Color.BLACK);
        board.set(new Position(4,4), whitePawn);
        board.set(new Position(6,5), blackPawn);
        assertTrue(board.move(new Position(6,5), new Position(4,5)));
        assertTrue(board.move(new Position(4,4), new Position(5,5)));
        assertNull(board.get(new Position(4,5)));
        assertTrue(board.get(new Position(5,5)) instanceof Pawn);
    }
    @Test
    void testKingSideCastling() {
        Board board = new Board();
        board.set(new Position(0,5), null);
        board.set(new Position(0,6), null);
        assertTrue(board.move(new Position(0,4), new Position(0,6)));
        Piece king = board.get(new Position(0,6));
        Piece rook = board.get(new Position(0,5));
        assertTrue(king instanceof King);
        assertTrue(rook instanceof Rook);
        assertNull(board.get(new Position(0,4)));
        assertNull(board.get(new Position(0,7)));
    }
    @Test
    void testQueenSideCastling() {
        Board board = new Board();
        board.set(new Position(0,1), null);
        board.set(new Position(0,2), null);
        board.set(new Position(0,3), null);
        assertTrue(board.move(new Position(0,4), new Position(0,2)));
        assertTrue(board.get(new Position(0,2)) instanceof King);
        assertTrue(board.get(new Position(0,3)) instanceof Rook);
    }
    @Test
    void testCastlingFailWhenKingMoved() {
        Board board = new Board();
        clearBoard(board);
        King king = new King(Color.WHITE);
        king.setMoved(true);
        Rook rook = new Rook(Color.WHITE);
        board.set(new Position(0,4), king);
        board.set(new Position(0,7), rook);
        assertFalse(board.move(new Position(0,4), new Position(0,6)));
    }
    @Test
    void testCastlingFailWhenRookMoved() {
        Board board = new Board();
        clearBoard(board);
        King king = new King(Color.WHITE);
        Rook rook = new Rook(Color.WHITE);
        rook.setMoved(true);
        board.set(new Position(0,4), king);
        board.set(new Position(0,7), rook);
        assertFalse(board.move(new Position(0,4), new Position(0,6)));
    }
    @Test
    void testPromotionToQueen() {
        Board board = new Board();
        clearBoard(board);
        Pawn pawn = new Pawn(Color.WHITE);
        board.set(new Position(6,0), pawn);
        board.set(new Position(7,0), new Queen(Color.WHITE));
        Piece promoted = board.get(new Position(7,0));
        assertTrue(promoted instanceof Queen);
    }
    @Test
    void testCastlingSetsKingAndRookMovedFlags() {
        Board board = new Board();
        board.set(new Position(0,5), null);
        board.set(new Position(0,6), null);

        assertTrue(board.move(new Position(0,4), new Position(0,6)));

        Piece king = board.get(new Position(0,6));
        Piece rook = board.get(new Position(0,5));
        assertTrue(king instanceof King);
        assertTrue(rook instanceof Rook);
        assertTrue(king.hasMoved());
        assertTrue(rook.hasMoved());
    }

    @Test
    void testCastlingFailWhenKingPassesThroughAttackedSquare() {
        Board board = new Board();
        clearBoard(board);
        board.set(new Position(0,4), new King(Color.WHITE));
        board.set(new Position(0,7), new Rook(Color.WHITE));
        board.set(new Position(7,5), new Rook(Color.BLACK));

        assertFalse(board.move(new Position(0,4), new Position(0,6)));
        assertTrue(board.get(new Position(0,4)) instanceof King);
        assertTrue(board.get(new Position(0,7)) instanceof Rook);
        assertNull(board.get(new Position(0,5)));
        assertNull(board.get(new Position(0,6)));
    }

    @Test
    void testEnPassantTargetSetAfterDoublePawnMove() {
        Board board = new Board();

        assertTrue(board.move(new Position(6,5), new Position(4,5)));

        assertEquals(new Position(5,5), board.getEnPassantTarget());
    }

    @Test
    void testEnPassantCaptureRemovesPassedPawnAndClearsTarget() {
        Board board = new Board();
        clearBoard(board);
        board.set(new Position(4,4), new Pawn(Color.WHITE));
        board.set(new Position(6,5), new Pawn(Color.BLACK));

        assertTrue(board.move(new Position(6,5), new Position(4,5)));
        assertTrue(board.move(new Position(4,4), new Position(5,5)));

        assertNull(board.get(new Position(4,4)));
        assertNull(board.get(new Position(4,5)));
        assertTrue(board.get(new Position(5,5)) instanceof Pawn);
        assertNull(board.getEnPassantTarget());
    }

    @Test
    void testPromotionMoveToQueenWithExplicitChoice() {
        Board board = new Board();
        clearBoard(board);
        board.set(new Position(6,0), new Pawn(Color.WHITE));

        assertTrue(board.move(new Position(6,0), new Position(7,0), "Queen"));

        assertNull(board.get(new Position(6,0)));
        assertTrue(board.get(new Position(7,0)) instanceof Queen);
        assertTrue(board.get(new Position(7,0)).hasMoved());
    }

    @Test
    void testPromotionMoveUsesExplicitChoice() {
        Board board = new Board();
        clearBoard(board);
        board.set(new Position(1,7), new Pawn(Color.BLACK));

        assertTrue(board.move(new Position(1,7), new Position(0,7), "Knight"));

        assertNull(board.get(new Position(1,7)));
        assertTrue(board.get(new Position(0,7)) instanceof Knight);
        assertEquals(Color.BLACK, board.get(new Position(0,7)).getColor());
    }
}
