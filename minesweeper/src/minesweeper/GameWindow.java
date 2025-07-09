package minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.border.SoftBevelBorder;

/**
 * Hauptfenster des Spiels Minesweeper
 */
public class GameWindow extends JFrame {

    private JLabel bombDisplay, timerLabel, highscoreLabel;
    private JButton newGame;
    private Timer timer;
    private int secondsPassed;
    private Field[][] gameField;
    private int rows = 15, cols = 12, bombs = 25;
    private HighscoreManager highscoreManager;
    private boolean gameEnded = false;

    public GameWindow() {
        setTitle("Minesweeper");
        setSize(440, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        gameField = new Field[rows][cols];
        newGame = new JButton("New Game");
        highscoreManager = new HighscoreManager();

        // Oberes Panel mit Infos
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));

        // Felder erzeugen
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                gameField[i][j] = new Field(this);
                gridPanel.add(gameField[i][j]);
            }

        // Labels
        bombDisplay = new JLabel();
        bombDisplay.setFont(new Font("Arial", Font.BOLD, 24));
        bombDisplay.setPreferredSize(new Dimension(80, 50));

        timerLabel = new JLabel("Time: 0");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setPreferredSize(new Dimension(120, 50));

        highscoreLabel = new JLabel("Best: -");
        highscoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        highscoreLabel.setPreferredSize(new Dimension(120, 50));

        newGame.setPreferredSize(new Dimension(100, 50));
        newGame.addActionListener(e -> setupNewGame());

        // Zusammenbauen
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(bombDisplay);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(timerLabel);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(highscoreLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(newGame);
        topPanel.add(Box.createHorizontalStrut(10));

        add(topPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);

        setupNewGame();
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // Neues Spiel initialisieren
    public void setupNewGame() {
        gameEnded = false;

        // Felder zurücksetzen
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                Field f = gameField[i][j];
                f.setBomb(false);
                f.resetNumbersTouching();
                f.setTagged(false);
                f.setText("");
                f.setEnabled(true);
                f.setBackground(UIManager.getColor("control"));
                f.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
                for (ActionListener al : f.getActionListeners()) {
                    f.removeActionListener(al);
                }
                f.addActionListener(f);
            }

        // Bomben platzieren
        Random rand = new Random();
        int placedBombs = 0;
        while (placedBombs < bombs) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (!gameField[r][c].isBomb()) {
                gameField[r][c].setBomb(true);
                placedBombs++;
            }
        }

        // Zahlen berechnen
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (!gameField[i][j].isBomb())
                    gameField[i][j].setNumbersTouching(countAdjacentBombs(i, j));

        startTimer();
        updateBombDisplay();
        updateHighscoreDisplay();
    }

    private int countAdjacentBombs(int row, int col) {
        int count = 0;
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int k = 0; k < 8; k++) {
            int r = row + dx[k], c = col + dy[k];
            if (isValid(r, c) && gameField[r][c].isBomb())
                count++;
        }
        return count;
    }

    public void revealEmptyFields(Field field) {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (gameField[i][j] == field) {
                    floodFill(i, j);
                    return;
                }
    }

    private void floodFill(int row, int col) {
        if (!isValid(row, col) || gameField[row][col].isBomb() || !gameField[row][col].isEnabled()) return;

        Field f = gameField[row][col];
        f.setBackground(new Color(192, 192, 192));
        f.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        f.setEnabled(false);
        int num = f.getNumbersTouching();
        if (num > 0) {
            f.setText(String.valueOf(num));
            return;
        }

        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            floodFill(row + dx[i], col + dy[i]);
        }
    }

    public void updateBombDisplay() {
        int flagged = 0;
        for (Field[] row : gameField)
            for (Field f : row)
                if (f.isTagged()) flagged++;
        bombDisplay.setText(String.valueOf(bombs - flagged));
    }

    private void startTimer() {
        if (timer != null && timer.isRunning()) timer.stop();
        secondsPassed = 0;
        timerLabel.setText("Time: 0");

        timer = new Timer(1000, e -> {
            secondsPassed++;
            timerLabel.setText("Time: " + secondsPassed);
        });
        timer.start();
    }

    public void gameOver() {
        if (gameEnded) return;
        gameEnded = true;
        timer.stop();

        for (Field[] row : gameField)
            for (Field f : row) {
                if (f.isBomb()) {
                    f.setText("B");
                    f.setBackground(Color.RED);
                }
                f.setEnabled(false);
            }

        JOptionPane.showMessageDialog(this, "Game Over!", "BOOM!", JOptionPane.ERROR_MESSAGE);
    }

    public void checkWinCondition() {
        if (gameEnded) return;
        int revealed = 0;
        for (Field[] row : gameField)
            for (Field f : row)
                if (!f.isEnabled() && !f.isBomb()) revealed++;

        if (revealed == (rows * cols - bombs)) {
            gameEnded = true;
            timer.stop();
            boolean newHighscore = highscoreManager.checkAndUpdate(secondsPassed);
            updateHighscoreDisplay();

            String msg = "Du hast gewonnen in " + secondsPassed + " Sekunden!";
            if (newHighscore) msg += "\nNEUER HIGHSCORE!";
            JOptionPane.showMessageDialog(this, msg, "Sieg!", JOptionPane.INFORMATION_MESSAGE);
            disableAllFields();
        }
    }

    private void disableAllFields() {
        for (Field[] row : gameField)
            for (Field f : row)
                f.setEnabled(false);
    }

    private void updateHighscoreDisplay() {
        int hs = highscoreManager.getHighscore();
        highscoreLabel.setText(hs == Integer.MAX_VALUE ? "Best: -" : "Best: " + hs);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow().setVisible(true));
    }
}
