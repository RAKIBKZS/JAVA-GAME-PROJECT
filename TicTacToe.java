import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class TicTacToe {
    int boardWidth = 600;
    int boardHeight = 700;

    JFrame frame = new JFrame("Tic-Tac-Toe");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel controlPanel = new JPanel();

    JButton[][] board = new JButton[3][3];
    JButton restartButton = new JButton("Restart");

    String playerX = "X";
    String playerO = "O";
    String currentPlayer = playerX;

    boolean gameOver = false;
    int turns = 0;

    // Scoreboard
    int playerWins = 0, robotWins = 0, ties = 0;
    JLabel scoreLabel = new JLabel();

    boolean vsRobot = false; // mode toggle
    int aiLevel = 0; // 0=Normal, 1=Standard, 2=Hard

    Random rand = new Random();

    TicTacToe() {
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setBackground(Color.darkGray);
        textLabel.setForeground(Color.white);
        textLabel.setFont(new Font("Arial", Font.BOLD, 40));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Tic-Tac-Toe");
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(3, 3));
        boardPanel.setBackground(Color.darkGray);
        frame.add(boardPanel, BorderLayout.CENTER);

        controlPanel.setLayout(new GridLayout(2, 1));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        updateScoreboard();

        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.addActionListener(e -> resetBoard());

        controlPanel.add(scoreLabel);
        controlPanel.add(restartButton);
        frame.add(controlPanel, BorderLayout.SOUTH);

        // Keyboard shortcut: press any key to restart
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameOver) resetBoard();
            }
        });

        // Mode selection
        int mode = JOptionPane.showOptionDialog(frame,
                "Choose Mode:",
                "Game Mode",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Player vs Player", "Player vs Robot"},
                "Player vs Player");
        vsRobot = (mode == 1);

        if (vsRobot) {
            aiLevel = JOptionPane.showOptionDialog(frame,
                    "Choose AI Difficulty:",
                    "AI Level",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Normal", "Standard", "Hard"},
                    "Normal");
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton tile = new JButton();
                board[r][c] = tile;
                boardPanel.add(tile);

                tile.setBackground(Color.darkGray);
                tile.setForeground(Color.white);
                tile.setFont(new Font("Arial", Font.BOLD, 120));
                tile.setFocusable(false);

                tile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (gameOver) return;
                        JButton tile = (JButton) e.getSource();
                        if (tile.getText().equals("")) {
                            tile.setText(currentPlayer);
                            turns++;
                            checkWinner();
                            if (!gameOver) {
                                currentPlayer = currentPlayer.equals(playerX) ? playerO : playerX;
                                textLabel.setText(currentPlayer + "'s turn.");
                                if (vsRobot && currentPlayer.equals(playerO)) {
                                    robotMove();
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    void robotMove() {
        if (aiLevel == 0) {
            randomMove();
        } else if (aiLevel == 1) {
            blockOrRandom();
        } else {
            minimaxMove();
        }
    }

    void randomMove() {
        while (!gameOver) {
            int r = rand.nextInt(3);
            int c = rand.nextInt(3);
            if (board[r][c].getText().equals("")) {
                board[r][c].setText(playerO);
                turns++;
                checkWinner();
                if (!gameOver) {
                    currentPlayer = playerX;
                    textLabel.setText(currentPlayer + "'s turn.");
                }
                break;
            }
        }
    }

    void blockOrRandom() {
        // Try to block playerX winning move
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) {
                    board[r][c].setText(playerX);
                    if (isWinner(playerX)) {
                        board[r][c].setText(playerO);
                        turns++;
                        checkWinner();
                        if (!gameOver) {
                            currentPlayer = playerX;
                            textLabel.setText(currentPlayer + "'s turn.");
                        }
                        return;
                    }
                    board[r][c].setText("");
                }
            }
        }
        randomMove();
    }

    void minimaxMove() {
        int bestScore = Integer.MIN_VALUE;
        int moveR = -1, moveC = -1;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) {
                    board[r][c].setText(playerO);
                    int score = minimax(false);
                    board[r][c].setText("");
                    if (score > bestScore) {
                        bestScore = score;
                        moveR = r;
                        moveC = c;
                    }
                }
            }
        }

        board[moveR][moveC].setText(playerO);
        turns++;
        checkWinner();
        if (!gameOver) {
            currentPlayer = playerX;
            textLabel.setText(currentPlayer + "'s turn.");
        }
    }

    int minimax(boolean isMaximizing) {
        if (isWinner(playerO)) return 1;
        if (isWinner(playerX)) return -1;
        if (turns == 9) return 0;

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals("")) {
                    board[r][c].setText(isMaximizing ? playerO : playerX);
                    turns++;
                    int score = minimax(!isMaximizing);
                    turns--;
                    board[r][c].setText("");
                    if (isMaximizing) bestScore = Math.max(score, bestScore);
                    else bestScore = Math.min(score, bestScore);
                }
            }
        }
        return bestScore;
    }

    boolean isWinner(String player) {
        // horizontal
        for (int r = 0; r < 3; r++) {
            if (board[r][0].getText().equals(player) &&
                board[r][1].getText().equals(player) &&
                board[r][2].getText().equals(player)) return true;
        }
        // vertical
        for (int c = 0; c < 3; c++) {
            if (board[0][c].getText().equals(player) &&
                board[1][c].getText().equals(player) &&
                board[2][c].getText().equals(player)) return true;
        }
        // diagonal
        if (board[0][0].getText().equals(player) &&
            board[1][1].getText().equals(player) &&
            board[2][2].getText().equals(player)) return true;
        // anti-diagonal
        if (board[0][2].getText().equals(player) &&
            board[1][1].getText().equals(player) &&
            board[2][0].getText().equals(player)) return true;

        return false;
    }

    void checkWinner() {
        if (isWinner(currentPlayer)) {
            highlightWinner(currentPlayer);
            gameOver = true;
            updateScores();
            return;
        }
        if (turns == 9) {
            highlightTie();
            gameOver = true;
            ties++;
            updateScoreboard();
        }
    }
        void highlightWinner(String player) {
        textLabel.setText(player + " is the winner!");
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c].getText().equals(player)) {
                    board[r][c].setForeground(Color.green);
                    board[r][c].setBackground(Color.gray);
                }
            }
        }
    }

    void highlightTie() {
        textLabel.setText("Tie!");
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setForeground(Color.orange);
                board[r][c].setBackground(Color.gray);
            }
        }
    }

    void resetBoard() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setText("");
                board[r][c].setForeground(Color.white);
                board[r][c].setBackground(Color.darkGray);
            }
        }
        turns = 0;
        gameOver = false;
        currentPlayer = playerX;
        textLabel.setText("Tic-Tac-Toe");
    }

    void updateScores() {
        if (vsRobot && currentPlayer.equals(playerO)) robotWins++;
        else playerWins++;
        updateScoreboard();
    }

    void updateScoreboard() {
        scoreLabel.setText("Player Wins: " + playerWins +
                           " | Robot Wins: " + robotWins +
                           " | Ties: " + ties);
    }

    public static void main(String[] args) {
        new TicTacToe();
    }
}
