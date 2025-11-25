package com.graphics;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Scale;

import java.util.*;

/**
 * Controller for managing bounding boxes with draggable vertices
 * Allows real-time shape stretching by dragging corner vertices
 */
public class BoundingBoxController {

    private final Group sceneRoot;
    private final Map<Node, BoundingBox> boundingBoxes = new HashMap<>();
    private boolean enabled = true;
    private boolean aspectRatioLocked = false;

    // Visual materials
    private final PhongMaterial edgeMaterial = new PhongMaterial(Color.YELLOW);
    private final PhongMaterial vertexMaterial = new PhongMaterial(Color.ORANGE);
    private final PhongMaterial activeVertexMaterial = new PhongMaterial(Color.RED);

    public BoundingBoxController(Group sceneRoot) {
        this.sceneRoot = sceneRoot;
        edgeMaterial.setSpecularColor(Color.WHITE);
        vertexMaterial.setSpecularColor(Color.WHITE);
        activeVertexMaterial.setSpecularColor(Color.WHITE);
    }

    /**
     * Create and display a bounding box for the given node
     */
    public void createBoundingBox(Node node) {
        if (node == null || boundingBoxes.containsKey(node)) {
            return;
        }

        BoundingBox bbox = new BoundingBox(node);
        boundingBoxes.put(node, bbox);

        if (enabled) {
            bbox.show();
        }
    }

    /**
     * Remove bounding box for a node
     */
    public void removeBoundingBox(Node node) {
        BoundingBox bbox = boundingBoxes.remove(node);
        if (bbox != null) {
            bbox.hide();
        }
    }

    /**
     * Update bounding box for a node (call after transformation)
     */
    public void updateBoundingBox(Node node) {
        BoundingBox bbox = boundingBoxes.get(node);
        if (bbox != null) {
            bbox.update();
        }
    }

    /**
     * Show all bounding boxes
     */
    public void showAll() {
        enabled = true;
        for (BoundingBox bbox : boundingBoxes.values()) {
            bbox.show();
        }
    }

    /**
     * Hide all bounding boxes
     */
    public void hideAll() {
        enabled = false;
        for (BoundingBox bbox : boundingBoxes.values()) {
            bbox.hide();
        }
    }

    /**
     * Clear all bounding boxes
     */
    public void clearAll() {
        for (BoundingBox bbox : boundingBoxes.values()) {
            bbox.hide();
        }
        boundingBoxes.clear();
    }

    public void setAspectRatioLocked(boolean locked) {
        this.aspectRatioLocked = locked;
    }

    public boolean isAspectRatioLocked() {
        return aspectRatioLocked;
    }

    /**
     * Inner class representing a bounding box for a single node
     */
    private class BoundingBox {
        private final Node targetNode;
        private final Group boxGroup = new Group();

        // 8 corner vertices
        private final Sphere[] vertices = new Sphere[8];

        // 12 edges
        private final Cylinder[] edges = new Cylinder[12];

        // Original bounds
        private Bounds originalBounds;
        private double originalScaleX, originalScaleY, originalScaleZ;

        // Dragging state
        private VertexHandle draggedVertex = null;
        private double dragStartX, dragStartY;
        private double dragStartSceneX, dragStartSceneY, dragStartSceneZ;

        // Vertex positions (relative to center)
        private enum VertexHandle {
            // Corners: (x, y, z) where each is -1 or +1
            FrontTopLeft(-1, -1, -1),
            FrontTopRight(1, -1, -1),
            FrontBottomLeft(-1, 1, -1),
            FrontBottomRight(1, 1, -1),
            BackTopLeft(-1, -1, 1),
            BackTopRight(1, -1, 1),
            BackBottomLeft(-1, 1, 1),
            BackBottomRight(1, 1, 1);

            final int x, y, z;

            VertexHandle(int x, int y, int z) {
                this.x = x;
                this.y = y;
                this.z = z;
            }
        }

        public BoundingBox(Node node) {
            this.targetNode = node;
            this.originalBounds = node.getBoundsInLocal();
            this.originalScaleX = node.getScaleX();
            this.originalScaleY = node.getScaleY();
            this.originalScaleZ = node.getScaleZ();

            createVisualization();
        }

        private void createVisualization() {
            // Create 8 corner spheres
            for (int i = 0; i < 8; i++) {
                vertices[i] = new Sphere(0.1);
                vertices[i].setMaterial(vertexMaterial);

                final int index = i;
                final VertexHandle handle = VertexHandle.values()[i];

                // Mouse handlers for dragging
                vertices[i].setOnMousePressed(e -> {
                    draggedVertex = handle;
                    dragStartX = e.getSceneX();
                    dragStartY = e.getSceneY();
                    dragStartSceneX = targetNode.getTranslateX();
                    dragStartSceneY = targetNode.getTranslateY();
                    dragStartSceneZ = targetNode.getTranslateZ();
                    vertices[index].setMaterial(activeVertexMaterial);
                    e.consume();
                });

                vertices[i].setOnMouseDragged(e -> {
                    if (draggedVertex == handle) {
                        handleVertexDrag(e);
                    }
                    e.consume();
                });

                vertices[i].setOnMouseReleased(e -> {
                    if (draggedVertex == handle) {
                        vertices[index].setMaterial(vertexMaterial);
                        draggedVertex = null;
                    }
                    e.consume();
                });

                // Hover effect
                vertices[i].setOnMouseEntered(e -> {
                    if (draggedVertex == null) {
                        vertices[index].setScaleX(1.5);
                        vertices[index].setScaleY(1.5);
                        vertices[index].setScaleZ(1.5);
                    }
                });

                vertices[i].setOnMouseExited(e -> {
                    if (draggedVertex == null) {
                        vertices[index].setScaleX(1.0);
                        vertices[index].setScaleY(1.0);
                        vertices[index].setScaleZ(1.0);
                    }
                });

                boxGroup.getChildren().add(vertices[i]);
            }

            // Create 12 edges
            for (int i = 0; i < 12; i++) {
                edges[i] = new Cylinder(0.02, 1);
                edges[i].setMaterial(edgeMaterial);
                boxGroup.getChildren().add(edges[i]);
            }

            update();
        }

        private void handleVertexDrag(MouseEvent e) {
            double dx = e.getSceneX() - dragStartX;
            double dy = e.getSceneY() - dragStartY;

            // Sensitivity factor
            double sensitivity = 0.01;

            Bounds bounds = targetNode.getBoundsInLocal();
            double currentWidth = bounds.getWidth();
            double currentHeight = bounds.getHeight();
            double currentDepth = bounds.getDepth();

            // Calculate scale change based on which vertex is being dragged
            double scaleChangeX = 1.0;
            double scaleChangeY = 1.0;
            double scaleChangeZ = 1.0;

            // Horizontal drag affects X scale for left/right vertices
            if (draggedVertex.x != 0) {
                scaleChangeX = 1.0 + (draggedVertex.x * dx * sensitivity);
            }

            // Vertical drag affects Y scale for top/bottom vertices
            if (draggedVertex.y != 0) {
                scaleChangeY = 1.0 + (draggedVertex.y * dy * sensitivity);
            }

            // For depth, we can use a combination or specific key modifier
            // For now, combine horizontal drag with depth for back vertices
            if (draggedVertex.z != 0) {
                scaleChangeZ = 1.0 + (draggedVertex.z * dx * sensitivity * 0.5);
            }

            if (aspectRatioLocked) {
                // Use the maximum change for all axes
                double maxChange = Math.max(Math.max(
                        Math.abs(scaleChangeX - 1.0),
                        Math.abs(scaleChangeY - 1.0)),
                        Math.abs(scaleChangeZ - 1.0));
                double sign = (scaleChangeX - 1.0) + (scaleChangeY - 1.0) + (scaleChangeZ - 1.0) > 0 ? 1 : -1;
                scaleChangeX = scaleChangeY = scaleChangeZ = 1.0 + sign * maxChange;
            }

            // Apply scale changes
            double newScaleX = Math.max(0.1, originalScaleX * scaleChangeX);
            double newScaleY = Math.max(0.1, originalScaleY * scaleChangeY);
            double newScaleZ = Math.max(0.1, originalScaleZ * scaleChangeZ);

            targetNode.setScaleX(newScaleX);
            targetNode.setScaleY(newScaleY);
            targetNode.setScaleZ(newScaleZ);

            // Update visualization
            update();
        }

        public void update() {
            Bounds bounds = targetNode.getBoundsInLocal();

            double width = bounds.getWidth() * targetNode.getScaleX();
            double height = bounds.getHeight() * targetNode.getScaleY();
            double depth = bounds.getDepth() * targetNode.getScaleZ();

            double centerX = targetNode.getTranslateX();
            double centerY = targetNode.getTranslateY();
            double centerZ = targetNode.getTranslateZ();

            double halfW = width / 2;
            double halfH = height / 2;
            double halfD = depth / 2;

            // Update vertex positions
            VertexHandle[] handles = VertexHandle.values();
            for (int i = 0; i < 8; i++) {
                vertices[i].setTranslateX(centerX + handles[i].x * halfW);
                vertices[i].setTranslateY(centerY + handles[i].y * halfH);
                vertices[i].setTranslateZ(centerZ + handles[i].z * halfD);
            }

            // Update edges - 12 edges connecting the vertices
            // 4 edges along X axis
            updateEdge(edges[0], vertices[0], vertices[1]); // Front top
            updateEdge(edges[1], vertices[2], vertices[3]); // Front bottom
            updateEdge(edges[2], vertices[4], vertices[5]); // Back top
            updateEdge(edges[3], vertices[6], vertices[7]); // Back bottom

            // 4 edges along Y axis
            updateEdge(edges[4], vertices[0], vertices[2]); // Front left
            updateEdge(edges[5], vertices[1], vertices[3]); // Front right
            updateEdge(edges[6], vertices[4], vertices[6]); // Back left
            updateEdge(edges[7], vertices[5], vertices[7]); // Back right

            // 4 edges along Z axis
            updateEdge(edges[8], vertices[0], vertices[4]); // Top left
            updateEdge(edges[9], vertices[1], vertices[5]); // Top right
            updateEdge(edges[10], vertices[2], vertices[6]); // Bottom left
            updateEdge(edges[11], vertices[3], vertices[7]); // Bottom right
        }

        private void updateEdge(Cylinder edge, Sphere v1, Sphere v2) {
            double x1 = v1.getTranslateX();
            double y1 = v1.getTranslateY();
            double z1 = v1.getTranslateZ();

            double x2 = v2.getTranslateX();
            double y2 = v2.getTranslateY();
            double z2 = v2.getTranslateZ();

            // Calculate length
            double dx = x2 - x1;
            double dy = y2 - y1;
            double dz = z2 - z1;
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Set edge position to midpoint
            edge.setTranslateX((x1 + x2) / 2);
            edge.setTranslateY((y1 + y2) / 2);
            edge.setTranslateZ((z1 + z2) / 2);

            // Set edge length
            edge.setHeight(length);

            // Calculate rotation to align with the two vertices
            // Default cylinder is along Y axis
            if (length > 0.001) {
                // Calculate angles
                double angleXZ = Math.atan2(dx, dz);
                double angleY = Math.acos(dy / length);

                edge.getTransforms().clear();
                edge.getTransforms().addAll(
                        new javafx.scene.transform.Rotate(-Math.toDegrees(angleXZ),
                                javafx.scene.transform.Rotate.Y_AXIS),
                        new javafx.scene.transform.Rotate(Math.toDegrees(angleY),
                                javafx.scene.transform.Rotate.X_AXIS));
            }
        }

        public void show() {
            if (!sceneRoot.getChildren().contains(boxGroup)) {
                sceneRoot.getChildren().add(boxGroup);
            }
        }

        public void hide() {
            sceneRoot.getChildren().remove(boxGroup);
        }
    }
}
