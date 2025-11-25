package com.graphics;

// mvn exec:java
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.*;

public class GraphicsSystem extends Application {

    private SubScene subScene;
    private Group root3D;
    private PerspectiveCamera camera;

    // Camera transforms
    private final Rotate cameraXRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate cameraYRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(0, 0, -15);

    // Object Management
    private final Map<String, Node> objectMap = new HashMap<>();

    // Advanced managers
    private BoundingBoxController boundingBoxController;
    private MultiSelectionManager selectionManager;

    // UI Components
    private Label statusLabel;
    private Label cameraPosLabel;
    private Label modeLabel;
    private Label selectionLabel;
    private Label sceneModeLabel;
    private TextField scaleField;
    private TextField rotateField;
    private TextField posXField, posYField, posZField;
    private ListView<String> objectListView;
    private TabPane leftTabPane;

    // Material selection
    private ComboBox<MaterialLibrary.MaterialType> materialComboBox;

    // Mouse tracking
    private double mouseOldX, mouseOldY;

    // Lighting
    private AmbientLight ambientLight;
    private PointLight pointLight;
    private Sphere pointLightIndicator;
    private boolean showLightIndicators = true;

    // Bounding boxes
    private boolean showBoundingBoxes = false;

    // Interaction modes
    private boolean cameraLocked = false; // false = camera mode, true = object move mode
    private boolean isDraggingLight = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Initialize managers
        root3D = new Group();
        boundingBoxController = new BoundingBoxController(root3D);
        selectionManager = new MultiSelectionManager();

        // Menu Bar
        MenuBar menuBar = createMenuBar();

        // Initialize UI components
        objectListView = new ListView<>();
        objectListView.setPrefHeight(200);
        objectListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        objectListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean ctrlPressed = false; // TODO: track ctrl state
                Node node = objectMap.get(newVal);
                if (node != null) {
                    selectionManager.select(node, ctrlPressed);
                    updateSelectionInfo();
                }
            }
        });

        // Create 3D Scene
        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(20, 20, 30));

        // Setup Camera
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.getTransforms().addAll(cameraXRotate, cameraYRotate, cameraTranslate);
        subScene.setCamera(camera);

        // Setup Lighting
        ambientLight = new AmbientLight(Color.gray(0.3));
        pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(5);
        pointLight.setTranslateY(-5);
        pointLight.setTranslateZ(-10);
        root3D.getChildren().addAll(ambientLight, pointLight);

        // Create light indicator
        createLightIndicator();

        // Create UI Panel (Left)
        VBox leftPanel = createControlPanel();

        // Create Status Bar (Bottom)
        HBox statusBar = createStatusBar();

        // Add mode indicator overlay
        Label sceneModeLabel = new Label("模式: 相机");
        sceneModeLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-padding: 8px 12px; -fx-font-size: 14px; -fx-font-weight: bold;");
        sceneModeLabel.setMouseTransparent(true);
        StackPane sceneContainer = new StackPane();
        sceneContainer.getChildren().addAll(subScene, sceneModeLabel);
        StackPane.setAlignment(sceneModeLabel, javafx.geometry.Pos.TOP_LEFT);
        StackPane.setMargin(sceneModeLabel, new Insets(10));

        // Layout
        root.setTop(menuBar);
        root.setCenter(sceneContainer);
        root.setLeft(leftPanel);
        root.setBottom(statusBar);

        // Event Handlers
        setupEventHandlers();

        Scene scene = new Scene(root, 1200, 800);

        // Add keyboard event handlers
        scene.setOnKeyPressed(e -> handleKeyPress(e));

        primaryStage.setTitle("Advanced JavaFX 3D Graphics System");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Add initial objects
        addInitialObjects();

        // Bind SubScene size
        subScene.widthProperty().bind(root.widthProperty().subtract(leftPanel.widthProperty()));
        subScene.heightProperty().bind(root.heightProperty().subtract(statusBar.heightProperty()));
    }

    private void addInitialObjects() {
        addObjectFromFactory(ShapeFactory.ShapeType.CUBE, -3, 0, 0);
        addObjectFromFactory(ShapeFactory.ShapeType.SPHERE, 0, 0, 0);
        addObjectFromFactory(ShapeFactory.ShapeType.TORUS, 3, 0, 0);
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(300);
        panel.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");

        Label title = new Label("Advanced Controls");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");

        // Create tabbed pane
        leftTabPane = new TabPane();
        leftTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Objects
        Tab objectsTab = new Tab("Objects");
        objectsTab.setContent(createObjectsPane());

        // Tab 2: Transform
        Tab transformTab = new Tab("Transform");
        transformTab.setContent(createTransformPane());

        // Tab 3: Materials
        Tab materialsTab = new Tab("Materials");
        materialsTab.setContent(createMaterialsPane());

        // Tab 4: Lighting
        Tab lightingTab = new Tab("Lighting");
        lightingTab.setContent(createLightingPane());

        leftTabPane.getTabs().addAll(objectsTab, transformTab, materialsTab, lightingTab);

        panel.getChildren().addAll(title, leftTabPane);
        VBox.setVgrow(leftTabPane, Priority.ALWAYS);

        return panel;
    }

    private Node createObjectsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label label = new Label("Add Shapes:");
        label.setStyle("-fx-text-fill: white;");

        // Shape buttons in grid
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);

        String[][] shapes = {
                { "Cube", "Sphere", "Cylinder" },
                { "Cone", "Torus", "Pyramid" },
                { "Tetrahedron", "Octahedron", "Dodecahedron" },
                { "Icosahedron", "Prism", "Tube" }
        };

        for (int row = 0; row < shapes.length; row++) {
            for (int col = 0; col < shapes[row].length; col++) {
                String shapeName = shapes[row][col];
                Button btn = new Button(shapeName);
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-font-size: 10px;");

                final String name = shapeName.toUpperCase();
                btn.setOnAction(e -> addObjectFromFactory(ShapeFactory.ShapeType.valueOf(name), 0, 0, 0));

                grid.add(btn, col, row);
            }
        }

        // Scene objects list
        Label listLabel = new Label("Scene Objects:");
        listLabel.setStyle("-fx-text-fill: white;");

        Button deleteBtn = createButton("Delete Selected", this::deleteSelected);
        Button clearBtn = createButton("Clear Scene", this::clearScene);

        pane.getChildren().addAll(label, grid, listLabel, objectListView, deleteBtn, clearBtn);
        VBox.setVgrow(objectListView, Priority.ALWAYS);

        return pane;
    }

    private Node createTransformPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(8);

        scaleField = new TextField("1.0");
        rotateField = new TextField("0.0");
        posXField = new TextField("0.0");
        posYField = new TextField("0.0");
        posZField = new TextField("0.0");

        Label[] labels = {
                new Label("Scale:"),
                new Label("Rotate:"),
                new Label("Pos X:"),
                new Label("Pos Y:"),
                new Label("Pos Z:")
        };

        for (Label lbl : labels) {
            lbl.setStyle("-fx-text-fill: white;");
        }

        grid.addRow(0, labels[0], scaleField);
        grid.addRow(1, labels[1], rotateField);
        grid.addRow(2, labels[2], posXField);
        grid.addRow(3, labels[3], posYField);
        grid.addRow(4, labels[4], posZField);

        Button applyBtn = createButton("Apply Transform", this::applyTransform);

        // Alignment tools
        Label alignLabel = new Label("Alignment Tools:");
        alignLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        GridPane alignGrid = new GridPane();
        alignGrid.setHgap(5);
        alignGrid.setVgap(5);

        Button alignLeftBtn = createSmallButton("Left",
                () -> selectionManager.alignSelection(MultiSelectionManager.Alignment.LEFT));
        Button alignCenterXBtn = createSmallButton("Center X",
                () -> selectionManager.alignSelection(MultiSelectionManager.Alignment.CENTER_X));
        Button alignRightBtn = createSmallButton("Right",
                () -> selectionManager.alignSelection(MultiSelectionManager.Alignment.RIGHT));

        Button alignTopBtn = createSmallButton("Top",
                () -> selectionManager.alignSelection(MultiSelectionManager.Alignment.TOP));
        Button alignCenterYBtn = createSmallButton("Center Y",
                () -> selectionManager.alignSelection(MultiSelectionManager.Alignment.CENTER_Y));
        Button alignBottomBtn = createSmallButton("Bottom",
                () -> selectionManager.alignSelection(MultiSelectionManager.Alignment.BOTTOM));

        alignGrid.addRow(0, alignLeftBtn, alignCenterXBtn, alignRightBtn);
        alignGrid.addRow(1, alignTopBtn, alignCenterYBtn, alignBottomBtn);

        pane.getChildren().addAll(grid, applyBtn, alignLabel, alignGrid);

        return pane;
    }

    private Node createMaterialsPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label label = new Label("Material Presets:");
        label.setStyle("-fx-text-fill: white;");

        materialComboBox = new ComboBox<>();
        materialComboBox.getItems().addAll(MaterialLibrary.MaterialType.values());
        materialComboBox.setMaxWidth(Double.MAX_VALUE);
        materialComboBox.setValue(MaterialLibrary.MaterialType.PLASTIC_GLOSSY);

        Button applyMatBtn = createButton("Apply Material", this::applyMaterial);

        ColorPicker colorPicker = new ColorPicker(Color.RED);
        Button applyColorBtn = createButton("Apply Color", () -> applyCustomColor(colorPicker.getValue()));

        pane.getChildren().addAll(label, materialComboBox, applyMatBtn,
                new Separator(), new Label("Custom Color:"), colorPicker, applyColorBtn);

        return pane;
    }

    private Node createLightingPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(10));

        Label ambLabel = new Label("Ambient Light:");
        ambLabel.setStyle("-fx-text-fill: white;");

        Slider ambientSlider = new Slider(0, 1, 0.3);
        ambientSlider.setShowTickLabels(true);
        ambientSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ambientLight.setColor(Color.gray(newVal.doubleValue()));
        });

        Label pointLabel = new Label("Point Light:");
        pointLabel.setStyle("-fx-text-fill: white;");

        Slider pointSlider = new Slider(0, 1, 1.0);
        pointSlider.setShowTickLabels(true);
        pointSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double intensity = newVal.doubleValue();
            pointLight.setColor(Color.color(intensity, intensity, intensity));
        });

        TextField lightXField = new TextField("5.0");
        TextField lightYField = new TextField("-5.0");
        TextField lightZField = new TextField("-10.0");

        GridPane lightPosGrid = new GridPane();
        lightPosGrid.setHgap(5);
        lightPosGrid.setVgap(5);

        Label[] lightLabels = { new Label("X:"), new Label("Y:"), new Label("Z:") };
        for (Label lbl : lightLabels) {
            lbl.setStyle("-fx-text-fill: white;");
        }

        lightPosGrid.addRow(0, lightLabels[0], lightXField);
        lightPosGrid.addRow(1, lightLabels[1], lightYField);
        lightPosGrid.addRow(2, lightLabels[2], lightZField);

        Button applyLightPos = createButton("Update Position", () -> {
            try {
                double x = Double.parseDouble(lightXField.getText());
                double y = Double.parseDouble(lightYField.getText());
                double z = Double.parseDouble(lightZField.getText());
                pointLight.setTranslateX(x);
                pointLight.setTranslateY(y);
                pointLight.setTranslateZ(z);
                if (pointLightIndicator != null) {
                    pointLightIndicator.setTranslateX(x);
                    pointLightIndicator.setTranslateY(y);
                    pointLightIndicator.setTranslateZ(z);
                }
                statusLabel.setText("Light position updated");
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid light position");
            }
        });

        pane.getChildren().addAll(ambLabel, ambientSlider, pointLabel, pointSlider,
                new Label("Light Position:"), lightPosGrid, applyLightPos);

        return pane;
    }

    private void createLightIndicator() {
        pointLightIndicator = new Sphere(0.2);
        PhongMaterial indicatorMat = new PhongMaterial(Color.YELLOW);
        pointLightIndicator.setMaterial(indicatorMat);

        pointLightIndicator.setTranslateX(pointLight.getTranslateX());
        pointLightIndicator.setTranslateY(pointLight.getTranslateY());
        pointLightIndicator.setTranslateZ(pointLight.getTranslateZ());

        root3D.getChildren().add(pointLightIndicator);
        pointLightIndicator.setVisible(showLightIndicators);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem newScene = new MenuItem("New Scene");
        newScene.setOnAction(e -> clearScene());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(newScene, new SeparatorMenuItem(), exitItem);

        // View Menu
        Menu viewMenu = new Menu("View");
        MenuItem resetCamera = new MenuItem("Reset Camera");
        resetCamera.setOnAction(e -> resetCameraView());

        CheckMenuItem toggleBBox = new CheckMenuItem("Show Bounding Boxes");
        toggleBBox.setSelected(showBoundingBoxes);
        toggleBBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showBoundingBoxes = newVal;
            if (newVal) {
                boundingBoxController.showAll();
            } else {
                boundingBoxController.hideAll();
            }
        });

        CheckMenuItem toggleLights = new CheckMenuItem("Show Light Indicators");
        toggleLights.setSelected(true);
        toggleLights.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showLightIndicators = newVal;
            if (pointLightIndicator != null) {
                pointLightIndicator.setVisible(newVal);
            }
        });

        CheckMenuItem lockAspect = new CheckMenuItem("Lock Aspect Ratio");
        lockAspect.selectedProperty().addListener((obs, oldVal, newVal) -> {
            boundingBoxController.setAspectRatioLocked(newVal);
        });

        viewMenu.getItems().addAll(resetCamera, new SeparatorMenuItem(),
                toggleBBox, toggleLights, lockAspect);

        // Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        MenuItem controlsItem = new MenuItem("Controls");
        controlsItem.setOnAction(e -> showControlsDialog());
        helpMenu.getItems().addAll(controlsItem, aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private void resetCameraView() {
        cameraXRotate.setAngle(0);
        cameraYRotate.setAngle(0);
        cameraTranslate.setX(0);
        cameraTranslate.setY(0);
        cameraTranslate.setZ(-15);
        updateCameraStatus();
        statusLabel.setText("Camera reset to default");
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Advanced JavaFX 3D Graphics System");
        alert.setContentText(
                "Version 2.0\n\n" +
                        "Features:\n" +
                        "• 14+ geometric shapes\n" +
                        "• Interactive bounding boxes with vertex dragging\n" +
                        "• Multi-selection and alignment tools\n" +
                        "• 25+ material presets\n" +
                        "• Advanced lighting controls\n\n" +
                        "山东财经大学 计算机图形学课程");
        alert.showAndWait();
    }

    private void showControlsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Controls");
        alert.setHeaderText("Navigation & Interaction");
        alert.setContentText(
                "Camera Controls:\n" +
                        "  • Left Drag: Rotate camera\n" +
                        "  • Right Drag: Pan camera\n" +
                        "  • Scroll: Zoom in/out\n\n" +
                        "Object Interaction:\n" +
                        "  • Click: Select object\n" +
                        "  • Ctrl+Click: Multi-select\n" +
                        "  • Drag Bounding Box Vertices: Stretch shape\n\n" +
                        "Features:\n" +
                        "  • View > Show Bounding Boxes\n" +
                        "  • View > Lock Aspect Ratio");
        alert.showAndWait();
    }

    private HBox createStatusBar() {
        HBox bar = new HBox(15);
        bar.setPadding(new Insets(5, 10, 5, 10));
        bar.setStyle("-fx-background-color: #1e1e1e;");

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: white;");

        cameraPosLabel = new Label("Camera: (0, 0, -15)");
        cameraPosLabel.setStyle("-fx-text-fill: white;");

        modeLabel = new Label("Mode: CAMERA");
        modeLabel.setStyle("-fx-text-fill: white;");

        selectionLabel = new Label("Selected: 0");
        selectionLabel.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(statusLabel, spacer, selectionLabel,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                modeLabel,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                cameraPosLabel);

        return bar;
    }

    private Button createButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button createSmallButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(80);
        btn.setStyle("-fx-font-size: 9px;");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void addObjectFromFactory(ShapeFactory.ShapeType type, double x, double y, double z) {
        PhongMaterial material = new PhongMaterial(Color.color(Math.random(), Math.random(), Math.random()));
        Node node = ShapeFactory.createShape(type, material);

        if (node != null) {
            node.setTranslateX(x);
            node.setTranslateY(y);
            node.setTranslateZ(z);

            String name = type.name() + " " + (objectMap.size() + 1);
            node.setId(name);

            // Add context menu
            createContextMenu(node);

            root3D.getChildren().add(node);
            objectMap.put(name, node);
            objectListView.getItems().add(name);

            // Create bounding box
            boundingBoxController.createBoundingBox(node);
            if (showBoundingBoxes) {
                boundingBoxController.updateBoundingBox(node);
            }

            statusLabel.setText("Added " + name);
        }
    }

    private void createContextMenu(Node node) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem propertiesItem = new MenuItem("Properties...");
        propertiesItem.setOnAction(e -> showPropertiesDialog(node));

        MenuItem colorItem = new MenuItem("Change Color...");
        colorItem.setOnAction(e -> {
            if (node instanceof Shape3D) {
                ColorPicker picker = new ColorPicker();
                Dialog<Color> dialog = new Dialog<>();
                dialog.setTitle("Change Color");
                dialog.setHeaderText("Select color for " + node.getId());
                dialog.getDialogPane().setContent(picker);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dialog.setResultConverter(btn -> btn == ButtonType.OK ? picker.getValue() : null);

                dialog.showAndWait().ifPresent(color -> {
                    PhongMaterial newMat = new PhongMaterial(color);
                    ((Shape3D) node).setMaterial(newMat);
                    statusLabel.setText("Color changed");
                });
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            root3D.getChildren().remove(node);
            objectMap.remove(node.getId());
            objectListView.getItems().remove(node.getId());
            boundingBoxController.removeBoundingBox(node);
            selectionManager.deselect(node);
            updateSelectionInfo();
            statusLabel.setText("Deleted " + node.getId());
        });

        contextMenu.getItems().addAll(propertiesItem, colorItem,
                new SeparatorMenuItem(), deleteItem);

        node.setOnContextMenuRequested(event -> {
            contextMenu.show(node, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    private void showPropertiesDialog(Node node) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Object Properties");
        dialog.setHeaderText(node.getId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Type:"), 0, 0);
        grid.add(new Label(node.getClass().getSimpleName()), 1, 0);
        grid.add(new Label("Position X:"), 0, 1);
        grid.add(new Label(String.format("%.2f", node.getTranslateX())), 1, 1);
        grid.add(new Label("Position Y:"), 0, 2);
        grid.add(new Label(String.format("%.2f", node.getTranslateY())), 1, 2);
        grid.add(new Label("Position Z:"), 0, 3);
        grid.add(new Label(String.format("%.2f", node.getTranslateZ())), 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void clearScene() {
        // Keep Lights and indicators
        List<Node> toRemove = new ArrayList<>();
        for (Node node : root3D.getChildren()) {
            if (!(node instanceof LightBase) && node != pointLightIndicator) {
                toRemove.add(node);
            }
        }

        root3D.getChildren().removeAll(toRemove);
        objectMap.clear();
        objectListView.getItems().clear();
        boundingBoxController.clearAll();
        selectionManager.clearSelection();
        updateSelectionInfo();
        statusLabel.setText("Scene Cleared");
    }

    private void deleteSelected() {
        Set<Node> selected = selectionManager.getSelectedObjects();
        if (selected.isEmpty()) {
            statusLabel.setText("No objects selected");
            return;
        }

        for (Node node : selected) {
            root3D.getChildren().remove(node);
            objectMap.remove(node.getId());
            objectListView.getItems().remove(node.getId());
            boundingBoxController.removeBoundingBox(node);
        }

        selectionManager.clearSelection();
        updateSelectionInfo();
        statusLabel.setText("Deleted " + selected.size() + " object(s)");
    }

    private void applyTransform() {
        Set<Node> selected = selectionManager.getSelectedObjects();
        if (selected.isEmpty()) {
            statusLabel.setText("No objects selected");
            return;
        }

        try {
            double scale = Double.parseDouble(scaleField.getText());
            double rotate = Double.parseDouble(rotateField.getText());
            double x = Double.parseDouble(posXField.getText());
            double y = Double.parseDouble(posYField.getText());
            double z = Double.parseDouble(posZField.getText());

            for (Node node : selected) {
                node.setScaleX(scale);
                node.setScaleY(scale);
                node.setScaleZ(scale);
                node.setRotate(rotate);
                node.setTranslateX(x);
                node.setTranslateY(y);
                node.setTranslateZ(z);

                boundingBoxController.updateBoundingBox(node);
            }

            statusLabel.setText("Transform applied to " + selected.size() + " object(s)");
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid input values");
        }
    }

    private void applyMaterial() {
        Set<Node> selected = selectionManager.getSelectedObjects();
        if (selected.isEmpty()) {
            statusLabel.setText("No objects selected");
            return;
        }

        MaterialLibrary.MaterialType matType = materialComboBox.getValue();
        if (matType == null) {
            return;
        }

        for (Node node : selected) {
            if (node instanceof Shape3D) {
                PhongMaterial material = MaterialLibrary.getMaterialCopy(matType);
                ((Shape3D) node).setMaterial(material);
            }
        }

        statusLabel.setText("Material applied: " + matType.name());
    }

    private void applyCustomColor(Color color) {
        Set<Node> selected = selectionManager.getSelectedObjects();
        if (selected.isEmpty()) {
            statusLabel.setText("No objects selected");
            return;
        }

        for (Node node : selected) {
            if (node instanceof Shape3D) {
                PhongMaterial material = new PhongMaterial(color);
                ((Shape3D) node).setMaterial(material);
            }
        }

        statusLabel.setText("Custom color applied");
    }

    private void setupEventHandlers() {
        subScene.setOnMousePressed(e -> {
            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();

            // Object picking
            if (e.isPrimaryButtonDown()) {
                PickResult result = e.getPickResult();
                Node picked = result.getIntersectedNode();

                if (picked != null && picked != subScene && objectMap.containsValue(picked)) {
                    selectionManager.select(picked, e.isControlDown());
                    updateSelectionInfo();
                } else if (!e.isControlDown()) {
                    selectionManager.clearSelection();
                    updateSelectionInfo();
                }
            }
        });

        subScene.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - mouseOldX;
            double dy = e.getSceneY() - mouseOldY;

            if (e.isPrimaryButtonDown() && !e.isControlDown()) {
                if (cameraLocked) {
                    // Object move mode: move selected objects
                    if (selectionManager.getSelectionCount() > 0) {
                        selectionManager.translateSelection(dx * 0.05, dy * 0.05, 0);
                        for (Node node : selectionManager.getSelectedObjects()) {
                            boundingBoxController.updateBoundingBox(node);
                        }
                    }
                } else {
                    // Camera mode: rotate camera
                    cameraXRotate.setAngle(cameraXRotate.getAngle() - dy * 0.2);
                    cameraYRotate.setAngle(cameraYRotate.getAngle() + dx * 0.2);
                }
            } else if (e.isSecondaryButtonDown()) {
                // Pan camera
                cameraTranslate.setX(cameraTranslate.getX() + dx * 0.05);
                cameraTranslate.setY(cameraTranslate.getY() + dy * 0.05);
            }

            mouseOldX = e.getSceneX();
            mouseOldY = e.getSceneY();
            updateCameraStatus();
        });

        // Scroll Zoom
        subScene.setOnScroll((ScrollEvent e) -> {
            double delta = e.getDeltaY();
            cameraTranslate.setZ(cameraTranslate.getZ() + delta * 0.05);
            updateCameraStatus();
        });
    }

    private void updateCameraStatus() {
        cameraPosLabel.setText(String.format("Camera: (%.1f, %.1f, %.1f)",
                cameraTranslate.getX(), cameraTranslate.getY(), cameraTranslate.getZ()));
    }

    private void updateSelectionInfo() {
        int count = selectionManager.getSelectionCount();
        selectionLabel.setText("Selected: " + count);
    }

    /**
     * Handle keyboard shortcuts
     */
    private void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case BACK_SPACE:
            case DELETE:
                // Delete selected objects
                deleteSelected();
                e.consume();
                break;

            case R:
                // Toggle camera lock / object move mode (Ctrl+R)
                if (e.isControlDown()) {
                    cameraLocked = !cameraLocked;
                    updateModeLabel();
                    if (sceneModeLabel != null) {
                        sceneModeLabel.setText(cameraLocked ? "模式: 移动对象" : "模式: 相机");
                    }
                    statusLabel.setText(cameraLocked ? "Mode: OBJECT MOVE..." : "Mode: CAMERA...");
                    e.consume();
                }
                break;

            case ESCAPE:
                // Clear selection
                selectionManager.clearSelection();
                updateSelectionInfo();
                e.consume();
                break;
        }
    }

    private void updateModeLabel() {
        if (modeLabel != null) {
            modeLabel.setText("Mode: " + (cameraLocked ? "MOVE OBJECT" : "CAMERA"));
        }
    }
}
