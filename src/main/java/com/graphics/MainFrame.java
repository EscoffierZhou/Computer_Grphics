package com.graphics;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * ====================================================================
 * MainFrame.java - 应用程序主窗口
 * ====================================================================
 * 
 * 【功能说明】
 * 这是应用程序的入口类和主界面。
 * 包含菜单栏、工具栏、状态栏，以及2D/3D显示区域。
 * 
 * 【界面结构】
 * ┌─────────────────────────────────────────────────────────┐
 * │ 菜单栏 (9个菜单) │
 * ├─────────────────────────────────────────────────────────┤
 * │ 工具栏按钮: 2D 3D │ 平移 放缩 旋转 │ 裁剪 │ 透视 正交 ... │
 * ├─────────────────────────────────────────────────────────┤
 * │ │
 * │ 主显示区域 (CardLayout) │
 * │ - 3D场景面板 (Scene3DPanel) │
 * │ - 2D画布面板 (Canvas2DPanel) │
 * │ │
 * ├─────────────────────────────────────────────────────────┤
 * │ 状态栏: 就绪 | Ready 模式: 3D场景 │
 * └─────────────────────────────────────────────────────────┘
 * 
 * 【菜单项对应课程大纲】
 * 1. 文件 - 保存画布、退出
 * 2. 光栅图形 - 直线、圆、多边形扫描转换
 * 3. 变换 - 平移、旋转、缩放、对称、组合变换
 * 4. 裁剪 - Cohen-Sutherland, Cyrus-Beck, 多边形裁剪
 * 5. 投影 - 透视投影、正交投影、视点变换
 * 6. 人机交互 - 选择、平移、旋转、缩放模式
 * 7. 可见面判定 - Z-Buffer、扫描线消隐、背面剔除
 * 8. 光照明模型 - 环境光、点光源、材质、Flat/Gouraud/Phong着色
 * 9. 机器人动画 - 姿态重置、关节动画、形状设计器
 * 10. 帮助 - 项目说明书、操作指南、关于
 * 
 * @author Computer Graphics Course
 */
public class MainFrame extends JFrame {

    // ==================== 主显示面板 ====================

    /** 3D场景面板 - 显示机器人和舞台 */
    private Scene3DPanel scene3DPanel;

    /** 2D画布面板 - 演示2D图形算法 */
    private Canvas2DPanel canvas2DPanel;

    /** 用于切换2D/3D显示的面板 */
    private JPanel displayPanel;

    /** CardLayout用于切换显示 */
    private CardLayout cardLayout;

    // ==================== 状态栏组件 ====================

    /** 状态信息标签 */
    private JLabel statusLabel;

    /** 当前模式标签 */
    private JLabel modeLabel;

    // ==================== 当前模式 ====================

    /** 是否处于2D模式（false表示3D模式） */
    private boolean is2DMode = false;

    // ==================== 构造函数 ====================

    public MainFrame() {
        super("计算机图形学课程作业:周兴&王小雨&纪昕池");
        initializeUI();
    }

    /**
     * 初始化用户界面
     */
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null); // 窗口居中

        // 使用BorderLayout布局
        setLayout(new BorderLayout());

        // 创建菜单栏
        setJMenuBar(createMenuBar());

        // 创建快速工具栏（顶部）
        add(createToolBar(), BorderLayout.NORTH);

        // 创建主显示区域
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 初始化3D和2D面板
        scene3DPanel = new Scene3DPanel();
        canvas2DPanel = new Canvas2DPanel();

        // 使用CardLayout在2D和3D之间切换
        cardLayout = new CardLayout();
        displayPanel = new JPanel(cardLayout);
        displayPanel.add(scene3DPanel, "3D");
        displayPanel.add(canvas2DPanel, "2D");

        mainPanel.add(displayPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // 创建状态栏（底部）
        add(createStatusBar(), BorderLayout.SOUTH);

        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateStatus("就绪 | Ready");
    }

    // ==================== 模式切换 ====================

    /**
     * 切换到2D模式
     * 显示2D画布，用于演示光栅化算法
     */
    private void switchTo2D() {
        cardLayout.show(displayPanel, "2D");
        is2DMode = true;
        modeLabel.setText("模式: 2D画布");
    }

    /**
     * 切换到3D模式
     * 显示3D场景，包含机器人和舞台
     */
    private void switchTo3D() {
        cardLayout.show(displayPanel, "3D");
        is2DMode = false;
        modeLabel.setText("模式: 3D场景");
    }

    // ==================== 菜单栏创建 ====================

    /**
     * 创建菜单栏
     * 包含9个菜单，对应课程大纲的各个模块
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ========== 0. 文件菜单 ==========
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(createMenuItem("保存画布", e -> saveCanvasImage()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("退出", e -> System.exit(0)));
        menuBar.add(fileMenu);

        // ========== 1. 光栅图形绘制 ==========
        // 对应课程大纲: 直线扫描转换、圆扫描转换、多边形扫描、区域填充
        JMenu rasterMenu = new JMenu("光栅图形");
        rasterMenu.add(createMenuItem("切换到2D画布", e -> switchTo2D()));
        rasterMenu.add(createMenuItem("切换到3D场景", e -> switchTo3D()));
        rasterMenu.addSeparator();
        rasterMenu.add(createMenuItem("直线绘制 (Bresenham)", e -> showLineDialog()));
        rasterMenu.add(createMenuItem("圆绘制", e -> showCircleDialog()));
        rasterMenu.addSeparator();
        rasterMenu.add(createMenuItem("多边形扫描填充", e -> showPolygonScanDialog()));
        rasterMenu.add(createMenuItem("区域填充", e -> showFillDialog()));
        rasterMenu.addSeparator();
        rasterMenu.add(createMenuItem("文字工具", e -> showTextDialog()));
        rasterMenu.add(createMenuItem("设置线宽", e -> showLineWidthDialog()));
        rasterMenu.addSeparator();
        rasterMenu.add(createMenuItem("清空画布", e -> {
            canvas2DPanel.clearCanvas();
            updateStatus("画布已清空");
        }));
        menuBar.add(rasterMenu);

        // ========== 2. 几何变换 ==========
        // 对应课程大纲: 平移、缩放、旋转、对称、组合变换
        JMenu transformMenu = new JMenu("变换");
        transformMenu.add(createMenuItem("平移", e -> showTranslateDialog()));
        transformMenu.add(createMenuItem("放缩", e -> showScaleDialog()));
        transformMenu.add(createMenuItem("旋转", e -> showRotateDialog()));
        transformMenu.add(createMenuItem("对称", e -> showSymmetryDialog()));
        transformMenu.addSeparator();
        transformMenu.add(createMenuItem("组合变换", e -> showCombinedTransformDialog()));
        menuBar.add(transformMenu);

        // ========== 3. 裁剪算法 ==========
        // 对应课程大纲: Sutherland-Cohen, Cyrus-Beck, 多边形裁剪
        JMenu clipMenu = new JMenu("裁剪");
        clipMenu.add(createMenuItem("Sutherland-Cohen", e -> showCohenClipDialog()));
        clipMenu.add(createMenuItem("Cyrus-Beck", e -> showCyrusBeckDialog()));
        clipMenu.add(createMenuItem("多边形裁剪", e -> showPolygonClipDialog()));
        menuBar.add(clipMenu);

        // ========== 4. 投影变换 ==========
        // 对应课程大纲: 投影变换、视见体变换
        JMenu projectionMenu = new JMenu("投影");
        projectionMenu.add(createMenuItem("透视投影 (Frustum)", e -> setFrustumProjection()));
        projectionMenu.add(createMenuItem("透视投影 (Perspective)", e -> setPerspectiveProjection()));
        projectionMenu.add(createMenuItem("平行投影 (正交)", e -> setOrthographicProjection()));
        projectionMenu.addSeparator();
        projectionMenu.add(createMenuItem("视点变换", e -> showViewpointDialog()));
        menuBar.add(projectionMenu);

        // ========== 5. 人机交互 ==========
        JMenu interactionMenu = new JMenu("人机交互");
        interactionMenu.add(createMenuItem("选择模式", e -> setInteractionMode("SELECT")));
        interactionMenu.add(createMenuItem("平移模式", e -> setInteractionMode("TRANSLATE")));
        interactionMenu.add(createMenuItem("旋转模式", e -> setInteractionMode("ROTATE")));
        interactionMenu.add(createMenuItem("缩放模式", e -> setInteractionMode("SCALE")));
        menuBar.add(interactionMenu);

        // ========== 6. 可见面判定 ==========
        // 对应课程大纲: Z-Buffer、扫描线消隐、背面剔除
        JMenu visibilityMenu = new JMenu("可见面判定");
        visibilityMenu.add(createMenuItem("Z-Buffer消隐", e -> enableZBuffer()));
        visibilityMenu.add(createMenuItem("扫描线消隐", e -> enableScanlineHSR()));
        visibilityMenu.add(createMenuItem("后向面消除", e -> enableBackfaceCulling()));
        visibilityMenu.addSeparator();
        visibilityMenu.add(createMenuItem("显示线框 (未消隐)", e -> showWireframe()));
        menuBar.add(visibilityMenu);

        // ========== 7. 光照明模型 ==========
        // 对应课程大纲: 光照模型、明暗处理
        JMenu lightingMenu = new JMenu("光照明模型");
        lightingMenu.add(createMenuItem("环境光设置", e -> showAmbientLightDialog()));
        lightingMenu.add(createMenuItem("点光源设置", e -> showPointLightDialog()));
        lightingMenu.addSeparator();
        lightingMenu.add(createMenuItem("材质设置", e -> showMaterialDialog()));
        lightingMenu.addSeparator();
        lightingMenu.add(createMenuItem("Flat明暗 (无插值)", e -> setFlatShading()));
        lightingMenu.add(createMenuItem("Gouraud明暗", e -> setGouraudShading()));
        lightingMenu.add(createMenuItem("Phong明暗", e -> setPhongShading()));
        menuBar.add(lightingMenu);

        // ========== 8. 机器人动画 ==========
        // 附加功能：多边形机器人和关节动画
        JMenu robotMenu = new JMenu("机器人动画");
        robotMenu.add(createMenuItem("重置姿态", e -> resetRobotPose()));
        robotMenu.addSeparator();
        robotMenu.add(createMenuItem("形状设计器", e -> showShapeDesigner()));
        robotMenu.add(createMenuItem("添加机器人", e -> addRobot()));
        robotMenu.add(createMenuItem("删除最后一个", e -> removeLastRobot()));
        robotMenu.addSeparator();
        robotMenu.add(createMenuItem("头部旋转", e -> rotateHead()));
        robotMenu.add(createMenuItem("手臂动画", e -> animateArms()));
        robotMenu.add(createMenuItem("腿部动画", e -> animateLegs()));
        robotMenu.addSeparator();
        robotMenu.add(createMenuItem("行走动画", e -> walkAnimation()));
        robotMenu.add(createMenuItem("挥手动画", e -> waveAnimation()));
        menuBar.add(robotMenu);

        // ========== 9. 帮助 ==========
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.add(createMenuItem("项目说明书", e -> showProjectManual()));
        helpMenu.add(createMenuItem("操作指南", e -> showUserGuide()));
        helpMenu.addSeparator();
        helpMenu.add(createMenuItem("关于", e -> showAbout()));
        menuBar.add(helpMenu);

        return menuBar;
    }

    // ==================== 工具栏创建 ====================

    /**
     * 创建快速工具栏
     * 提供常用功能的快捷访问
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false); // 禁止拖动

        // 模式切换按钮
        toolBar.add(createToolButton("2D", e -> switchTo2D()));
        toolBar.add(createToolButton("3D", e -> switchTo3D()));
        toolBar.addSeparator();

        // 变换操作按钮
        toolBar.add(createToolButton("平移", e -> showTranslateDialog()));
        toolBar.add(createToolButton("放缩", e -> showScaleDialog()));
        toolBar.add(createToolButton("旋转", e -> showRotateDialog()));
        toolBar.addSeparator();

        // 裁剪
        toolBar.add(createToolButton("裁剪", e -> showCohenClipDialog()));
        toolBar.addSeparator();

        // 投影
        toolBar.add(createToolButton("透视", e -> setPerspectiveProjection()));
        toolBar.add(createToolButton("正交", e -> setOrthographicProjection()));
        toolBar.add(createToolButton("视点", e -> showViewpointDialog()));
        toolBar.addSeparator();

        // 其他常用功能
        toolBar.add(createToolButton("清空", e -> clearAll()));
        toolBar.add(createToolButton("消隐", e -> enableZBuffer()));
        toolBar.add(createToolButton("线框", e -> showWireframe()));
        toolBar.add(createToolButton("材质", e -> showMaterialDialog()));
        toolBar.add(createToolButton("明暗", e -> cycleShadingMode()));

        return toolBar;
    }

    /**
     * 循环切换明暗模式
     * Flat → Gouraud → Phong → Flat ...
     */
    private void cycleShadingMode() {
        Scene3DPanel.ShadingMode current = scene3DPanel.getShadingMode();
        Scene3DPanel.ShadingMode next = switch (current) {
            case FLAT -> Scene3DPanel.ShadingMode.GOURAUD;
            case GOURAUD -> Scene3DPanel.ShadingMode.PHONG;
            case PHONG -> Scene3DPanel.ShadingMode.FLAT;
        };
        scene3DPanel.setShadingMode(next);
        updateStatus("明暗模式: " + next);
    }

    // ==================== 状态栏创建 ====================

    /**
     * 创建状态栏
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        statusLabel = new JLabel("就绪 | Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);

        modeLabel = new JLabel("模式: 3D场景");
        modeLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusBar.add(modeLabel, BorderLayout.EAST);

        return statusBar;
    }

    // ==================== 辅助方法 ====================

    private JMenuItem createMenuItem(String text, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(action);
        return item;
    }

    private JButton createToolButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setToolTipText(text);
        button.addActionListener(action);
        button.setFocusPainted(false);
        return button;
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    // ==================== 光栅图形菜单处理 ====================

    private void showLineDialog() {
        switchTo2D();
        LineDrawingDialog dialog = new LineDrawingDialog(this, canvas2DPanel);
        dialog.setVisible(true);
        updateStatus("直线绘制模式 - 在2D画布上绘制");
    }

    private void showCircleDialog() {
        switchTo2D();
        CircleDrawingDialog dialog = new CircleDrawingDialog(this, canvas2DPanel);
        dialog.setVisible(true);
        updateStatus("圆绘制模式");
    }

    private void showPolygonScanDialog() {
        switchTo2D();
        PolygonScanDialog dialog = new PolygonScanDialog(this, canvas2DPanel);
        dialog.setVisible(true);
        updateStatus("多边形扫描填充");
    }

    private void showFillDialog() {
        switchTo2D();
        FillAlgorithmDialog dialog = new FillAlgorithmDialog(this, canvas2DPanel);
        dialog.setVisible(true);
        updateStatus("区域填充模式");
    }

    // ==================== 变换菜单处理 ====================

    private void showTranslateDialog() {
        TransformDialog dialog = new TransformDialog(this, scene3DPanel, TransformDialog.TransformType.TRANSLATE);
        dialog.setVisible(true);
    }

    private void showScaleDialog() {
        TransformDialog dialog = new TransformDialog(this, scene3DPanel, TransformDialog.TransformType.SCALE);
        dialog.setVisible(true);
    }

    private void showRotateDialog() {
        TransformDialog dialog = new TransformDialog(this, scene3DPanel, TransformDialog.TransformType.ROTATE);
        dialog.setVisible(true);
    }

    private void showSymmetryDialog() {
        TransformDialog dialog = new TransformDialog(this, scene3DPanel, TransformDialog.TransformType.SYMMETRY);
        dialog.setVisible(true);
    }

    private void showCombinedTransformDialog() {
        CombinedTransformDialog dialog = new CombinedTransformDialog(this, scene3DPanel);
        dialog.setVisible(true);
    }

    // ==================== 裁剪菜单处理 ====================

    private void showCohenClipDialog() {
        switchTo2D();
        ClippingDialog dialog = new ClippingDialog(this, canvas2DPanel, ClippingDialog.ClipType.COHEN_SUTHERLAND);
        dialog.setVisible(true);
        updateStatus("Sutherland-Cohen裁剪 - 在2D画布上演示");
    }

    private void showCyrusBeckDialog() {
        switchTo2D();
        ClippingDialog dialog = new ClippingDialog(this, canvas2DPanel, ClippingDialog.ClipType.CYRUS_BECK);
        dialog.setVisible(true);
        updateStatus("Cyrus-Beck裁剪");
    }

    private void showPolygonClipDialog() {
        switchTo2D();
        ClippingDialog dialog = new ClippingDialog(this, canvas2DPanel, ClippingDialog.ClipType.POLYGON);
        dialog.setVisible(true);
        updateStatus("多边形裁剪");
    }

    // ==================== 投影菜单处理 ====================

    private void setFrustumProjection() {
        switchTo3D();
        scene3DPanel.setProjectionMode(Scene3DPanel.ProjectionMode.FRUSTUM);
        updateStatus("透视投影 (Frustum) - FOV变化明显");
    }

    private void setPerspectiveProjection() {
        switchTo3D();
        scene3DPanel.setProjectionMode(Scene3DPanel.ProjectionMode.PERSPECTIVE);
        updateStatus("透视投影 (Perspective)");
    }

    private void setOrthographicProjection() {
        switchTo3D();
        scene3DPanel.setProjectionMode(Scene3DPanel.ProjectionMode.ORTHOGRAPHIC);
        updateStatus("平行投影 (正交) - 无透视缩小");
    }

    private void showViewpointDialog() {
        switchTo3D();
        ViewpointDialog dialog = new ViewpointDialog(this, scene3DPanel);
        dialog.setVisible(true);
    }

    // ==================== 人机交互菜单处理 ====================

    private void setInteractionMode(String mode) {
        scene3DPanel.setInteractionMode(mode);
        updateStatus("交互模式: " + mode);
    }

    // ==================== 可见面判定菜单处理 ====================

    private void enableZBuffer() {
        switchTo3D();
        scene3DPanel.setHiddenSurfaceMode(Scene3DPanel.HSRMode.ZBUFFER);
        updateStatus("Z-Buffer消隐 - 正确的深度排序");
    }

    private void enableScanlineHSR() {
        switchTo3D();
        scene3DPanel.setHiddenSurfaceMode(Scene3DPanel.HSRMode.SCANLINE);
        updateStatus("扫描线消隐");
    }

    private void enableBackfaceCulling() {
        switchTo3D();
        scene3DPanel.setHiddenSurfaceMode(Scene3DPanel.HSRMode.BACKFACE);
        updateStatus("后向面消除 - 只绘制朝向观察者的面");
    }

    private void showWireframe() {
        switchTo3D();
        scene3DPanel.setHiddenSurfaceMode(Scene3DPanel.HSRMode.WIREFRAME);
        updateStatus("线框模式 - 显示所有边(包括被遮挡的)");
    }

    // ==================== 光照明模型菜单处理 ====================

    private void showAmbientLightDialog() {
        LightingDialog dialog = new LightingDialog(this, scene3DPanel, LightingDialog.LightType.AMBIENT);
        dialog.setVisible(true);
    }

    private void showPointLightDialog() {
        LightingDialog dialog = new LightingDialog(this, scene3DPanel, LightingDialog.LightType.POINT);
        dialog.setVisible(true);
    }

    private void showMaterialDialog() {
        MaterialDialog dialog = new MaterialDialog(this, scene3DPanel);
        dialog.setVisible(true);
    }

    private void setFlatShading() {
        switchTo3D();
        scene3DPanel.setShadingMode(Scene3DPanel.ShadingMode.FLAT);
        updateStatus("Flat明暗 - 每个面单一颜色,无渐变");
    }

    private void setGouraudShading() {
        switchTo3D();
        scene3DPanel.setShadingMode(Scene3DPanel.ShadingMode.GOURAUD);
        updateStatus("Gouraud明暗 - 顶点着色后插值");
    }

    private void setPhongShading() {
        switchTo3D();
        scene3DPanel.setShadingMode(Scene3DPanel.ShadingMode.PHONG);
        updateStatus("Phong明暗 - 逐像素光照计算,高光明显");
    }

    // ==================== 机器人动画菜单处理 ====================

    private void resetRobotPose() {
        scene3DPanel.getRobot().resetPose();
        scene3DPanel.repaint();
        updateStatus("机器人姿态已重置");
    }

    private void showShapeDesigner() {
        ShapeDesignerDialog dialog = new ShapeDesignerDialog(this, scene3DPanel);
        dialog.setVisible(true);
    }

    private void addRobot() {
        scene3DPanel.addRobot();
        updateStatus("已添加新机器人，共 " + scene3DPanel.getRobotCount() + " 个");
    }

    private void removeLastRobot() {
        scene3DPanel.removeLastRobot();
        updateStatus("机器人数量: " + scene3DPanel.getRobotCount());
    }

    private void rotateHead() {
        scene3DPanel.getRobot().animateHead();
        updateStatus("头部旋转动画");
    }

    private void animateArms() {
        scene3DPanel.getRobot().animateArms();
        updateStatus("手臂动画");
    }

    private void animateLegs() {
        scene3DPanel.getRobot().animateLegs();
        updateStatus("腿部动画");
    }

    private void walkAnimation() {
        scene3DPanel.getRobot().walkAnimation();
        updateStatus("行走动画");
    }

    private void waveAnimation() {
        scene3DPanel.getRobot().waveAnimation();
        updateStatus("挥手动画");
    }

    // ==================== 帮助菜单处理 ====================

    private void clearAll() {
        canvas2DPanel.clearCanvas();
        scene3DPanel.clearAllRobots();
        updateStatus("已清空所有内容");
    }

    private void showProjectManual() {
        ProjectManualDialog dialog = new ProjectManualDialog(this);
        dialog.setVisible(true);
    }

    private void showUserGuide() {
        String guide = """
                操作指南:

                【2D/3D切换】
                - 光栅图形菜单 或 工具栏2D/3D按钮切换显示模式
                - 2D模式: 演示直线/圆/填充等算法
                - 3D模式: 显示机器人和舞台

                【鼠标操作 (3D模式)】
                - 左键拖拽: 旋转场景
                - 右键拖拽: 平移场景
                - 滚轮: 缩放场景

                【菜单功能】
                - 光栅图形: 2D图形绘制算法演示
                - 变换: 3D几何变换 (对机器人生效)
                - 裁剪: 直线裁剪演示 (2D画布)
                - 投影: 透视/平行投影切换
                - 可见面判定: 消隐算法
                - 光照明模型: 光照和材质
                - 机器人动画: 关节动画控制
                """;
        JOptionPane.showMessageDialog(this, guide, "操作指南", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        String about = """
                计算机图形学课程大作业

                技术栈: 纯Java2D/Swing (软件渲染)

                实现功能:
                1. 直线/圆的扫描转换 (DDA, Bresenham, 正负法)
                2. 多边形扫描与区域填充
                3. 几何变换 (平移/缩放/旋转/对称)
                4. 裁剪算法 (Cohen-Sutherland)
                5. 投影变换 (透视/正交)
                6. 可见面判定 (Z-buffer/后向面消除)
                7. 光照明模型 (Phong模型)
                8. 明暗处理 (Flat/Gouraud/Phong)
                9. 机器人动画 (关节层级)

                山东财经大学 - 计算机图形学
                """;
        JOptionPane.showMessageDialog(this, about, "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== 新增功能方法 ====================

    private void showTextDialog() {
        switchTo2D();
        new TextDialog(this, canvas2DPanel).setVisible(true);
    }

    private void showLineWidthDialog() {
        switchTo2D();
        String input = JOptionPane.showInputDialog(this, "请输入线宽 (1-10):", "2");
        if (input != null && !input.isEmpty()) {
            try {
                int width = Integer.parseInt(input);
                if (width < 1)
                    width = 1;
                if (width > 10)
                    width = 10;
                canvas2DPanel.setLineWidth(width);
                updateStatus("线宽已设置为: " + width);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "无效的输入");
            }
        }
    }

    private void saveCanvasImage() {
        if (!is2DMode) {
            JOptionPane.showMessageDialog(this, "请先切换到2D画布模式");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("保存画布图片");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getParent(), file.getName() + ".png");
            }
            try {
                canvas2DPanel.saveImage(file);
                JOptionPane.showMessageDialog(this, "保存成功!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage());
            }
        }
    }

    // ==================== 主函数 ====================

    /**
     * 应用程序入口点
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
