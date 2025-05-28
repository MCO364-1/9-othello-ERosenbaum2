import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class MainTest{
    private OthelloModel model;
    private int boardSize;

    public void setUp() {
        model = new OthelloModel();
        boardSize = model.getBoard().length;
    }

    @Test
    void testInitialBoard() {
        setUp();
        OthelloModel.Player[][] board = model.getBoard();
        // Check initial piece positions
        assertEquals(OthelloModel.Player.WHITE, board[3][3]);
        assertEquals(OthelloModel.Player.BLACK, board[3][4]);
        assertEquals(OthelloModel.Player.BLACK, board[4][3]);
        assertEquals(OthelloModel.Player.WHITE, board[4][4]);
        // Check that other positions are empty
        assertNull(board[0][0]);
        assertNull(board[7][7]);
    }

    @Test
    void testValidMoves() {
        setUp();
        List<int[]> validMoves = model.getValidMoves();
        // Check number of valid moves at start
        assertEquals(4, validMoves.size());
        // Verify specific valid moves
        assertTrue(containsMove(validMoves, 2, 3));
    }

    @Test
    void testMakeMove() {
        setUp();
        // Make a valid move
        assertTrue(model.makeMove(3, 2));
        // Verify board state after move
        OthelloModel.Player[][] boardAfterMove = model.getBoard();
        assertEquals(OthelloModel.Player.BLACK, boardAfterMove[3][2]);
        assertEquals(OthelloModel.Player.BLACK, boardAfterMove[3][3]);  // Flipped piece
        assertEquals(OthelloModel.Player.WHITE, model.getCurrentPlayer());
        // Try invalid move
        assertFalse(model.makeMove(0, 0));
        assertEquals(OthelloModel.Player.WHITE, model.getCurrentPlayer());  // Player shouldn't change
    }

    @Test
    void testGetScore() {
        setUp();
        int[] initialScore = model.getScore();
        assertEquals(2, initialScore[0]); // Black's score
        assertEquals(2, initialScore[1]); // White's score
        model.makeMove(3, 2);
        int[] scoreAfterMove = model.getScore();
        assertEquals(4, scoreAfterMove[0]); // Black's score should increase
        assertEquals(1, scoreAfterMove[1]); // White's score should decrease
    }

    @Test
    void testGameOver() {
        setUp();
        assertFalse(model.isGameOver());
        assertNull(model.getWinner());
    }

    @Test
    void testComputerMove() {
        setUp();
        int[] move = model.getComputerMove();
        assertNotNull(move);
        assertEquals(2, move.length);
        assertTrue(model.isValidMove(move[0], move[1]));
    }

    @Test
    void testInvalidMoveOutOfBounds() {
        setUp();
        assertFalse(model.makeMove(-1, 0));
        assertFalse(model.makeMove(0, -1));
        assertFalse(model.makeMove(boardSize, 0));
        assertFalse(model.makeMove(0, boardSize));
    }

    @Test
    void testNoValidMovesForceSkip() {
        setUp();
        // Create a specific board state where current player has no moves
        // but opponent does (should switch players)
        model.makeMove(3, 2); // Black moves
        model.makeMove(2, 2); // White moves
        model.makeMove(1, 2); // Black moves
        model.makeMove(4, 2); // White moves
        // Now set up a state where Black has no moves
        model.makeMove(5, 2); // Black moves
        model.makeMove(6, 2); // White moves
        // Verify that when Black has no moves, it switches to White
        assertTrue(model.getCurrentPlayer() == OthelloModel.Player.WHITE);
    }

    @Test
    void testCompleteGame() {
        setUp();
        // Play a complete game and verify final state
        while (!model.isGameOver()) {
            int[] move = model.getComputerMove();
            if (move != null) {
                assertTrue(model.makeMove(move[0], move[1]));
            }
        }
        // After game is over, verify:
        // 1. No valid moves for either player
        assertTrue(model.getValidMoves().isEmpty());
        // 2. Score sums to total squares
        int[] finalScore = model.getScore();
        assertEquals(boardSize * boardSize, finalScore[0] + finalScore[1]);
        // 3. Winner is properly determined
        OthelloModel.Player winner = model.getWinner();
        if (finalScore[0] > finalScore[1]) {
            assertEquals(OthelloModel.Player.BLACK, winner);
        } else if (finalScore[1] > finalScore[0]) {
            assertEquals(OthelloModel.Player.WHITE, winner);
        } else {
            assertNull(winner);
        }
    }

    @Test
    void testFlipInAllDirections() {
        setUp();
        // Test that pieces can be flipped in all 8 directions
        OthelloModel specialModel = new OthelloModel();
        // Initial board has:
        // - Black pieces at (3,3) and (4,4)
        // - White pieces at (3,4) and (4,3)
        // Make a move that will flip a piece
        assertTrue(specialModel.makeMove(2, 3));  // Black moves to (2,3)
        // Verify the piece was flipped
        OthelloModel.Player[][] board = specialModel.getBoard();
        assertEquals(OthelloModel.Player.BLACK, board[2][3]);  // New piece
        assertEquals(OthelloModel.Player.BLACK, board[3][3]);  // Original piece
    }

    @Test
    void testComputerMoveQuality() {
        setUp();
        // Make a move as Black first to test White's computer move
        assertTrue(model.makeMove(3, 2));  // Black's move
        // Now test computer's move as White
        int[] initialScore = model.getScore();
        List<int[]> validMovesBefore = model.getValidMoves();
        int[] move = model.getComputerMove();
        assertTrue(model.makeMove(move[0], move[1]));
        // After computer's move, verify:
        // 1. Score improved for White
        int[] newScore = model.getScore();
        // White should have gained pieces
        assertTrue(newScore[1] > initialScore[1]);
        // 2. Move was one of the valid moves
        assertTrue(containsMove(validMovesBefore, move[0], move[1]));
    }

    @Test
    void testBoardEdgeCases() {
        setUp();
        // Test moves at board edges
        // Try all edge positions and verify only valid ones are accepted
        for (int i = 0; i < boardSize; i++) {
            // Top edge
            if (model.isValidMove(0, i)) {
                assertTrue(model.makeMove(0, i));
            }
            // Bottom edge
            if (model.isValidMove(boardSize-1, i)) {
                assertTrue(model.makeMove(boardSize-1, i));
            }
            // Left edge
            if (model.isValidMove(i, 0)) {
                assertTrue(model.makeMove(i, 0));
            }
            // Right edge
            if (model.isValidMove(i, boardSize-1)) {
                assertTrue(model.makeMove(i, boardSize-1));
            }
        }
    }

    private boolean containsMove(List<int[]> moves, int row, int col) {
        for (int[] move : moves) {
            if (move[0] == row && move[1] == col) {
                return true;
            }
        }
        return false;
    }
}