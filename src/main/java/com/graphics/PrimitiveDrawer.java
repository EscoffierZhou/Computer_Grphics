package com.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements 2D primitive drawing algorithms
 */
public class PrimitiveDrawer {

    /**
     * Draw a line using Bresenham's algorithm
     */
    public static void drawLine(GraphicsContext gc, int x0, int y0, int x1, int y1, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(2);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            gc.fillOval(x0 - 1, y0 - 1, 3, 3);

            if (x0 == x1 && y0 == y1)
                break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /**
     * Draw a point
     */
    public static void drawPoint(GraphicsContext gc, int x, int y, Color color, int size) {
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size);
    }

    /**
     * Draw a circle using Midpoint Circle algorithm
     */
    public static void drawCircle(GraphicsContext gc, int xc, int yc, int r, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(2);

        int x = 0;
        int y = r;
        int d = 1 - r;

        drawCirclePoints(gc, xc, yc, x, y);

        while (x < y) {
            x++;
            if (d < 0) {
                d += 2 * x + 1;
            } else {
                y--;
                d += 2 * (x - y) + 1;
            }
            drawCirclePoints(gc, xc, yc, x, y);
        }
    }

    private static void drawCirclePoints(GraphicsContext gc, int xc, int yc, int x, int y) {
        drawPoint(gc, xc + x, yc + y, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc - x, yc + y, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc + x, yc - y, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc - x, yc - y, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc + y, yc + x, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc - y, yc + x, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc + y, yc - x, (Color) gc.getStroke(), 2);
        drawPoint(gc, xc - y, yc - x, (Color) gc.getStroke(), 2);
    }

    /**
     * Draw a filled circle
     */
    public static void drawFilledCircle(GraphicsContext gc, int xc, int yc, int r, Color color) {
        gc.setFill(color);
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                if (x * x + y * y <= r * r) {
                    gc.fillOval(xc + x - 1, yc + y - 1, 2, 2);
                }
            }
        }
    }

    /**
     * Draw a triangle (wireframe)
     */
    public static void drawTriangle(GraphicsContext gc, int x0, int y0, int x1, int y1, int x2, int y2, Color color) {
        drawLine(gc, x0, y0, x1, y1, color);
        drawLine(gc, x1, y1, x2, y2, color);
        drawLine(gc, x2, y2, x0, y0, color);
    }

    /**
     * Draw a filled triangle
     */
    public static void drawFilledTriangle(GraphicsContext gc, int x0, int y0, int x1, int y1, int x2, int y2,
            Color color) {
        gc.setFill(color);
        gc.fillPolygon(new double[] { x0, x1, x2 }, new double[] { y0, y1, y2 }, 3);
    }

    /**
     * Draw a rectangle
     */
    public static void drawRectangle(GraphicsContext gc, int x, int y, int width, int height, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
    }

    /**
     * Draw a filled rectangle
     */
    public static void drawFilledRectangle(GraphicsContext gc, int x, int y, int width, int height, Color color) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
    }
}
