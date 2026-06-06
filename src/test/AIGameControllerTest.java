package test;

import controller.GameController;
import model.Board;
import model.King;
import model.MoveLog;
import model.Pawn;
import model.Piece;
import model.Position;
import model.Queen;
import view.SaveManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

public class AIGameControllerTest {
    private Board board;
    private GameController controller;

    @BeforeEach
    void setUp() {
        board = new Board();
        controller = new GameController(board, null, true);
    }

    @AfterEach
    void tearDown() {
        SaveManager.deleteSaveFile(true);
    }

    @Test
    void aiAutomaticallyMovesAfterPlayerMove() throws InterruptedException {
        controller.handleSquareClick(1, 0);
        controller.handleSquareClick(2, 0);

        waitForMoveHistorySize(2);

        assertEquals(2, controller.getMoveHistory().size(), "Máy phải tự đi thêm 1 nước sau lượt người chơi.");
        assertEquals(Color.WHITE, controller.getCurrentTurn(), "Sau khi máy đi xong, lượt phải quay lại quân Trắng.");
        assertEquals(Color.BLACK, controller.getMoveHistory().get(1).getPlayerColor(), "Nước thứ hai phải là nước của máy/quân Đen.");
        assertFalse(board.isInCheck(Color.BLACK), "AI không được chọn nước khiến Vua Đen tự bị chiếu.");
    }

    @Test
    void aiPrefersCapturingHighValuePieceWhenAvailable() throws InterruptedException {
        clearBoard();
        board.set(new Position(0, 4), new King(Color.WHITE));
        board.set(new Position(7, 4), new King(Color.BLACK));
        board.set(new Position(1, 7), new Pawn(Color.WHITE));
        board.set(new Position(4, 3), new Queen(Color.BLACK));
        board.set(new Position(4, 0), new Queen(Color.WHITE));

        controller.handleSquareClick(1, 7);
        controller.handleSquareClick(2, 7);

        waitForMoveHistorySize(2);

        Piece pieceOnTarget = board.get(new Position(4, 0));
        MoveLog aiMove = controller.getMoveHistory().get(1);

        assertTrue(pieceOnTarget instanceof Queen, "AI nên dùng Hậu Đen để ăn quân có giá trị cao.");
        assertEquals(Color.BLACK, pieceOnTarget.getColor(), "Ô mục tiêu phải thuộc về quân Đen sau nước đi của AI.");
        assertEquals(new Position(4, 3), aiMove.getFrom(), "AI phải chọn nước ăn Hậu Trắng từ vị trí đang có lợi nhất.");
        assertEquals(new Position(4, 0), aiMove.getTo(), "AI phải đi tới ô có Hậu Trắng để ăn quân.");
        assertTrue(aiMove.getCapturedPiece() instanceof Queen, "Lịch sử nước đi phải ghi nhận AI đã ăn Hậu.");
    }

    @Test
    void playerCannotControlBlackPiecesInAIGame() {
        controller.setCurrentTurn(Color.BLACK);

        controller.handleSquareClick(6, 0);
        controller.handleSquareClick(5, 0);

        assertNotNull(board.get(new Position(6, 0)), "Quân Đen không được bị người chơi kéo đi trong chế độ AI.");
        assertNull(board.get(new Position(5, 0)), "Ô đích vẫn phải trống vì lượt Đen thuộc về máy.");
        assertTrue(controller.getMoveHistory().isEmpty(), "Không được ghi lịch sử cho nước đi bị chặn.");
        assertEquals(Color.BLACK, controller.getCurrentTurn(), "Lượt vẫn phải là Đen khi người chơi cố điều khiển máy.");
    }

    private void waitForMoveHistorySize(int expectedSize) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (controller.getMoveHistory().size() >= expectedSize) {
                return;
            }
            Thread.sleep(50);
        }
        fail("AI không tự hoàn thành nước đi trong thời gian chờ.");
    }

    private void clearBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board.set(new Position(row, col), null);
            }
        }
    }
}
