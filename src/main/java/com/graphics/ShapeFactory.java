package com.graphics;

import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

/**
 * Factory class for creating various 3D geometric shapes
 * Supports basic primitives and advanced polyhedra
 */
public class ShapeFactory {

    public enum ShapeType {
        CUBE, SPHERE, CYLINDER, CONE, PYRAMID,
        TORUS, CAPSULE, TETRAHEDRON, OCTAHEDRON,
        DODECAHEDRON, ICOSAHEDRON, PRISM, TUBE, RING
    }

    /**
     * Create a shape of the specified type with default parameters
     */
    public static Node createShape(ShapeType type, PhongMaterial material) {
        switch (type) {
            case CUBE:
                return createCube(1.0, material);
            case SPHERE:
                return createSphere(0.7, material);
            case CYLINDER:
                return createCylinder(0.5, 1.5, material);
            case CONE:
                return createCone(0.7, 1.5, material);
            case PYRAMID:
                return createPyramid(1.0, 1.5, material);
            case TORUS:
                return createTorus(0.6, 0.2, 32, 16, material);
            case CAPSULE:
                return createCapsule(0.4, 1.0, material);
            case TETRAHEDRON:
                return createTetrahedron(1.0, material);
            case OCTAHEDRON:
                return createOctahedron(0.8, material);
            case DODECAHEDRON:
                return createDodecahedron(0.7, material);
            case ICOSAHEDRON:
                return createIcosahedron(0.8, material);
            case PRISM:
                return createPrism(6, 0.6, 1.2, material);
            case TUBE:
                return createTube(0.5, 0.3, 1.5, material);
            case RING:
                return createRing(0.8, 0.1, material);
            default:
                return createCube(1.0, material);
        }
    }

    // ========== Basic Shapes ==========

    public static Box createCube(double size, PhongMaterial material) {
        Box box = new Box(size, size, size);
        box.setMaterial(material);
        return box;
    }

    public static Sphere createSphere(double radius, PhongMaterial material) {
        Sphere sphere = new Sphere(radius);
        sphere.setMaterial(material);
        return sphere;
    }

    public static Cylinder createCylinder(double radius, double height, PhongMaterial material) {
        Cylinder cylinder = new Cylinder(radius, height);
        cylinder.setMaterial(material);
        return cylinder;
    }

    public static MeshView createCone(double radius, double height, PhongMaterial material) {
        return createCustomCone(radius, height, 32, material);
    }

    // ========== Advanced Shapes ==========

    /**
     * Create a torus (donut shape)
     * 
     * @param majorRadius   Distance from center of torus to center of tube
     * @param minorRadius   Radius of the tube
     * @param majorSegments Number of segments around major circle
     * @param minorSegments Number of segments around minor circle
     */
    public static MeshView createTorus(double majorRadius, double minorRadius,
            int majorSegments, int minorSegments,
            PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        // Generate vertices
        for (int i = 0; i <= majorSegments; i++) {
            double theta = 2.0 * Math.PI * i / majorSegments;
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);

            for (int j = 0; j <= minorSegments; j++) {
                double phi = 2.0 * Math.PI * j / minorSegments;
                double cosPhi = Math.cos(phi);
                double sinPhi = Math.sin(phi);

                float x = (float) ((majorRadius + minorRadius * cosPhi) * cosTheta);
                float y = (float) (minorRadius * sinPhi);
                float z = (float) ((majorRadius + minorRadius * cosPhi) * sinTheta);

                mesh.getPoints().addAll(x, y, z);

                // Texture coordinates
                mesh.getTexCoords().addAll((float) i / majorSegments, (float) j / minorSegments);
            }
        }

        // Generate faces
        for (int i = 0; i < majorSegments; i++) {
            for (int j = 0; j < minorSegments; j++) {
                int p0 = i * (minorSegments + 1) + j;
                int p1 = p0 + 1;
                int p2 = (i + 1) * (minorSegments + 1) + j;
                int p3 = p2 + 1;

                // Two triangles per quad
                mesh.getFaces().addAll(p0, p0, p2, p2, p1, p1);
                mesh.getFaces().addAll(p1, p1, p2, p2, p3, p3);
            }
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a custom cone using mesh
     */
    private static MeshView createCustomCone(double radius, double height, int segments, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        // Apex vertex
        mesh.getPoints().addAll(0, (float) -height / 2, 0);
        mesh.getTexCoords().addAll(0.5f, 0.5f);

        // Base vertices
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            mesh.getPoints().addAll(x, (float) height / 2, z);
            mesh.getTexCoords().addAll((float) i / segments, 1);
        }

        // Side faces
        for (int i = 0; i < segments; i++) {
            mesh.getFaces().addAll(
                    0, 0,
                    i + 1, i + 1,
                    i + 2, i + 2);
        }

        // Base center
        int centerIdx = segments + 2;
        mesh.getPoints().addAll(0, (float) height / 2, 0);
        mesh.getTexCoords().addAll(0.5f, 0.5f);

        // Base faces
        for (int i = 0; i < segments; i++) {
            mesh.getFaces().addAll(
                    centerIdx, centerIdx,
                    i + 2, i + 2,
                    i + 1, i + 1);
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a square pyramid
     */
    public static MeshView createPyramid(double baseSize, double height, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        float h = (float) baseSize / 2;
        float y0 = (float) -height / 2;
        float y1 = (float) height / 2;

        // Vertices: apex + 4 base corners
        mesh.getPoints().addAll(
                0, y0, 0, // 0: apex
                -h, y1, -h, // 1: base corner 1
                h, y1, -h, // 2: base corner 2
                h, y1, h, // 3: base corner 3
                -h, y1, h // 4: base corner 4
        );

        // Texture coordinates
        mesh.getTexCoords().addAll(
                0.5f, 0, // 0
                0, 1, // 1
                0.5f, 1, // 2
                1, 1 // 3
        );

        // Faces (4 triangular sides + 2 triangular base)
        mesh.getFaces().addAll(
                // Side faces
                0, 0, 2, 2, 1, 1,
                0, 0, 3, 2, 2, 1,
                0, 0, 4, 2, 3, 1,
                0, 0, 1, 2, 4, 1,
                // Base (2 triangles)
                1, 1, 2, 2, 3, 3,
                1, 1, 3, 3, 4, 2);

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a tetrahedron (4 faces)
     */
    public static MeshView createTetrahedron(double size, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        float a = (float) (size / Math.sqrt(2));

        // 4 vertices of tetrahedron
        mesh.getPoints().addAll(
                a, a, a,
                -a, -a, a,
                -a, a, -a,
                a, -a, -a);

        // Texture coordinates
        mesh.getTexCoords().addAll(
                0, 0,
                0.5f, 1,
                1, 0);

        // 4 triangular faces
        mesh.getFaces().addAll(
                0, 0, 1, 1, 2, 2,
                0, 0, 2, 1, 3, 2,
                0, 0, 3, 1, 1, 2,
                1, 0, 3, 1, 2, 2);

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create an octahedron (8 faces)
     */
    public static MeshView createOctahedron(double size, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        float s = (float) size;

        // 6 vertices
        mesh.getPoints().addAll(
                s, 0, 0, // 0: right
                -s, 0, 0, // 1: left
                0, s, 0, // 2: top
                0, -s, 0, // 3: bottom
                0, 0, s, // 4: front
                0, 0, -s // 5: back
        );

        // Texture coordinates
        mesh.getTexCoords().addAll(
                0, 0,
                0.5f, 1,
                1, 0);

        // 8 triangular faces
        mesh.getFaces().addAll(
                // Top half
                2, 0, 0, 1, 4, 2,
                2, 0, 4, 1, 1, 2,
                2, 0, 1, 1, 5, 2,
                2, 0, 5, 1, 0, 2,
                // Bottom half
                3, 0, 4, 1, 0, 2,
                3, 0, 1, 1, 4, 2,
                3, 0, 5, 1, 1, 2,
                3, 0, 0, 1, 5, 2);

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a dodecahedron (12 pentagonal faces)
     */
    public static MeshView createDodecahedron(double size, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        float phi = (float) ((1.0 + Math.sqrt(5.0)) / 2.0); // Golden ratio
        float a = (float) size;
        float b = (float) (size / phi);
        float c = (float) (size * phi);

        // 20 vertices
        mesh.getPoints().addAll(
                -a, -a, -a, -a, -a, a, -a, a, -a, -a, a, a,
                a, -a, -a, a, -a, a, a, a, -a, a, a, a,
                0, -b, -c, 0, -b, c, 0, b, -c, 0, b, c,
                -b, -c, 0, -b, c, 0, b, -c, 0, b, c, 0,
                -c, 0, -b, -c, 0, b, c, 0, -b, c, 0, b);

        // Texture coordinates
        for (int i = 0; i < 20; i++) {
            mesh.getTexCoords().addAll(0.5f, 0.5f);
        }

        // Pentagon faces - represented as 3 triangles each
        int[][] faces = {
                { 0, 8, 4, 18, 16 }, { 0, 12, 14, 4, 8 }, { 0, 16, 2, 13, 12 },
                { 1, 9, 5, 19, 17 }, { 1, 14, 12, 13, 17 }, { 1, 17, 3, 11, 9 },
                { 2, 10, 6, 15, 13 }, { 2, 16, 18, 6, 10 }, { 3, 13, 15, 7, 11 },
                { 4, 14, 5, 19, 18 }, { 5, 14, 1, 9, 19 }, { 6, 18, 19, 7, 15 },
                { 7, 19, 9, 11, 15 }, { 8, 18, 6, 10, 4 }, { 10, 6, 15, 13, 2 },
                { 11, 3, 13, 2, 10 }, { 12, 13, 3, 17, 1 }, { 14, 4, 18, 5, 1 },
                { 15, 6, 18, 19, 7 }, { 16, 0, 12, 13, 2 }
        };

        // Convert pentagons to triangles
        for (int[] pentagon : faces) {
            // Triangle 1
            mesh.getFaces().addAll(
                    pentagon[0], pentagon[0],
                    pentagon[1], pentagon[1],
                    pentagon[2], pentagon[2]);
            // Triangle 2
            mesh.getFaces().addAll(
                    pentagon[0], pentagon[0],
                    pentagon[2], pentagon[2],
                    pentagon[3], pentagon[3]);
            // Triangle 3
            mesh.getFaces().addAll(
                    pentagon[0], pentagon[0],
                    pentagon[3], pentagon[3],
                    pentagon[4], pentagon[4]);
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create an icosahedron (20 triangular faces)
     */
    public static MeshView createIcosahedron(double size, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        float phi = (float) ((1.0 + Math.sqrt(5.0)) / 2.0);
        float s = (float) size;

        // 12 vertices
        mesh.getPoints().addAll(
                -s, phi * s, 0,
                s, phi * s, 0,
                -s, -phi * s, 0,
                s, -phi * s, 0,
                0, -s, phi * s,
                0, s, phi * s,
                0, -s, -phi * s,
                0, s, -phi * s,
                phi * s, 0, -s,
                phi * s, 0, s,
                -phi * s, 0, -s,
                -phi * s, 0, s);

        // Texture coordinates
        for (int i = 0; i < 12; i++) {
            mesh.getTexCoords().addAll(0.5f, 0.5f);
        }

        // 20 triangular faces
        int[][] faces = {
                { 0, 11, 5 }, { 0, 5, 1 }, { 0, 1, 7 }, { 0, 7, 10 }, { 0, 10, 11 },
                { 1, 5, 9 }, { 5, 11, 4 }, { 11, 10, 2 }, { 10, 7, 6 }, { 7, 1, 8 },
                { 3, 9, 4 }, { 3, 4, 2 }, { 3, 2, 6 }, { 3, 6, 8 }, { 3, 8, 9 },
                { 4, 9, 5 }, { 2, 4, 11 }, { 6, 2, 10 }, { 8, 6, 7 }, { 9, 8, 1 }
        };

        for (int[] face : faces) {
            mesh.getFaces().addAll(
                    face[0], face[0],
                    face[1], face[1],
                    face[2], face[2]);
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a prism with customizable number of sides
     */
    public static MeshView createPrism(int sides, double radius, double height, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();

        float h = (float) height / 2;

        // Top and bottom vertices
        for (int i = 0; i <= sides; i++) {
            double angle = 2.0 * Math.PI * i / sides;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));

            // Top vertex
            mesh.getPoints().addAll(x, -h, z);
            // Bottom vertex
            mesh.getPoints().addAll(x, h, z);
        }

        // Texture coordinates
        for (int i = 0; i <= sides * 2; i++) {
            mesh.getTexCoords().addAll(0.5f, 0.5f);
        }

        // Side faces
        for (int i = 0; i < sides; i++) {
            int t0 = i * 2;
            int t1 = t0 + 1;
            int t2 = (i + 1) * 2;
            int t3 = t2 + 1;

            // Two triangles per side
            mesh.getFaces().addAll(t0, t0, t2, t2, t1, t1);
            mesh.getFaces().addAll(t1, t1, t2, t2, t3, t3);
        }

        // Top and bottom caps need center points
        int topCenter = sides * 2 + 1;
        int bottomCenter = topCenter + 1;
        mesh.getPoints().addAll(0, -h, 0);
        mesh.getPoints().addAll(0, h, 0);
        mesh.getTexCoords().addAll(0.5f, 0.5f);
        mesh.getTexCoords().addAll(0.5f, 0.5f);

        // Top cap
        for (int i = 0; i < sides; i++) {
            int t0 = i * 2;
            int t2 = ((i + 1) % sides) * 2;
            mesh.getFaces().addAll(topCenter, topCenter, t2, t2, t0, t0);
        }

        // Bottom cap
        for (int i = 0; i < sides; i++) {
            int t1 = i * 2 + 1;
            int t3 = ((i + 1) % sides) * 2 + 1;
            mesh.getFaces().addAll(bottomCenter, bottomCenter, t1, t1, t3, t3);
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a capsule (cylinder with hemispherical caps)
     * Simplified implementation - returns a cylinder
     */
    public static Cylinder createCapsule(double radius, double cylinderHeight, PhongMaterial material) {
        // Simplified version - just return a cylinder
        // Full implementation would create custom mesh with spherical caps
        Cylinder cylinder = new Cylinder(radius, cylinderHeight);
        cylinder.setMaterial(material);
        return cylinder;
    }

    /**
     * Create a tube (hollow cylinder)
     */
    public static MeshView createTube(double outerRadius, double innerRadius, double height, PhongMaterial material) {
        TriangleMesh mesh = new TriangleMesh();
        int segments = 32;
        float h = (float) height / 2;

        // Outer and inner vertices
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            float cosA = (float) Math.cos(angle);
            float sinA = (float) Math.sin(angle);

            // Top outer
            mesh.getPoints().addAll(
                    (float) outerRadius * cosA, -h, (float) outerRadius * sinA);
            // Top inner
            mesh.getPoints().addAll(
                    (float) innerRadius * cosA, -h, (float) innerRadius * sinA);
            // Bottom outer
            mesh.getPoints().addAll(
                    (float) outerRadius * cosA, h, (float) outerRadius * sinA);
            // Bottom inner
            mesh.getPoints().addAll(
                    (float) innerRadius * cosA, h, (float) innerRadius * sinA);
        }

        // Texture coordinates
        for (int i = 0; i <= segments * 4; i++) {
            mesh.getTexCoords().addAll(0.5f, 0.5f);
        }

        // Generate faces (simplified)
        for (int i = 0; i < segments; i++) {
            int base = i * 4;
            int next = ((i + 1) % (segments + 1)) * 4;

            // Outer wall
            mesh.getFaces().addAll(base, base, next, next, base + 2, base + 2);
            mesh.getFaces().addAll(base + 2, base + 2, next, next, next + 2, next + 2);

            // Inner wall
            mesh.getFaces().addAll(base + 1, base + 1, base + 3, base + 3, next + 1, next + 1);
            mesh.getFaces().addAll(next + 1, next + 1, base + 3, base + 3, next + 3, next + 3);
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        return meshView;
    }

    /**
     * Create a ring (like a flat torus)
     */
    public static MeshView createRing(double outerRadius, double thickness, PhongMaterial material) {
        return createTorus(outerRadius - thickness / 2, thickness / 2, 32, 16, material);
    }
}
