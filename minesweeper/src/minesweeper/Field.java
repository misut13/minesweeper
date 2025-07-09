package minesweeper;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Repräsentiert ein einzelnes Feld im Minesweeper-Spiel.
 */
class Field extends JButton implements ActionListener {
    private boolean isBomb;              // Gibt an, ob dieses Feld eine Bombe enthält
    private boolean isTagged;            // Gibt an, ob das Feld als Bombe markiert wurde
    private int numbersTouching;         // Anzahl angrenzender Bomben
    private Color defaultBackground;     // Standardhintergrund
    private Color pressedBackground;     // Hintergrund bei aufgedecktem Feld
    private GameWindow gameWindow;       // Referenz auf das Hauptfenster

    public Field(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.isBomb = false;
        this.isTagged = false;
        this.numbersTouching = 0;

        // Erscheinungsbild des Buttons
        this.setFont(new Font("Dialog", Font.BOLD, 20));
        this.setMargin(new Insets(0, 0, 0, 0));
        defaultBackground = UIManager.getColor("control");
        pressedBackground = new Color(192, 192, 192);

        this.setBackground(defaultBackground);
        this.setForeground(Color.BLACK);
        this.setOpaque(true);
        this.setContentAreaFilled(true);
        this.setFocusPainted(false);
        this.setBorderPainted(true);
        this.setUI(new BasicButtonUI());
        this.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        this.setPreferredSize(new Dimension(40, 40));
        this.addActionListener(this);

        // Rechter Mausklick zum Markieren/Entfernen der Flagge
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && isEnabled()) {
                    toggleFlag();
                }
            }
        });
    }

    // Getter und Setter
    public boolean isBomb() {
        return isBomb;
    }

    public void setBomb(boolean isBomb) {
        this.isBomb = isBomb;
    }

    public boolean isTagged() {
        return isTagged;
    }

    public void setTagged(boolean isTagged) {
        this.isTagged = isTagged;
    }

    public int getNumbersTouching() {
        return numbersTouching;
    }

    public void incNumbersTouching() {
        this.numbersTouching += 1;
    }

    public void setNumbersTouching(int count) {
        this.numbersTouching = count;
    }

    public void resetNumbersTouching() {
        this.numbersTouching = 0;
    }

    public Color getPressedBackground() {
        return pressedBackground;
    }

    // Markieren oder Entmarkieren eines Feldes als Bombe
    private void toggleFlag() {
        if (!this.getText().equals("F")) {
            this.setText("F");
            this.setForeground(Color.RED);
            this.setTagged(true);
        } else {
            this.setText("");
            this.setTagged(false);
        }
        gameWindow.updateBombDisplay();
    }

    // Wird aufgerufen, wenn das Feld per Linksklick angeklickt wird
    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.isTagged) return;

        if (this.isBomb) {
            this.setText("B");
            this.setBackground(Color.RED);
            gameWindow.gameOver();
        } else {
            if (this.numbersTouching == 0) {
                gameWindow.revealEmptyFields(this);
            } else {
                this.setText(String.valueOf(this.numbersTouching));
            }
            this.setBackground(pressedBackground);
            this.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        }

        this.setEnabled(false);
        this.removeActionListener(this);
        gameWindow.checkWinCondition();
    }
}
