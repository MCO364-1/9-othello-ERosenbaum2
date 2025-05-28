import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class OthelloGUI extends JFrame {
    private static int cellSize = 60;
    private OthelloModel model;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private boolean isComputerTurn;

    public OthelloGUI() {
        model = new OthelloModel();
        isComputerTurn = false;
        setTitle("Othello");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        boardPanel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                drawBoard(g);
            }
        };
        boardPanel.setSize(494, 480);
        boardPanel.setBackground(new Color(0, 100, 0));
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isComputerTurn) {
                    int col = e.getX() / cellSize;
                    int row = e.getY() / cellSize;
                    List<int[]> validMoves = model.getValidMoves();
                    if (containsMove(validMoves, row, col)) {
                        makeMove(row, col);
                        if (!model.isGameOver()) {
                            isComputerTurn = true;
                            updateStatus();
                            boardPanel.repaint();
                            int[] computerMove = model.getComputerMove();
                            if (computerMove != null && computerMove.length == 2) {
                                makeMove(computerMove[0], computerMove[1]);
                            }
                            isComputerTurn = false;
                            updateStatus();
                            boardPanel.repaint();
                        }
                    }
                }
            }
        });

        statusLabel = new JLabel("Black's turn");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        add(boardPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        setSize(494, 536);
        setLocationRelativeTo(null);
        updateStatus();
        setVisible(true);
    }

    private void drawBoard(Graphics g) {
        OthelloModel.Player[][] board = model.getBoard();
        List<int[]> validMoves;
        if (!isComputerTurn) {
            validMoves = model.getValidMoves();
        } else {
            validMoves = null;
        }
        g.setColor(Color.BLACK);
        for (int i = 0; i <= OthelloModel.boardSize; i++) {
            g.drawLine(i * cellSize, 0, i * cellSize, OthelloModel.boardSize * cellSize);
            g.drawLine(0, i * cellSize, OthelloModel.boardSize * cellSize, i * cellSize);
        }
        for (int row = 0; row < OthelloModel.boardSize; row++) {
            for (int col = 0; col < OthelloModel.boardSize; col++) {
                if (validMoves != null && containsMove(validMoves, row, col)) {
                    g.setColor(new Color(0, 150, 0));
                    g.fillRect(col * cellSize + 1, row * cellSize + 1, cellSize - 1, cellSize - 1);
                }
                if (board[row][col] != null) {
                    Color pieceColor;
                    if (board[row][col] == OthelloModel.Player.BLACK) {
                        pieceColor = Color.BLACK;
                    } else {
                        pieceColor = Color.WHITE;
                    }
                    g.setColor(pieceColor);
                    g.fillOval(col * cellSize + 5, row * cellSize + 5, cellSize - 10, cellSize - 10);
                }
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

    private void makeMove(int row, int col) {
        model.makeMove(row, col);
        updateStatus();
        boardPanel.repaint();
    }

    private void updateStatus() {
        if (model.isGameOver()) {
            OthelloModel.Player winner = model.getWinner();
            if (winner != null) {
                statusLabel.setText(winner + " wins!");
            } else {
                statusLabel.setText("Game Over - It's a tie!");
            }
        } else {
            String playerTurn;
            if (model.getCurrentPlayer() == OthelloModel.Player.BLACK) {
                playerTurn = "Black";
            } else {
                playerTurn = "White";
            }
            if (isComputerTurn) {
                statusLabel.setText("Computer (" + playerTurn + ") is thinking...");
            } else {
                statusLabel.setText(playerTurn + "'s turn");
            }
        }
    }

    public static void main(String[] args) {
        new OthelloGUI();
    }
} 