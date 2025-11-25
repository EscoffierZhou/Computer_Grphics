package com.graphics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

/**
 * Simple scene exporter that saves object data to a custom format
 */
public class SceneExporter {

    public static void exportScene(Map<String, Node> objectMap, String filename) {
        try {
            // Create output directory if it doesn't exist
            Path outputDir = Paths.get("output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            Path filePath = outputDir.resolve(filename);
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write("# 3D Graphics Scene Export\n");
                writer.write("# Format: ObjectName,Type,X,Y,Z,ColorR,ColorG,ColorB\n\n");

                for (Map.Entry<String, Node> entry : objectMap.entrySet()) {
                    Node node = entry.getValue();
                    String name = entry.getKey();
                    String type = node.getClass().getSimpleName();

                    double x = node.getTranslateX();
                    double y = node.getTranslateY();
                    double z = node.getTranslateZ();

                    // Get color if it's a Shape3D
                    double r = 0.5, g = 0.5, b = 0.5;
                    if (node instanceof Shape3D) {
                        Shape3D shape = (Shape3D) node;
                        if (shape.getMaterial() instanceof PhongMaterial) {
                            PhongMaterial mat = (PhongMaterial) shape.getMaterial();
                            Color color = (Color) mat.getDiffuseColor();
                            r = color.getRed();
                            g = color.getGreen();
                            b = color.getBlue();
                        }
                    }

                    writer.write(String.format("%s,%s,%.2f,%.2f,%.2f,%.3f,%.3f,%.3f\n",
                            name, type, x, y, z, r, g, b));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export scene: " + e.getMessage(), e);
        }
    }

    public static class SceneData {
        public String name;
        public String type;
        public double x, y, z;
        public double r, g, b;

        public SceneData(String line) {
            String[] parts = line.split(",");
            this.name = parts[0];
            this.type = parts[1];
            this.x = Double.parseDouble(parts[2]);
            this.y = Double.parseDouble(parts[3]);
            this.z = Double.parseDouble(parts[4]);
            this.r = Double.parseDouble(parts[5]);
            this.g = Double.parseDouble(parts[6]);
            this.b = Double.parseDouble(parts[7]);
        }
    }

    public static java.util.List<SceneData> importScene(String filename) {
        java.util.List<SceneData> objects = new java.util.ArrayList<>();

        try {
            Path filePath = Paths.get("output", filename);
            java.util.List<String> lines = Files.readAllLines(filePath);

            for (String line : lines) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                objects.add(new SceneData(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import scene: " + e.getMessage(), e);
        }

        return objects;
    }
}
