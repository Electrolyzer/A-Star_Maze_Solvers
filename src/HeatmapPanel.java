package src;

// HeatmapPanel.java
import java.awt.*;
import javax.swing.*;

/**
 * Displays a heatmap visualization of cell exploration frequencies.
 * Shows how often each cell in a maze was explored by an algorithm.
 */
public class HeatmapPanel extends JPanel {
    private int[][] data;
    private int maxValue;

    /**
     * Constructs a HeatmapPanel with the specified exploration data and title.
     * 
     * @param data  2D array of exploration counts for each cell
     * @param title The title to display above the heatmap
     */
    public HeatmapPanel(int[][] data, String title) {
        this.data = data;
        this.maxValue = findMaxValue(data);
        setPreferredSize(new Dimension(300, 320));
        setBorder(BorderFactory.createTitledBorder(title));
    }

    private int findMaxValue(int[][] data) {
        int max = 0;
        for (int[] row : data) {
            for (int value : row) {
                max = Math.max(max, value);
            }
        }
        return max;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (maxValue == 0)
            return;

        int cellSize = Math.min(getWidth() / data.length, (getHeight() - 20) / data.length);
        int offsetX = (getWidth() - data.length * cellSize) / 2;
        int offsetY = (getHeight() - data.length * cellSize) / 2;

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                float intensity = (float) data[i][j] / maxValue;
                Color color = new Color(intensity, 0, 1 - intensity, 0.8f);
                g.setColor(color);
                g.fillRect(offsetX + j * cellSize, offsetY + i * cellSize, cellSize, cellSize);
            }
        }

        // Draw legend
        g.setColor(Color.BLACK);
        g.drawString("Max: " + maxValue, 10, 30);
    }
}