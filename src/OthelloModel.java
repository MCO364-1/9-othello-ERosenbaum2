import java.util.ArrayList;
import java.util.List;

public class OthelloModel {
    public static int boardSize = 8;
    private static int[][] directions = {
            {-1, -1}, {-1, 0},{-1, 1},
            {0, -1},          {0, 1},
            {1, -1},  {1, 0}, {1, 1}
    };

    private Player[][] board;
    private Player currentPlayer;
    private List<int[]> cachedValidMoves;

    enum Player {
        BLACK, WHITE;

        public Player getOpponent() {
            if (this == BLACK) {
                return WHITE;
            } else {
                return BLACK;
            }
        }
    }

    public OthelloModel() {
        board = new Player[boardSize][boardSize];
        currentPlayer = Player.BLACK;
        initializeBoard();
        updateValidMoves();
    }

    private void initializeBoard() {
        // Set up initial pieces
        int center = boardSize / 2;
        board[center - 1][center - 1] = Player.WHITE;
        board[center - 1][center] = Player.BLACK;
        board[center][center - 1] = Player.BLACK;
        board[center][center] = Player.WHITE;
    }

    public Player[][] getBoard() {
        Player[][] copy = new Player[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, boardSize);
        }
        return copy;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isValidMove(int row, int col) {
        if (!checkBounds(row, col) || board[row][col] != null) {
            return false;
        }

        for (int[] direction : directions) {
            if (!getFlippedPiecesInDirection(row, col, direction[0], direction[1]).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkBounds(int row, int col) {
        return row >= 0 && row < boardSize && col >= 0 && col < boardSize;
    }

    private static class FlipList {
        private final int[][] positions;
        private int size;

        public FlipList() {
            // Maximum possible flips in any direction is boardSize-1
            positions = new int[boardSize - 1][2];
            size = 0;
        }

        public void add(int row, int col) {
            positions[size][0] = row;
            positions[size][1] = col;
            size++;
        }

        public void clear() {
            size = 0;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int[][] getPositions() {
            int[][] result = new int[size][2];
            for (int i = 0; i < size; i++) {
                result[i][0] = positions[i][0];
                result[i][1] = positions[i][1];
            }
            return result;
        }
    }

    private FlipList getFlippedPiecesInDirection(int row, int col, int dr, int dc) {
        FlipList flipped = new FlipList();
        int currRow = row + dr;
        int currCol = col + dc;
        while (checkBounds(currRow, currCol) && board[currRow][currCol] != null) {
            if (board[currRow][currCol] == currentPlayer.getOpponent()) {
                flipped.add(currRow, currCol);
                currRow += dr;
                currCol += dc;
            } else {
                if (board[currRow][currCol] == currentPlayer && !flipped.isEmpty()) {
                    return flipped;
                }
                flipped.clear();
                return flipped;
            }
        }
        flipped.clear();
        return flipped;
    }

    private void updateValidMoves() {
        cachedValidMoves = new ArrayList<>();
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (isValidMove(row, col)) {
                    cachedValidMoves.add(new int[]{row, col});
                }
            }
        }
    }

    public List<int[]> getValidMoves() {
        List<int[]> copy = new ArrayList<>(cachedValidMoves.size());
        for (int[] move : cachedValidMoves) {
            copy.add(new int[]{move[0], move[1]});
        }
        return copy;
    }

    public boolean makeMove(int row, int col) {
        if (!isValidMove(row, col)) {
            return false;
        }
        List<int[]> allFlipped = new ArrayList<>();
        for (int[] direction : directions) {
            FlipList flippedInDirection = getFlippedPiecesInDirection(row, col, direction[0], direction[1]);
            if (!flippedInDirection.isEmpty()) {
                for (int[] pos : flippedInDirection.getPositions()) {
                    allFlipped.add(pos);
                }
            }
        }
        board[row][col] = currentPlayer;
        for (int[] pos : allFlipped) {
            board[pos[0]][pos[1]] = currentPlayer;
        }
        switchPlayer();
        updateValidMoves();
        if (cachedValidMoves.isEmpty()) {
            switchPlayer();
            updateValidMoves();
            if (cachedValidMoves.isEmpty()) {
                return true; // Game is over
            }
        }
        return true;
    }

    private void switchPlayer() {
        currentPlayer = currentPlayer.getOpponent();
    }

    public int[] getScore() {
        int blackCount = 0;
        int whiteCount = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] == Player.BLACK) blackCount++;
                else if (board[i][j] == Player.WHITE) whiteCount++;
            }
        }
        return new int[]{blackCount, whiteCount};
    }

    public boolean isGameOver() {
        return cachedValidMoves.isEmpty() && checkOpponentMoves();
    }

    private boolean checkOpponentMoves() {
        switchPlayer();
        updateValidMoves();
        boolean noMoves = cachedValidMoves.isEmpty();
        switchPlayer();
        updateValidMoves();
        return noMoves;
    }

    public Player getWinner() {
        if (!isGameOver()) {
            return null;
        }
        int[] scores = getScore();
        if (scores[0] > scores[1]) return Player.BLACK;
        if (scores[1] > scores[0]) return Player.WHITE;
        return null; // Tie
    }

    public int[] getComputerMove() {
        if (cachedValidMoves.isEmpty()) {
            return null;
        }
        int[] bestMove = null;
        int maxPiecesGained = -1;
        int[] currentScore = getScore();
        int currentPlayerIndex;
        if (currentPlayer == Player.BLACK) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex = 1;
        }
        // Pure greedy: find the move that gains the most pieces immediately
        for (int[] move : cachedValidMoves) {
            OthelloModel tempModel = new OthelloModel();
            // Copy the current board state
            for (int i = 0; i < boardSize; i++) {
                System.arraycopy(board[i], 0, tempModel.board[i], 0, boardSize);
            }
            tempModel.currentPlayer = currentPlayer;
            tempModel.updateValidMoves();
            // Make the move and calculate immediate piece gain
            tempModel.makeMove(move[0], move[1]);
            int[] newScore = tempModel.getScore();
            int piecesGained = newScore[currentPlayerIndex] - currentScore[currentPlayerIndex];
            // Select move that gains the most pieces
            if (piecesGained > maxPiecesGained) {
                maxPiecesGained = piecesGained;
                bestMove = move;
            }
        }
        // If no move found (shouldn't happen), return the first valid move
        if (bestMove == null) {
            bestMove = cachedValidMoves.get(0);
        }
        return new int[]{bestMove[0], bestMove[1]};
    }
}