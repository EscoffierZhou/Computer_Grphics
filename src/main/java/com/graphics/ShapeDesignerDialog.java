package com.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * ShapeDesignerDialog.java - 形状设计器对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这是一个可视化的3D形状设计工具，允许用户：
 * 1. 在2D画布上放置和排列形状
 * 2. 将2D设计转换为3D多边形并渲染到场景
 * 3. 保存和加载设计（JSON格式）
 * 
 * 【界面结构】
 * ┌─────────────────────────────────────────────────────────┐
 * │ 形状参数: [形状类型▼] [缩放] [旋转°] [颜色] │
 * ├────────────────────────────────────────────┬────────────┤
 * │ │ 已保存设计 │
 * │ 2D设计画布 │ - 设计1 │
 * │ (网格背景，可拖拽形状) │ - 设计2 │
 * │ │ │
 * │ │ [加载][删除] │
 * ├────────────────────────────────────────────┴────────────┤
 * │ [添加形状] [清空画布] [清空全部] [保存设计] [生成到场景] │
 * └─────────────────────────────────────────────────────────┘
 * 
 * 【2D到3D的坐标转换】
 * - 2D X → 3D X（水平方向，除以40）
 * - 2D Y → 3D -Y（垂直方向，翻转并除以40）
 * - 所有形状在Z=0平面
 * 
 * 【支持的形状类型】
 * - 立方体(Cube): 正方形的3D版本
 * - 长方体(Box): 竖长方形的3D版本
 * - 金字塔(Pyramid): 四棱锥
 * - 球体(Sphere): 用多边形近似
 * - 圆柱体(Cylinder): 用多边形近似
 * - 人形(Humanoid): 简笔人形
 * 
 * @author Computer Graphics Course
 */
public class ShapeDesignerDialog extends JDialog {

    // ==================== 核心组件 ====================

    /** 主3D场景引用 */
    private Scene3DPanel mainScene;

    /** 2D设计画布 */
    private DesignerCanvas designerCanvas;

    /** 形状类型选择下拉框 */
    private JComboBox<String> shapeTypeCombo;

    /** 缩放参数调节器 */
    private JSpinner scaleSpinner;

    /** 旋转角度调节器 */
    private JSpinner rotationSpinner;

    /** 颜色选择按钮 */
    private JButton colorButton;

    /** 当前选择的颜色 */
    private Color currentColor = new Color(100, 150, 200);

    /** 已保存设计列表 */
    private JList<String> savedDesignsList;

    /** 列表数据模型 */
    private DefaultListModel<String> savedDesignsModel;

    /** 设计保存目录 */
    private static final String DESIGNS_FILE = "robot_designs.json";

    // ==================== 构造函数 ====================

    public ShapeDesignerDialog(JFrame parent, Scene3DPanel scene) {
        super(parent, "形状设计器 - Shape Designer", false);
        this.mainScene = scene;
        initUI();
        loadSavedDesignsList();
    }

    /**
     * 初始化用户界面
     */
    private void initUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // ========== 顶部面板：形状选择和参数 ==========
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("形状参数"));

        // 形状类型选择
        topPanel.add(new JLabel("形状:"));
        shapeTypeCombo = new JComboBox<>(new String[] {
                "立方体 Cube", // 正方形
                "长方体 Box", // 竖长方形
                "金字塔 Pyramid",
                "球体 Sphere",
                "圆柱体 Cylinder",
                "人形 Humanoid"
        });
        topPanel.add(shapeTypeCombo);

        // 缩放参数
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("缩放:"));
        scaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 3.0, 0.1));
        topPanel.add(scaleSpinner);

        // 旋转角度
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("旋转°:"));
        rotationSpinner = new JSpinner(new SpinnerNumberModel(0, -180, 180, 15));
        topPanel.add(rotationSpinner);

        // 颜色选择
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("颜色:"));
        colorButton = new JButton("  ");
        colorButton.setBackground(currentColor);
        colorButton.setPreferredSize(new Dimension(40, 25));
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择颜色", currentColor);
            if (c != null) {
                currentColor = c;
                colorButton.setBackground(c);
            }
        });
        topPanel.add(colorButton);

        add(topPanel, BorderLayout.NORTH);

        // ========== 中央：2D设计画布 ==========
        designerCanvas = new DesignerCanvas();
        designerCanvas.setBorder(BorderFactory.createTitledBorder("2D设计区 - 点击放置，拖拽移动，滚轮旋转"));
        add(designerCanvas, BorderLayout.CENTER);

        // ========== 右侧：保存的设计列表 ==========
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("已保存设计"));
        rightPanel.setPreferredSize(new Dimension(160, 0));

        savedDesignsModel = new DefaultListModel<>();
        savedDesignsList = new JList<>(savedDesignsModel);
        savedDesignsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rightPanel.add(new JScrollPane(savedDesignsList), BorderLayout.CENTER);

        // 列表操作按钮
        JPanel listButtonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton loadDesignBtn = new JButton("加载设计");
        loadDesignBtn.addActionListener(e -> loadSelectedDesign());
        listButtonPanel.add(loadDesignBtn);

        JButton deleteDesignBtn = new JButton("删除设计");
        deleteDesignBtn.addActionListener(e -> deleteSelectedDesign());
        listButtonPanel.add(deleteDesignBtn);

        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.addActionListener(e -> loadSavedDesignsList());
        listButtonPanel.add(refreshBtn);

        rightPanel.add(listButtonPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        // ========== 底部：操作按钮 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton addShapeBtn = new JButton("添加形状");
        addShapeBtn.addActionListener(e -> designerCanvas.addShapeAtCenter());
        buttonPanel.add(addShapeBtn);

        JButton clearCanvasBtn = new JButton("清空画布");
        clearCanvasBtn.addActionListener(e -> designerCanvas.clearShapes());
        buttonPanel.add(clearCanvasBtn);

        JButton clearAllBtn = new JButton("清空全部(含3D)");
        clearAllBtn.addActionListener(e -> clearAll());
        buttonPanel.add(clearAllBtn);

        buttonPanel.add(Box.createHorizontalStrut(20));

        JButton saveDesignBtn = new JButton("保存设计");
        saveDesignBtn.addActionListener(e -> saveCurrentDesign());
        buttonPanel.add(saveDesignBtn);

        JButton generateBtn = new JButton("生成到场景");
        generateBtn.addActionListener(e -> generateToScene());
        buttonPanel.add(generateBtn);

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 清空全部（画布和3D场景）
     */
    private void clearAll() {
        designerCanvas.clearShapes();
        mainScene.clearAllRobots();
        mainScene.addRobot(); // 保留一个默认机器人
        mainScene.repaint();
        JOptionPane.showMessageDialog(this, "已清空画布和3D场景");
    }

    // ==================== 核心功能：生成到场景 ====================

    /**
     * 将2D设计转换为3D并添加到场景
     * 
     * 【坐标转换说明】
     * 2D画布坐标 (像素) → 3D世界坐标 (单位)
     * 
     * 转换公式:
     * x3d = (shape.x - canvasCenterX) / 40.0
     * y3d = -(shape.y - canvasCenterY) / 40.0 (Y轴翻转)
     * z3d = 0 (所有形状在同一平面)
     */
    private void generateToScene() {
        List<DesignerShape> shapes = designerCanvas.getShapes();

        if (shapes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "画布为空，请先添加形状", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 获取画布中心用于坐标转换
        int canvasCenterX = designerCanvas.getWidth() / 2;
        int canvasCenterY = designerCanvas.getHeight() / 2;

        // 清除场景中的现有机器人
        mainScene.clearAllRobots();

        // 创建自定义多边形列表
        List<Polygon3D> customParts = new ArrayList<>();

        // 遍历每个2D形状，转换为3D
        for (DesignerShape shape : shapes) {
            // 坐标转换
            double x3d = (shape.x - canvasCenterX) / 40.0;
            double y3d = -(shape.y - canvasCenterY) / 40.0; // Y轴翻转
            double z3d = 0;

            double scale = shape.scale;
            Color color = shape.color;
            String type = shape.type;

            // 根据形状类型创建对应的3D多边形
            if (type.contains("立方体") || type.contains("Cube")) {
                customParts.addAll(createBox(x3d, y3d, z3d, scale, scale, scale, color));
            } else if (type.contains("长方体") || type.contains("Box")) {
                customParts.addAll(createBox(x3d, y3d, z3d, scale * 0.6, scale * 1.5, scale * 0.6, color));
            } else if (type.contains("金字塔") || type.contains("Pyramid")) {
                customParts.addAll(createPyramid(x3d, y3d, z3d, scale, color));
            } else if (type.contains("球") || type.contains("Sphere")) {
                customParts.addAll(createSphere(x3d, y3d, z3d, scale * 0.5, 12, color));
            } else if (type.contains("圆柱") || type.contains("Cylinder")) {
                customParts.addAll(createCylinder(x3d, y3d, z3d, scale * 0.4, scale, 12, color));
            } else {
                customParts.addAll(createBox(x3d, y3d, z3d, scale, scale, scale, color));
            }
        }

        // 创建自定义机器人并添加到场景
        Robot customRobot = new Robot(customParts);
        customRobot.setPosition(0, 0, 0);

        mainScene.addRobot(customRobot);
        mainScene.repaint();

        JOptionPane.showMessageDialog(this,
                "已生成自定义机器人\n" +
                        "现在可以使用变换工具(平移/旋转/缩放)操作它了！");
    }

    // ==================== 3D形状创建辅助方法 ====================

    /**
     * 创建长方体（6个面）
     * 
     * 【顶点示意图】
     * 7-------6
     * /| /|
     * 3-------2 |
     * | 4-----|-5
     * |/ |/
     * 0-------1
     * 
     * @param cx    中心X
     * @param cy    中心Y
     * @param cz    中心Z
     * @param w     宽度(X方向)
     * @param h     高度(Y方向)
     * @param d     深度(Z方向)
     * @param color 颜色
     */
    private List<Polygon3D> createBox(double cx, double cy, double cz,
            double w, double h, double d, Color color) {
        List<Polygon3D> polys = new ArrayList<>();
        double hw = w / 2, hh = h / 2, hd = d / 2;

        // 前面 (z+)
        polys.add(new Polygon3D(new double[][] {
                { cx - hw, cy - hh, cz + hd }, { cx + hw, cy - hh, cz + hd },
                { cx + hw, cy + hh, cz + hd }, { cx - hw, cy + hh, cz + hd }
        }, color));
        // 后面 (z-)
        polys.add(new Polygon3D(new double[][] {
                { cx + hw, cy - hh, cz - hd }, { cx - hw, cy - hh, cz - hd },
                { cx - hw, cy + hh, cz - hd }, { cx + hw, cy + hh, cz - hd }
        }, color.darker()));
        // 左面 (x-)
        polys.add(new Polygon3D(new double[][] {
                { cx - hw, cy - hh, cz - hd }, { cx - hw, cy - hh, cz + hd },
                { cx - hw, cy + hh, cz + hd }, { cx - hw, cy + hh, cz - hd }
        }, color.darker()));
        // 右面 (x+)
        polys.add(new Polygon3D(new double[][] {
                { cx + hw, cy - hh, cz + hd }, { cx + hw, cy - hh, cz - hd },
                { cx + hw, cy + hh, cz - hd }, { cx + hw, cy + hh, cz + hd }
        }, color.darker()));
        // 上面 (y+)
        polys.add(new Polygon3D(new double[][] {
                { cx - hw, cy + hh, cz + hd }, { cx + hw, cy + hh, cz + hd },
                { cx + hw, cy + hh, cz - hd }, { cx - hw, cy + hh, cz - hd }
        }, color.brighter()));
        // 下面 (y-)
        polys.add(new Polygon3D(new double[][] {
                { cx - hw, cy - hh, cz - hd }, { cx + hw, cy - hh, cz - hd },
                { cx + hw, cy - hh, cz + hd }, { cx - hw, cy - hh, cz + hd }
        }, color.darker().darker()));

        return polys;
    }

    /**
     * 创建金字塔（1个底面 + 4个三角形侧面）
     */
    private List<Polygon3D> createPyramid(double cx, double cy, double cz,
            double size, Color color) {
        List<Polygon3D> polys = new ArrayList<>();
        double hs = size / 2;
        double top = cy + size; // 顶点Y坐标

        // 底面（正方形）
        polys.add(new Polygon3D(new double[][] {
                { cx - hs, cy, cz - hs }, { cx + hs, cy, cz - hs },
                { cx + hs, cy, cz + hs }, { cx - hs, cy, cz + hs }
        }, color.darker()));
        // 4个侧面（三角形）
        polys.add(new Polygon3D(new double[][] {
                { cx - hs, cy, cz + hs }, { cx + hs, cy, cz + hs }, { cx, top, cz }
        }, color));
        polys.add(new Polygon3D(new double[][] {
                { cx + hs, cy, cz + hs }, { cx + hs, cy, cz - hs }, { cx, top, cz }
        }, color.darker()));
        polys.add(new Polygon3D(new double[][] {
                { cx + hs, cy, cz - hs }, { cx - hs, cy, cz - hs }, { cx, top, cz }
        }, color));
        polys.add(new Polygon3D(new double[][] {
                { cx - hs, cy, cz - hs }, { cx - hs, cy, cz + hs }, { cx, top, cz }
        }, color.darker()));

        return polys;
    }

    /**
     * 创建球体（使用经纬线多边形近似）
     * 
     * 【参数化表面】
     * x = r * sin(θ) * cos(φ)
     * y = r * cos(θ)
     * z = r * sin(θ) * sin(φ)
     * 
     * θ ∈ [0, π] (纬度)
     * φ ∈ [0, 2π] (经度)
     */
    private List<Polygon3D> createSphere(double cx, double cy, double cz,
            double r, int seg, Color color) {
        List<Polygon3D> polys = new ArrayList<>();
        int latB = seg / 2, lonB = seg;

        for (int lat = 0; lat < latB; lat++) {
            double t1 = lat * Math.PI / latB, t2 = (lat + 1) * Math.PI / latB;
            for (int lon = 0; lon < lonB; lon++) {
                double p1 = lon * 2 * Math.PI / lonB, p2 = (lon + 1) * 2 * Math.PI / lonB;
                // 4个顶点
                double x1 = cx + r * Math.sin(t1) * Math.cos(p1), y1 = cy + r * Math.cos(t1),
                        z1 = cz + r * Math.sin(t1) * Math.sin(p1);
                double x2 = cx + r * Math.sin(t1) * Math.cos(p2), y2 = cy + r * Math.cos(t1),
                        z2 = cz + r * Math.sin(t1) * Math.sin(p2);
                double x3 = cx + r * Math.sin(t2) * Math.cos(p2), y3 = cy + r * Math.cos(t2),
                        z3 = cz + r * Math.sin(t2) * Math.sin(p2);
                double x4 = cx + r * Math.sin(t2) * Math.cos(p1), y4 = cy + r * Math.cos(t2),
                        z4 = cz + r * Math.sin(t2) * Math.sin(p1);
                // 上半球略亮
                polys.add(
                        new Polygon3D(new double[][] { { x1, y1, z1 }, { x2, y2, z2 }, { x3, y3, z3 }, { x4, y4, z4 } },
                                lat < latB / 2 ? color.brighter() : color));
            }
        }
        return polys;
    }

    /**
     * 创建圆柱体（侧面 + 上下盖）
     * 
     * 【参数化】
     * x = r * cos(θ)
     * z = r * sin(θ)
     * y ∈ [-h/2, h/2]
     */
    private List<Polygon3D> createCylinder(double cx, double cy, double cz,
            double r, double h, int seg, Color color) {
        List<Polygon3D> polys = new ArrayList<>();
        double hh = h / 2;
        for (int i = 0; i < seg; i++) {
            double a1 = 2 * Math.PI * i / seg, a2 = 2 * Math.PI * (i + 1) / seg;
            double x1 = cx + r * Math.cos(a1), z1 = cz + r * Math.sin(a1);
            double x2 = cx + r * Math.cos(a2), z2 = cz + r * Math.sin(a2);
            // 侧面
            polys.add(new Polygon3D(new double[][] {
                    { x1, cy - hh, z1 }, { x2, cy - hh, z2 }, { x2, cy + hh, z2 }, { x1, cy + hh, z1 }
            }, color));
            // 上盖
            polys.add(new Polygon3D(new double[][] { { x1, cy + hh, z1 }, { x2, cy + hh, z2 }, { cx, cy + hh, cz } },
                    color.brighter()));
            // 下盖
            polys.add(new Polygon3D(new double[][] { { x1, cy - hh, z1 }, { x2, cy - hh, z2 }, { cx, cy - hh, cz } },
                    color.darker()));
        }
        return polys;
    }

    /**
     * 将颜色转换为十六进制字符串
     */
    private String colorToHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ==================== 设计保存/加载功能 ====================

    /**
     * 保存当前设计到JSON文件
     */
    private void saveCurrentDesign() {
        List<DesignerShape> shapes = designerCanvas.getShapes();
        if (shapes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "画布为空，请先添加形状", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = JOptionPane.showInputDialog(this, "请输入设计名称:", "保存设计", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty())
            return;

        // 清理文件名
        name = name.trim().replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "_");

        try {
            // 创建designs目录
            File dir = new File("designs");
            if (!dir.exists())
                dir.mkdirs();

            // 写入JSON文件
            File file = new File(dir, name + ".json");
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("{");
                writer.println("  \"name\": \"" + name + "\",");
                writer.println("  \"shapes\": [");
                for (int i = 0; i < shapes.size(); i++) {
                    DesignerShape s = shapes.get(i);
                    writer.print("    {\"type\":\"" + s.type + "\", \"x\":" + s.x + ", \"y\":" + s.y +
                            ", \"rotation\":" + s.rotation + ", \"scale\":" + s.scale +
                            ", \"color\":" + s.color.getRGB() + "}");
                    if (i < shapes.size() - 1)
                        writer.println(",");
                    else
                        writer.println();
                }
                writer.println("  ]");
                writer.println("}");
            }

            JOptionPane.showMessageDialog(this, "设计已保存: " + name);
            loadSavedDesignsList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载已保存设计列表
     */
    private void loadSavedDesignsList() {
        savedDesignsModel.clear();
        File dir = new File("designs");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    savedDesignsModel.addElement(f.getName().replace(".json", ""));
                }
            }
        }
    }

    /**
     * 加载选中的设计
     */
    private void loadSelectedDesign() {
        String selected = savedDesignsList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个设计", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            File file = new File("designs", selected + ".json");
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8");

            designerCanvas.clearShapes();

            // 简单解析JSON
            int shapesStart = content.indexOf("[");
            int shapesEnd = content.lastIndexOf("]");
            if (shapesStart > 0 && shapesEnd > shapesStart) {
                String shapesStr = content.substring(shapesStart + 1, shapesEnd);
                String[] shapeStrs = shapesStr.split("\\},\\s*\\{");

                for (String shapeStr : shapeStrs) {
                    shapeStr = shapeStr.replace("{", "").replace("}", "");
                    String type = extractValue(shapeStr, "type");
                    int x = Integer.parseInt(extractValue(shapeStr, "x"));
                    int y = Integer.parseInt(extractValue(shapeStr, "y"));
                    int rotation = Integer.parseInt(extractValue(shapeStr, "rotation"));
                    double scale = Double.parseDouble(extractValue(shapeStr, "scale"));
                    int colorRgb = Integer.parseInt(extractValue(shapeStr, "color"));

                    DesignerShape shape = new DesignerShape(type, x, y, new Color(colorRgb), scale);
                    shape.rotation = rotation;
                    designerCanvas.shapes.add(shape);
                }
            }

            designerCanvas.repaint();
            JOptionPane.showMessageDialog(this, "已加载设计: " + selected);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 从JSON字符串中提取指定键的值
     */
    private String extractValue(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0)
            return "";
        int colonIdx = json.indexOf(":", idx);
        if (colonIdx < 0)
            return "";
        int start = colonIdx + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"'))
            start++;
        int end = start;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != '"')
            end++;
        return json.substring(start, end).trim();
    }

    /**
     * 删除选中的设计
     */
    private void deleteSelectedDesign() {
        String selected = savedDesignsList.getSelectedValue();
        if (selected == null)
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定删除设计 \"" + selected + "\"?", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            File file = new File("designs", selected + ".json");
            if (file.delete()) {
                loadSavedDesignsList();
                JOptionPane.showMessageDialog(this, "已删除");
            }
        }
    }

    // ==================== 内部类：2D设计画布 ====================

    /**
     * 2D设计画布
     * 
     * 【交互方式】
     * - 点击空白处: 添加新形状
     * - 点击形状: 选中形状
     * - 拖拽形状: 移动形状
     * - 滚轮: 旋转选中的形状
     */
    private class DesignerCanvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

        /** 画布上的所有形状 */
        List<DesignerShape> shapes = new ArrayList<>();

        /** 当前选中的形状 */
        private DesignerShape selectedShape = null;

        /** 当前正在拖拽的形状 */
        private DesignerShape draggedShape = null;

        /** 拖拽偏移量 */
        private int dragOffsetX, dragOffsetY;

        public DesignerCanvas() {
            setBackground(new Color(35, 35, 45));
            setPreferredSize(new Dimension(500, 400));
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        /**
         * 在画布中心添加形状
         */
        public void addShapeAtCenter() {
            String type = (String) shapeTypeCombo.getSelectedItem();
            double scale = (Double) scaleSpinner.getValue();
            int rotation = (Integer) rotationSpinner.getValue();

            DesignerShape shape = new DesignerShape(type, getWidth() / 2, getHeight() / 2, currentColor, scale);
            shape.rotation = rotation;
            shapes.add(shape);
            selectedShape = shape;
            repaint();
        }

        /**
         * 清空所有形状
         */
        public void clearShapes() {
            shapes.clear();
            selectedShape = null;
            repaint();
        }

        /**
         * 获取所有形状的副本
         */
        public List<DesignerShape> getShapes() {
            return new ArrayList<>(shapes);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制网格
            g2d.setColor(new Color(55, 55, 65));
            int gridSize = 40;
            for (int x = 0; x < getWidth(); x += gridSize) {
                g2d.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += gridSize) {
                g2d.drawLine(0, y, getWidth(), y);
            }

            // 绘制中心十字线
            g2d.setColor(new Color(80, 80, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            g2d.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);

            // 绘制所有形状
            for (DesignerShape shape : shapes) {
                shape.draw(g2d, shape == selectedShape);
            }

            // 提示信息
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2d.drawString("点击添加 | 拖拽移动 | 滚轮旋转选中形状", 10, 20);
            g2d.drawString("形状数: " + shapes.size(), 10, getHeight() - 10);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // 检查是否点击了现有形状
            for (int i = shapes.size() - 1; i >= 0; i--) {
                DesignerShape shape = shapes.get(i);
                if (shape.contains(e.getX(), e.getY())) {
                    selectedShape = shape;
                    draggedShape = shape;
                    dragOffsetX = e.getX() - shape.x;
                    dragOffsetY = e.getY() - shape.y;
                    repaint();
                    return;
                }
            }

            // 点击空白处添加新形状
            String type = (String) shapeTypeCombo.getSelectedItem();
            double scale = (Double) scaleSpinner.getValue();
            int rotation = (Integer) rotationSpinner.getValue();
            DesignerShape shape = new DesignerShape(type, e.getX(), e.getY(), currentColor, scale);
            shape.rotation = rotation;
            shapes.add(shape);
            selectedShape = shape;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            draggedShape = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggedShape != null) {
                draggedShape.x = e.getX() - dragOffsetX;
                draggedShape.y = e.getY() - dragOffsetY;
                repaint();
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (selectedShape != null) {
                selectedShape.rotation += e.getWheelRotation() * 15;
                // 保持角度在[-180, 180]范围
                if (selectedShape.rotation > 180)
                    selectedShape.rotation -= 360;
                if (selectedShape.rotation < -180)
                    selectedShape.rotation += 360;
                repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    }

    // ==================== 内部类：设计器形状 ====================

    /**
     * 设计器形状
     * 表示2D画布上的一个可编辑形状
     */
    private static class DesignerShape {
        /** 形状类型 */
        String type;

        /** 中心位置 */
        int x, y;

        /** 显示尺寸 */
        int width, height;

        /** 颜色 */
        Color color;

        /** 缩放比例 */
        double scale;

        /** 旋转角度（度） */
        int rotation = 0;

        /**
         * 创建形状
         */
        DesignerShape(String type, int x, int y, Color color, double scale) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.color = color;
            this.scale = scale;

            // 根据类型设置不同的宽高
            if (type.contains("立方体") || type.contains("Cube")) {
                this.width = (int) (50 * scale);
                this.height = (int) (50 * scale);
            } else if (type.contains("长方体") || type.contains("Box")) {
                this.width = (int) (35 * scale);
                this.height = (int) (70 * scale);
            } else if (type.contains("人形") || type.contains("Humanoid")) {
                this.width = (int) (40 * scale);
                this.height = (int) (80 * scale);
            } else {
                this.width = (int) (50 * scale);
                this.height = (int) (50 * scale);
            }
        }

        /**
         * 检查点是否在形状内
         */
        boolean contains(int px, int py) {
            return px >= x - width / 2 - 10 && px <= x + width / 2 + 10 &&
                    py >= y - height / 2 - 10 && py <= y + height / 2 + 10;
        }

        /**
         * 绘制形状
         * 
         * @param g        Graphics2D对象
         * @param selected 是否被选中
         */
        void draw(Graphics2D g, boolean selected) {
            Graphics2D g2 = (Graphics2D) g.create();

            // 应用旋转变换
            g2.rotate(Math.toRadians(rotation), x, y);

            int w = width;
            int h = height;

            // 根据类型绘制不同的形状
            if (type.contains("人形") || type.contains("Humanoid")) {
                // 简笔人形
                g2.setColor(color);
                g2.fillOval(x - 10, y - h / 2, 20, 20); // 头
                g2.fillRect(x - 8, y - h / 2 + 20, 16, 25); // 身体
                g2.fillRect(x - 8, y - h / 2 + 45, 6, 20); // 左腿
                g2.fillRect(x + 2, y - h / 2 + 45, 6, 20); // 右腿
                g2.fillRect(x - 18, y - h / 2 + 22, 10, 5); // 左臂
                g2.fillRect(x + 8, y - h / 2 + 22, 10, 5); // 右臂
            } else if (type.contains("金字塔") || type.contains("Pyramid")) {
                g2.setColor(color);
                int[] xp = { x, x - w / 2, x + w / 2 };
                int[] yp = { y - h / 2, y + h / 2, y + h / 2 };
                g2.fillPolygon(xp, yp, 3);
            } else if (type.contains("球") || type.contains("Sphere")) {
                g2.setColor(color);
                g2.fillOval(x - w / 2, y - h / 2, w, h);
                g2.setColor(color.brighter());
                g2.fillOval(x - w / 4, y - h / 4, w / 4, h / 4); // 高光
            } else if (type.contains("圆柱") || type.contains("Cylinder")) {
                g2.setColor(color);
                g2.fillRect(x - w / 2, y - h / 2 + 10, w, h - 20);
                g2.setColor(color.brighter());
                g2.fillOval(x - w / 2, y - h / 2, w, 20); // 上盖
                g2.setColor(color.darker());
                g2.fillOval(x - w / 2, y + h / 2 - 20, w, 20); // 下盖
            } else if (type.contains("长方体") || type.contains("Box")) {
                // 长方体 - 带3D效果
                g2.setColor(color);
                g2.fillRect(x - w / 2, y - h / 2, w, h);
                g2.setColor(color.brighter());
                g2.fillRect(x - w / 2 + 3, y - h / 2 - 8, w - 3, 8); // 顶面
                g2.setColor(color.darker());
                g2.fillRect(x + w / 2, y - h / 2 - 5, 8, h); // 侧面
            } else {
                // 立方体 - 带3D效果
                g2.setColor(color);
                g2.fillRect(x - w / 2, y - h / 2, w, h);
                g2.setColor(color.brighter());
                g2.fillRect(x - w / 2 + 5, y - h / 2 - 10, w - 5, 10);
                g2.setColor(color.darker());
                g2.fillRect(x + w / 2, y - h / 2 - 5, 10, h);
            }

            // 边框（选中时高亮）
            g2.setColor(selected ? Color.YELLOW : Color.WHITE);
            g2.setStroke(new BasicStroke(selected ? 2.5f : 1f));
            g2.drawRect(x - w / 2, y - h / 2, w, h);

            g2.dispose();

            // 标签和坐标（不旋转）
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String label = type.split(" ")[0];
            g.drawString(label, x - g.getFontMetrics().stringWidth(label) / 2, y + h / 2 + 15);

            g.setColor(Color.YELLOW);
            g.setFont(new Font("Monospaced", Font.PLAIN, 9));
            g.drawString(String.format("(%d,%d) %d°", x, y, rotation), x - w / 2, y - h / 2 - 5);
        }
    }
}
