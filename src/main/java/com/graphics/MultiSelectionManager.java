package com.graphics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;

import java.util.*;

/**
 * Manager for multi-object selection and batch operations
 */
public class MultiSelectionManager {

    private final Set<Node> selectedObjects = new HashSet<>();
    private final Map<Node, PhongMaterial> originalMaterials = new HashMap<>();
    private final PhongMaterial selectionMaterial = new PhongMaterial(Color.GOLD);

    private SelectionMode mode = SelectionMode.REPLACE;

    public enum SelectionMode {
        REPLACE, // Click replaces selection
        ADD, // Ctrl+Click adds to selection
        TOGGLE // Ctrl+Click toggles selection
    }

    public MultiSelectionManager() {
        selectionMaterial.setSpecularColor(Color.WHITE);
    }

    /**
     * Select a single object based on current mode
     */
    public void select(Node node, boolean ctrlPressed) {
        if (node == null) {
            return;
        }

        if (ctrlPressed) {
            // Add to selection or toggle
            if (selectedObjects.contains(node)) {
                deselect(node);
            } else {
                addToSelection(node);
            }
        } else {
            // Replace selection
            clearSelection();
            addToSelection(node);
        }
    }

    /**
     * Add object to selection without clearing
     */
    public void addToSelection(Node node) {
        if (node != null && !selectedObjects.contains(node)) {
            selectedObjects.add(node);

            // Store original material and apply selection material
            if (node instanceof Shape3D) {
                Shape3D shape = (Shape3D) node;
                originalMaterials.put(node, (PhongMaterial) shape.getMaterial());
                shape.setMaterial(selectionMaterial);
            }
        }
    }

    /**
     * Remove object from selection
     */
    public void deselect(Node node) {
        if (selectedObjects.remove(node)) {
            // Restore original material
            if (node instanceof Shape3D && originalMaterials.containsKey(node)) {
                Shape3D shape = (Shape3D) node;
                shape.setMaterial(originalMaterials.get(node));
                originalMaterials.remove(node);
            }
        }
    }

    /**
     * Select multiple objects
     */
    public void selectMultiple(Collection<Node> nodes, boolean addToExisting) {
        if (!addToExisting) {
            clearSelection();
        }

        for (Node node : nodes) {
            addToSelection(node);
        }
    }

    /**
     * Clear all selections
     */
    public void clearSelection() {
        // Restore materials for all selected objects
        for (Node node : new ArrayList<>(selectedObjects)) {
            deselect(node);
        }
        selectedObjects.clear();
        originalMaterials.clear();
    }

    /**
     * Get all selected objects
     */
    public Set<Node> getSelectedObjects() {
        return new HashSet<>(selectedObjects);
    }

    /**
     * Get first selected object
     */
    public Node getFirstSelected() {
        return selectedObjects.isEmpty() ? null : selectedObjects.iterator().next();
    }

    /**
     * Check if object is selected
     */
    public boolean isSelected(Node node) {
        return selectedObjects.contains(node);
    }

    /**
     * Get selection count
     */
    public int getSelectionCount() {
        return selectedObjects.size();
    }

    /**
     * Batch transform all selected objects
     */
    public void translateSelection(double dx, double dy, double dz) {
        for (Node node : selectedObjects) {
            node.setTranslateX(node.getTranslateX() + dx);
            node.setTranslateY(node.getTranslateY() + dy);
            node.setTranslateZ(node.getTranslateZ() + dz);
        }
    }

    /**
     * Batch scale all selected objects
     */
    public void scaleSelection(double scaleX, double scaleY, double scaleZ) {
        for (Node node : selectedObjects) {
            node.setScaleX(node.getScaleX() * scaleX);
            node.setScaleY(node.getScaleY() * scaleY);
            node.setScaleZ(node.getScaleZ() * scaleZ);
        }
    }

    /**
     * Batch rotate all selected objects
     */
    public void rotateSelection(double angle, javafx.geometry.Point3D axis) {
        for (Node node : selectedObjects) {
            javafx.scene.transform.Rotate rotation = new javafx.scene.transform.Rotate(angle, axis);
            node.getTransforms().add(rotation);
        }
    }

    /**
     * Align selected objects
     */
    public enum Alignment {
        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK, CENTER_X, CENTER_Y, CENTER_Z
    }

    public void alignSelection(Alignment alignment) {
        if (selectedObjects.size() < 2) {
            return;
        }

        // Calculate alignment reference point
        double refValue = 0;

        switch (alignment) {
            case LEFT:
                refValue = selectedObjects.stream()
                        .mapToDouble(n -> n.getTranslateX() - n.getBoundsInLocal().getWidth() / 2)
                        .min().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateX(refValue + node.getBoundsInLocal().getWidth() / 2);
                }
                break;

            case RIGHT:
                refValue = selectedObjects.stream()
                        .mapToDouble(n -> n.getTranslateX() + n.getBoundsInLocal().getWidth() / 2)
                        .max().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateX(refValue - node.getBoundsInLocal().getWidth() / 2);
                }
                break;

            case TOP:
                refValue = selectedObjects.stream()
                        .mapToDouble(n -> n.getTranslateY() - n.getBoundsInLocal().getHeight() / 2)
                        .min().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateY(refValue + node.getBoundsInLocal().getHeight() / 2);
                }
                break;

            case BOTTOM:
                refValue = selectedObjects.stream()
                        .mapToDouble(n -> n.getTranslateY() + n.getBoundsInLocal().getHeight() / 2)
                        .max().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateY(refValue - node.getBoundsInLocal().getHeight() / 2);
                }
                break;

            case CENTER_X:
                refValue = selectedObjects.stream()
                        .mapToDouble(Node::getTranslateX)
                        .average().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateX(refValue);
                }
                break;

            case CENTER_Y:
                refValue = selectedObjects.stream()
                        .mapToDouble(Node::getTranslateY)
                        .average().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateY(refValue);
                }
                break;

            case CENTER_Z:
                refValue = selectedObjects.stream()
                        .mapToDouble(Node::getTranslateZ)
                        .average().orElse(0);
                for (Node node : selectedObjects) {
                    node.setTranslateZ(refValue);
                }
                break;
        }
    }

    /**
     * Distribute selected objects evenly
     */
    public void distributeSelection(Alignment axis) {
        if (selectedObjects.size() < 3) {
            return;
        }

        List<Node> sorted = new ArrayList<>(selectedObjects);

        switch (axis) {
            case CENTER_X:
                sorted.sort(Comparator.comparingDouble(Node::getTranslateX));
                distributeAlongAxis(sorted, true, false, false);
                break;
            case CENTER_Y:
                sorted.sort(Comparator.comparingDouble(Node::getTranslateY));
                distributeAlongAxis(sorted, false, true, false);
                break;
            case CENTER_Z:
                sorted.sort(Comparator.comparingDouble(Node::getTranslateZ));
                distributeAlongAxis(sorted, false, false, true);
                break;
        }
    }

    private void distributeAlongAxis(List<Node> sorted, boolean x, boolean y, boolean z) {
        if (sorted.size() < 3)
            return;

        double start = x ? sorted.get(0).getTranslateX()
                : y ? sorted.get(0).getTranslateY() : sorted.get(0).getTranslateZ();

        double end = x ? sorted.get(sorted.size() - 1).getTranslateX()
                : y ? sorted.get(sorted.size() - 1).getTranslateY() : sorted.get(sorted.size() - 1).getTranslateZ();

        double spacing = (end - start) / (sorted.size() - 1);

        for (int i = 1; i < sorted.size() - 1; i++) {
            double pos = start + spacing * i;
            if (x)
                sorted.get(i).setTranslateX(pos);
            if (y)
                sorted.get(i).setTranslateY(pos);
            if (z)
                sorted.get(i).setTranslateZ(pos);
        }
    }

    /**
     * Get bounding box encompassing all selected objects
     */
    public double[] getSelectionBounds() {
        if (selectedObjects.isEmpty()) {
            return new double[] { 0, 0, 0, 0, 0, 0 };
        }

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (Node node : selectedObjects) {
            javafx.geometry.Bounds bounds = node.getBoundsInLocal();
            double halfW = bounds.getWidth() * node.getScaleX() / 2;
            double halfH = bounds.getHeight() * node.getScaleY() / 2;
            double halfD = bounds.getDepth() * node.getScaleZ() / 2;

            minX = Math.min(minX, node.getTranslateX() - halfW);
            maxX = Math.max(maxX, node.getTranslateX() + halfW);
            minY = Math.min(minY, node.getTranslateY() - halfH);
            maxY = Math.max(maxY, node.getTranslateY() + halfH);
            minZ = Math.min(minZ, node.getTranslateZ() - halfD);
            maxZ = Math.max(maxZ, node.getTranslateZ() + halfD);
        }

        return new double[] { minX, minY, minZ, maxX, maxY, maxZ };
    }
}
