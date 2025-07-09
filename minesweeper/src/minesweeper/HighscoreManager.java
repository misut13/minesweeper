package minesweeper;

import java.io.*;
import java.util.Properties;

/**
 * Verwaltet den Highscore mithilfe einer Properties-Datei.
 */
public class HighscoreManager {
    private final String filePath;
    private final String KEY_HIGHSCORE = "highscore";
    private int bestTime = Integer.MAX_VALUE;

    public HighscoreManager() {
        filePath = System.getProperty("user.home") + File.separator + ".minesweeper.properties";
        loadHighscore();
    }

    public int getHighscore() {
        return bestTime;
    }

    public boolean checkAndUpdate(int timeInSeconds) {
        if (timeInSeconds < bestTime) {
            bestTime = timeInSeconds;
            saveHighscore();
            return true;
        }
        return false;
    }

    private void saveHighscore() {
        Properties props = new Properties();
        props.setProperty(KEY_HIGHSCORE, String.valueOf(bestTime));
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "Minesweeper Highscore");
        } catch (IOException e) {
            showError("Fehler beim Speichern des Highscores: " + e.getMessage());
        }
    }

    private void loadHighscore() {
        File file = new File(filePath);
        if (!file.exists()) {
            bestTime = Integer.MAX_VALUE;
            return;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            String value = props.getProperty(KEY_HIGHSCORE);
            if (value != null) bestTime = Integer.parseInt(value);
        } catch (IOException | NumberFormatException e) {
            showError("Fehler beim Laden des Highscores: " + e.getMessage());
            bestTime = Integer.MAX_VALUE;
        }
    }

    private void showError(String message) {
        System.err.println(message);
        // Optional: JOptionPane.showMessageDialog(null, message, "Fehler", JOptionPane.ERROR_MESSAGE);
    }
}
