package test;

import controller.GameController;
import model.AIMove;
import model.Bishop;
import model.Board;
import model.King;
import model.Knight;
import model.Pawn;
import model.Piece;
import model.Position;
import model.Queen;
import model.Rook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.Timer;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIMoveTest {
    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() throws Exception {
        board = new Board();
        clearBoard(board);
        controller = new GameController(board, null, true);
        controller.setCurrentTurn(Color.BLACK);
        stopMainTimer();
    }

    @AfterEach
    void tearDown() throws Exception {
        stopMainTimer();
    }

    private void clearBoard(Board board) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board.set(new Position(r, c), null);
            }
        }
    }

    private void stopMainTimer() throws Exception {
        Field timerField = GameController.class.getDeclaredField("gameTimer");
        timerField.setAccessible(true);
        Timer timer = (Timer) timerField.get(controller);
        if (timer != null) {
            timer.stop();
        }
    }

    private Object invokePrivate(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = GameController.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(controller, args);
    }

    @Test
    void chooseAIMoveShouldPreferCapturingHighValuePiece() throws Exception {
        board.set(new Position(7, 7), new King(Color.BLACK));
        board.set(new Position(0, 7), new King(Color.WHITE));
        board.set(new Position(4, 4), new Queen(Color.BLACK));
        board.set(new Position(4, 0), new Queen(Color.WHITE));
        board.set(new Position(5, 5), new Pawn(Color.WHITE));

        AIMove bestMove = (AIMove) invokePrivate("chooseAIMove", new Class<?>[]{});

        assertNotNull(bestMove, "AI must find at least one legal move.");
        assertEquals(new Position(4, 4), bestMove.getFrom());
        assertEquals(new Position(4, 0), bestMove.getTo());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLegalMovesShouldExcludeMoveThatLeavesKingInCheck() throws Exception {
        board.set(new Position(7, 4), new King(Color.BLACK));
        board.set(new Position(0, 4), new King(Color.WHITE));
        board.set(new Position(7, 0), new Rook(Color.WHITE));
        board.set(new Position(7, 2), new Rook(Color.BLACK));

        List<AIMove> legalMoves = (List<AIMove>) invokePrivate(
                "getLegalMoves",
                new Class<?>[]{Color.class},
                Color.BLACK
        );

        assertFalse(legalMoves.isEmpty(), "AI should still have other legal moves.");
        assertTrue(
                legalMoves.stream().noneMatch(move ->
                        move.getFrom().equals(new Position(7, 2))
                                && move.getTo().equals(new Position(6, 2))
                ),
                "AI must not choose a move that exposes its own king."
        );
    }

    @Test
    void scoreAIMoveShouldRestoreBoardAfterSimulation() throws Exception {
        board.set(new Position(7, 7), new King(Color.BLACK));
        board.set(new Position(0, 7), new King(Color.WHITE));
        board.set(new Position(3, 3), new Queen(Color.BLACK));
        board.set(new Position(5, 3), new Queen(Color.WHITE));

        Method scoreAIMoveMethod = GameController.class.getDeclaredMethod("scoreAIMove", AIMove.class);
        scoreAIMoveMethod.setAccessible(true);
        scoreAIMoveMethod.invoke(controller, new AIMove(new Position(3, 3), new Position(5, 3)));

        Piece restoredBlackQueen = board.get(new Position(3, 3));
        Piece restoredWhiteQueen = board.get(new Position(5, 3));

        assertNotNull(restoredBlackQueen);
        assertTrue(restoredBlackQueen instanceof Queen);
        assertEquals(Color.BLACK, restoredBlackQueen.getColor());
        assertNotNull(restoredWhiteQueen);
        assertTrue(restoredWhiteQueen instanceof Queen);
        assertEquals(Color.WHITE, restoredWhiteQueen.getColor());
    }

    @Test
    void pieceValueShouldRankPiecesByChessStrength() throws Exception {
        int pawn = (int) invokePrivate("pieceValue", new Class<?>[]{Piece.class}, new Pawn(Color.BLACK));
        int knight = (int) invokePrivate("pieceValue", new Class<?>[]{Piece.class}, new Knight(Color.BLACK));
        int bishop = (int) invokePrivate("pieceValue", new Class<?>[]{Piece.class}, new Bishop(Color.BLACK));
        int rook = (int) invokePrivate("pieceValue", new Class<?>[]{Piece.class}, new Rook(Color.BLACK));
        int queen = (int) invokePrivate("pieceValue", new Class<?>[]{Piece.class}, new Queen(Color.BLACK));
        int king = (int) invokePrivate("pieceValue", new Class<?>[]{Piece.class}, new King(Color.BLACK));

        assertTrue(pawn < knight);
        assertTrue(knight <= bishop);
        assertTrue(bishop < rook);
        assertTrue(rook < queen);
        assertTrue(queen < king);
    }
}
