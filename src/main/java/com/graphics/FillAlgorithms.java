package com.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.*;

/**
 * Implements polygon fill algorithms
 */
public class FillAlgorithms {

    /**
     * Scan-line fill algorithm for polygons
     */
    public static void scanLineFill(GraphicsContext gc, List<Point2D> vertices, Color color) {
        if (vertices.size() < 3)
            return;

        // Find bounding box
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (Point2D p : vertices) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        gc.setFill(color);

        // For each scan line
        for (int y = minY; y <= maxY; y++) {
            List<Integer> intersections = new ArrayList<>();

            // Find intersections with polygon edges
            for (int i = 0; i < vertices.size(); i++) {
                Point2D p1 = vertices.get(i);
                Point2D p2 = vertices.get((i + 1) % vertices.size());

                if ((p1.y <= y && p2.y > y) || (p2.y <= y && p1.y > y)) {
                    double x = p1.x + (double) (y - p1.y) / (p2.y - p1.y) * (p2.x - p1.x);
                    intersections.add((int) x);
                }
            }

            // Sort intersections
            Collections.sort(intersections);

            // Fill between pairs
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int x1 = intersections.get(i);
                int x2 = intersections.get(i + 1);
                gc.fillRect(x1, y, x2 - x1, 1);
            }
        }
    }

    /**
     * Boundary fill algorithm (4-connected)
     */
    public static void boundaryFill4(WritableImage image, int x, int y, Color fillColor, Color boundaryColor) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        if (x < 0 || x >= width || y < 0 || y >= height)
            return;

        PixelWriter pw = image.getPixelWriter();
        Color currentColor = image.getPixelReader().getColor(x, y);

        if (currentColor.equals(fillColor) || currentColor.equals(boundaryColor)) {
            return;
        }

        Stack<Point2D> stack = new Stack<>();
        stack.push(new Point2D(x, y));

        while (!stack.isEmpty()) {
            Point2D p = stack.pop();

            if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height)
                continue;

            Color c = image.getPixelReader().getColor(p.x, p.y);
            if (c.equals(fillColor) || c.equals(boundaryColor))
                continue;

            pw.setColor(p.x, p.y, fillColor);

            stack.push(new Point2D(p.x + 1, p.y));
            stack.push(new Point2D(p.x - 1, p.y));
            stack.push(new Point2D(p.x, p.y + 1));
            stack.push(new Point2D(p.x, p.y - 1));
        }
    }

    /**
     * Flood fill algorithm
     */
    public static void floodFill(WritableImage image, int x, int y, Color fillColor, Color targetColor) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        if (x < 0 || x >= width || y < 0 || y >= height)
            return;

        PixelWriter pw = image.getPixelWriter();
        Color currentColor = image.getPixelReader().getColor(x, y);

        if (!currentColor.equals(targetColor) || currentColor.equals(fillColor)) {
            return;
        }

        Queue<Point2D> queue = new LinkedList<>();
        queue.add(new Point2D(x, y));

        while (!queue.isEmpty()) {
            Point2D p = queue.poll();

            if (p.x < 0 || p.x >= width || p.y < 0 || p.y >= height)
                continue;

            Color c = image.getPixelReader().getColor(p.x, p.y);
            if (!c.equals(targetColor) || c.equals(fillColor))
                continue;

            pw.setColor(p.x, p.y, fillColor);

            queue.add(new Point2D(p.x + 1, p.y));
            queue.add(new Point2D(p.x - 1, p.y));
            queue.add(new Point2D(p.x, p.y + 1));
            queue.add(new Point2D(p.x, p.y - 1));
        }
    }

    /**
     * Simple 2D point class
     */
    public static class Point2D {
        public int x, y;

        public Point2D(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point2D))
                return false;
            Point2D p = (Point2D) obj;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}
