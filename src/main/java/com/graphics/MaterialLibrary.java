package com.graphics;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.util.HashMap;
import java.util.Map;

/**
 * Library of preset materials and textures for 3D objects
 */
public class MaterialLibrary {

    public enum MaterialType {
        // Metals
        GOLD, SILVER, COPPER, BRONZE, IRON,

        // Natural materials
        WOOD_OAK, WOOD_PINE, STONE_MARBLE, STONE_GRANITE, STONE_BRICK,

        // Plastics
        PLASTIC_MATTE, PLASTIC_GLOSSY,

        // Glass
        GLASS_CLEAR, GLASS_FROSTED, GLASS_BLUE, GLASS_GREEN,

        // Special
        RUBBER, EMISSIVE, MIRROR,

        // Basic colors with different properties
        RED_MATTE, BLUE_GLOSSY, GREEN_METALLIC, WHITE_EMISSIVE,

        // Procedural
        CHECKERBOARD, GRID, DOTS
    }

    private static final Map<MaterialType, PhongMaterial> materialCache = new HashMap<>();

    /**
     * Get a material by type
     */
    public static PhongMaterial getMaterial(MaterialType type) {
        if (!materialCache.containsKey(type)) {
            materialCache.put(type, createMaterial(type));
        }
        return materialCache.get(type);
    }

    /**
     * Create a copy of a material (for independent modification)
     */
    public static PhongMaterial getMaterialCopy(MaterialType type) {
        PhongMaterial original = getMaterial(type);
        PhongMaterial copy = new PhongMaterial();

        copy.setDiffuseColor(original.getDiffuseColor());
        copy.setSpecularColor(original.getSpecularColor());
        copy.setSpecularPower(original.getSpecularPower());

        if (original.getDiffuseMap() != null) {
            copy.setDiffuseMap(original.getDiffuseMap());
        }
        if (original.getSpecularMap() != null) {
            copy.setSpecularMap(original.getSpecularMap());
        }
        if (original.getBumpMap() != null) {
            copy.setBumpMap(original.getBumpMap());
        }
        if (original.getSelfIlluminationMap() != null) {
            copy.setSelfIlluminationMap(original.getSelfIlluminationMap());
        }

        return copy;
    }

    /**
     * Create material based on type
     */
    private static PhongMaterial createMaterial(MaterialType type) {
        PhongMaterial material = new PhongMaterial();

        switch (type) {
            // ========== METALS ==========
            case GOLD:
                material.setDiffuseColor(Color.rgb(255, 215, 0));
                material.setSpecularColor(Color.rgb(255, 255, 200));
                material.setSpecularPower(128);
                break;

            case SILVER:
                material.setDiffuseColor(Color.rgb(192, 192, 192));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(256);
                break;

            case COPPER:
                material.setDiffuseColor(Color.rgb(184, 115, 51));
                material.setSpecularColor(Color.rgb(255, 200, 150));
                material.setSpecularPower(96);
                break;

            case BRONZE:
                material.setDiffuseColor(Color.rgb(205, 127, 50));
                material.setSpecularColor(Color.rgb(255, 180, 100));
                material.setSpecularPower(64);
                break;

            case IRON:
                material.setDiffuseColor(Color.rgb(120, 120, 130));
                material.setSpecularColor(Color.rgb(200, 200, 210));
                material.setSpecularPower(32);
                break;

            // ========== WOOD ==========
            case WOOD_OAK:
                material.setDiffuseColor(Color.rgb(139, 90, 43));
                material.setSpecularColor(Color.rgb(100, 70, 40));
                material.setSpecularPower(8);
                break;

            case WOOD_PINE:
                material.setDiffuseColor(Color.rgb(194, 154, 108));
                material.setSpecularColor(Color.rgb(150, 120, 80));
                material.setSpecularPower(6);
                break;

            // ========== STONE ==========
            case STONE_MARBLE:
                material.setDiffuseColor(Color.rgb(240, 240, 235));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(48);
                break;

            case STONE_GRANITE:
                material.setDiffuseColor(Color.rgb(80, 80, 85));
                material.setSpecularColor(Color.rgb(100, 100, 110));
                material.setSpecularPower(24);
                break;

            case STONE_BRICK:
                material.setDiffuseColor(Color.rgb(150, 80, 60));
                material.setSpecularColor(Color.rgb(120, 60, 40));
                material.setSpecularPower(4);
                break;

            // ========== PLASTIC ==========
            case PLASTIC_MATTE:
                material.setDiffuseColor(Color.rgb(200, 200, 200));
                material.setSpecularColor(Color.rgb(150, 150, 150));
                material.setSpecularPower(16);
                break;

            case PLASTIC_GLOSSY:
                material.setDiffuseColor(Color.rgb(220, 220, 220));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(128);
                break;

            // ========== GLASS ==========
            case GLASS_CLEAR:
                material.setDiffuseColor(Color.rgb(240, 240, 255, 0.3));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(256);
                break;

            case GLASS_FROSTED:
                material.setDiffuseColor(Color.rgb(230, 230, 240, 0.6));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(64);
                break;

            case GLASS_BLUE:
                material.setDiffuseColor(Color.rgb(100, 150, 255, 0.4));
                material.setSpecularColor(Color.rgb(200, 220, 255));
                material.setSpecularPower(192);
                break;

            case GLASS_GREEN:
                material.setDiffuseColor(Color.rgb(100, 255, 150, 0.4));
                material.setSpecularColor(Color.rgb(200, 255, 220));
                material.setSpecularPower(192);
                break;

            // ========== SPECIAL ==========
            case RUBBER:
                material.setDiffuseColor(Color.rgb(50, 50, 50));
                material.setSpecularColor(Color.rgb(30, 30, 30));
                material.setSpecularPower(2);
                break;

            case EMISSIVE:
                material.setDiffuseColor(Color.rgb(255, 255, 100));
                material.setSpecularColor(Color.rgb(255, 255, 200));
                material.setSpecularPower(0);
                // Note: JavaFX doesn't have true emissive, but we simulate with bright colors
                break;

            case MIRROR:
                material.setDiffuseColor(Color.rgb(220, 220, 220));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(512);
                break;

            // ========== COLORED MATERIALS ==========
            case RED_MATTE:
                material.setDiffuseColor(Color.rgb(200, 50, 50));
                material.setSpecularColor(Color.rgb(150, 40, 40));
                material.setSpecularPower(16);
                break;

            case BLUE_GLOSSY:
                material.setDiffuseColor(Color.rgb(50, 100, 255));
                material.setSpecularColor(Color.rgb(200, 220, 255));
                material.setSpecularPower(128);
                break;

            case GREEN_METALLIC:
                material.setDiffuseColor(Color.rgb(50, 180, 80));
                material.setSpecularColor(Color.rgb(150, 255, 180));
                material.setSpecularPower(96);
                break;

            case WHITE_EMISSIVE:
                material.setDiffuseColor(Color.rgb(255, 255, 255));
                material.setSpecularColor(Color.rgb(255, 255, 255));
                material.setSpecularPower(0);
                break;

            // ========== PROCEDURAL PATTERNS ==========
            case CHECKERBOARD:
                material.setDiffuseColor(Color.WHITE);
                material.setDiffuseMap(createCheckerboardTexture(8));
                material.setSpecularPower(32);
                break;

            case GRID:
                material.setDiffuseColor(Color.LIGHTGRAY);
                material.setDiffuseMap(createGridTexture(16));
                material.setSpecularPower(16);
                break;

            case DOTS:
                material.setDiffuseColor(Color.WHITE);
                material.setDiffuseMap(createDotsTexture(8));
                material.setSpecularPower(24);
                break;

            default:
                material.setDiffuseColor(Color.GRAY);
                material.setSpecularColor(Color.WHITE);
                material.setSpecularPower(32);
        }

        return material;
    }

    /**
     * Create a custom material with specified properties
     */
    public static PhongMaterial createCustomMaterial(Color diffuse, Color specular, double specularPower) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(diffuse);
        material.setSpecularColor(specular);
        material.setSpecularPower(specularPower);
        return material;
    }

    /**
     * Create checkerboard texture
     */
    private static Image createCheckerboardTexture(int gridSize) {
        int size = 256;
        int[] pixels = new int[size * size];
        int cellSize = size / gridSize;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int cellX = x / cellSize;
                int cellY = y / cellSize;
                boolean isWhite = (cellX + cellY) % 2 == 0;
                pixels[y * size + x] = isWhite ? 0xFFFFFFFF : 0xFF333333;
            }
        }

        return createImageFromPixels(pixels, size, size);
    }

    /**
     * Create grid texture
     */
    private static Image createGridTexture(int gridSize) {
        int size = 256;
        int[] pixels = new int[size * size];
        int cellSize = size / gridSize;
        int lineWidth = 2;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean isLine = (x % cellSize < lineWidth) || (y % cellSize < lineWidth);
                pixels[y * size + x] = isLine ? 0xFF000000 : 0xFFCCCCCC;
            }
        }

        return createImageFromPixels(pixels, size, size);
    }

    /**
     * Create dots texture
     */
    private static Image createDotsTexture(int gridSize) {
        int size = 256;
        int[] pixels = new int[size * size];
        int cellSize = size / gridSize;
        int dotRadius = cellSize / 4;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int cellX = x / cellSize;
                int cellY = y / cellSize;
                int centerX = cellX * cellSize + cellSize / 2;
                int centerY = cellY * cellSize + cellSize / 2;

                int dx = x - centerX;
                int dy = y - centerY;
                boolean isDot = (dx * dx + dy * dy) <= (dotRadius * dotRadius);

                pixels[y * size + x] = isDot ? 0xFF000000 : 0xFFFFFFFF;
            }
        }

        return createImageFromPixels(pixels, size, size);
    }

    /**
     * Create image from pixel array
     */
    private static Image createImageFromPixels(int[] pixels, int width, int height) {
        javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(width, height);
        javafx.scene.image.PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[y * width + x];
                writer.setArgb(x, y, argb);
            }
        }

        return image;
    }

    /**
     * Modify material properties
     */
    public static void setMaterialShininess(PhongMaterial material, double shininess) {
        material.setSpecularPower(shininess);
    }

    public static void setMaterialReflectivity(PhongMaterial material, double reflectivity) {
        Color specular = material.getSpecularColor();
        double r = specular.getRed() * reflectivity;
        double g = specular.getGreen() * reflectivity;
        double b = specular.getBlue() * reflectivity;
        material.setSpecularColor(Color.color(r, g, b));
    }
}
