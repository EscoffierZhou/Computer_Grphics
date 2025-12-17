package com.graphics;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * PolygonScanDialog.java - 多边形扫描填充对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于演示多边形扫描线填充算法。
 * 与区域填充不同，扫描线算法不需要种子点，
 * 它直接根据多边形的顶点坐标计算填充区域。
 * 
 * 【支持的算法】
 * 1. 扫描线算法(ET/AEL) - 使用边表和活动边表
 * 2. 边缘填充算法 - 对每条边右侧的像素取反
 * 3. 边界标志算法 - 先画边界，再逐行填充
 * 
 * 【预设形状】
 * 提供多种预设形状方便测试：
 * 三角形、正方形、五边形、六边形、星形、箭头、心形
 * 
 * 【扫描线算法原理】
 * 1. 从多边形的最低点到最高点逐行扫描
 * 2. 对每条扫描线，找出与多边形边的交点
 * 3. 将交点排序，然后两两配对填充中间的像素
 * 
 * @author Computer Graphics Course
 */
public class PolygonScanDialog extends JDialog {

    // ==================== 属性 ====================

    /** 2D画布引用 */
    private Canvas2DPanel canvas;

    /** 算法选择下拉框 */
    private JComboBox<String> algorithmCombo;

    /** 预设形状下拉框 */
    private JComboBox<String> presetCombo;

    /** 填充颜色和边框颜色按钮 */
    private JButton fillColorButton, lineColorButton;

    /** 填充颜色（默认青色） */
    private Color fillColor = Color.CYAN;

    /** 边框颜色（默认白色） */
    private Color lineColor = Color.WHITE;

    /** 顶点坐标输入区域（多行文本框） */
    private JTextArea verticesArea;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     */
    public PolygonScanDialog(JFrame parent, Canvas2DPanel canvas) {
        super(parent, "多边形扫描填充 - Polygon Scan", false);
        this.canvas = canvas;
        initUI();
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        setSize(480, 480);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== 上部控制面板 ==========
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 预设形状选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(new JLabel("预设形状:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        presetCombo = new JComboBox<>(new String[] {
                "自定义",
                "三角形 Triangle",
                "正方形 Square",
                "五边形 Pentagon",
                "六边形 Hexagon",
                "星形 Star",
                "箭头 Arrow",
                "心形 Heart"
        });
        // 选择预设时自动加载顶点坐标
        presetCombo.addActionListener(e -> loadPreset());
        controlPanel.add(presetCombo, gbc);
        gbc.gridwidth = 1;

        // 算法选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("算法:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        algorithmCombo = new JComboBox<>(new String[] {
                "扫描线算法 (ET/AEL)", // 边表/活动边表方法
                "边缘填充算法", // 边缘取反方法
                "边界标志算法" // 先画边界再填充
        });
        controlPanel.add(algorithmCombo, gbc);
        gbc.gridwidth = 1;

        // 填充颜色
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(new JLabel("填充色:"), gbc);
        gbc.gridx = 1;
        fillColorButton = new JButton();
        fillColorButton.setBackground(fillColor);
        fillColorButton.setPreferredSize(new Dimension(60, 25));
        fillColorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择填充颜色", fillColor);
            if (c != null) {
                fillColor = c;
                fillColorButton.setBackground(c);
            }
        });
        controlPanel.add(fillColorButton, gbc);

        // 边框颜色
        gbc.gridx = 0;
        gbc.gridy = 3;
        controlPanel.add(new JLabel("边框色:"), gbc);
        gbc.gridx = 1;
        lineColorButton = new JButton();
        lineColorButton.setBackground(lineColor);
        lineColorButton.setPreferredSize(new Dimension(60, 25));
        lineColorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择边框颜色", lineColor);
            if (c != null) {
                lineColor = c;
                lineColorButton.setBackground(c);
            }
        });
        controlPanel.add(lineColorButton, gbc);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        // ========== 顶点输入区 ==========
        JPanel verticesPanel = new JPanel(new BorderLayout(5, 5));
        verticesPanel.add(new JLabel("顶点坐标 (每行一个: x,y):"), BorderLayout.NORTH);

        // 默认显示一个五边形的顶点
        verticesArea = new JTextArea("100,100\n300,80\n350,200\n280,350\n120,300");
        verticesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(verticesArea);
        verticesPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(verticesPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // ========== 底部按钮 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton drawButton = new JButton("填充 Fill");
        drawButton.addActionListener(e -> fillPolygon());
        buttonPanel.add(drawButton);

        JButton outlineButton = new JButton("描边 Outline");
        outlineButton.addActionListener(e -> drawOutline());
        buttonPanel.add(outlineButton);

        JButton clearButton = new JButton("清除 Clear");
        clearButton.addActionListener(e -> canvas.clearCanvas());
        buttonPanel.add(clearButton);

        JButton closeButton = new JButton("关闭 Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 顶部说明
        JTextArea info = new JTextArea("说明: 选择预设形状或手动输入顶点坐标");
        info.setEditable(false);
        info.setBackground(getBackground());
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(info, BorderLayout.NORTH);
    }

    // ==================== 预设形状加载 ====================

    /**
     * 根据选择的预设形状，自动生成顶点坐标
     * 使用三角函数计算正多边形或特殊形状的顶点
     */
    private void loadPreset() {
        String preset = (String) presetCombo.getSelectedItem();
        if (preset == null || preset.equals("自定义"))
            return;

        int cx = 300, cy = 250; // 中心点坐标
        int r = 100; // 半径

        String vertices = "";

        if (preset.contains("三角形")) {
            // 正三角形：3个顶点，相隔120度
            vertices = String.format("%d,%d\n%d,%d\n%d,%d",
                    cx, cy - r, // 顶点
                    cx - (int) (r * 0.866), cy + r / 2, // 左下
                    cx + (int) (r * 0.866), cy + r / 2); // 右下
        } else if (preset.contains("正方形")) {
            // 正方形：4个顶点
            vertices = String.format("%d,%d\n%d,%d\n%d,%d\n%d,%d",
                    cx - r, cy - r, // 左上
                    cx + r, cy - r, // 右上
                    cx + r, cy + r, // 右下
                    cx - r, cy + r); // 左下
        } else if (preset.contains("五边形")) {
            // 正五边形：5个顶点，使用参数方程
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                double angle = Math.PI / 2 + i * 2 * Math.PI / 5;
                int x = cx + (int) (r * Math.cos(angle));
                int y = cy - (int) (r * Math.sin(angle));
                if (i > 0)
                    sb.append("\n");
                sb.append(x).append(",").append(y);
            }
            vertices = sb.toString();
        } else if (preset.contains("六边形")) {
            // 正六边形：6个顶点
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3;
                int x = cx + (int) (r * Math.cos(angle));
                int y = cy + (int) (r * Math.sin(angle));
                if (i > 0)
                    sb.append("\n");
                sb.append(x).append(",").append(y);
            }
            vertices = sb.toString();
        } else if (preset.contains("星形")) {
            // 五角星：10个顶点，外圈和内圈交替
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                double angle = Math.PI / 2 + i * Math.PI / 5;
                int radius = (i % 2 == 0) ? r : r / 2; // 交替使用大小半径
                int x = cx + (int) (radius * Math.cos(angle));
                int y = cy - (int) (radius * Math.sin(angle));
                if (i > 0)
                    sb.append("\n");
                sb.append(x).append(",").append(y);
            }
            vertices = sb.toString();
        } else if (preset.contains("箭头")) {
            // 箭头形状：7个顶点
            vertices = String.format("%d,%d\n%d,%d\n%d,%d\n%d,%d\n%d,%d\n%d,%d\n%d,%d",
                    cx, cy - r, // 顶点
                    cx + r, cy, // 右翼
                    cx + r / 3, cy, // 右内
                    cx + r / 3, cy + r, // 右下
                    cx - r / 3, cy + r, // 左下
                    cx - r / 3, cy, // 左内
                    cx - r, cy); // 左翼
        } else if (preset.contains("心形")) {
            // 简化的心形：6个顶点
            vertices = String.format("%d,%d\n%d,%d\n%d,%d\n%d,%d\n%d,%d",
                    cx, cy - r / 3,
                    cx + r, cy - r / 2,
                    cx + r / 2, cy + r,
                    cx, cy + r / 2,
                    cx - r / 2, cy + r);
            vertices += String.format("\n%d,%d", cx - r, cy - r / 2);
        }

        verticesArea.setText(vertices);
    }

    // ==================== 顶点解析 ====================

    /**
     * 将文本区域中的顶点坐标解析为Point列表
     * 每行一个顶点，格式为 "x,y" 或 "x y"
     * 
     * @return 顶点列表
     */
    private List<Point> parseVertices() {
        List<Point> vertices = new ArrayList<>();
        String[] lines = verticesArea.getText().split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            // 支持逗号或空格分隔
            String[] parts = line.split("[,\\s]+");
            if (parts.length >= 2) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    vertices.add(new Point(x, y));
                } catch (NumberFormatException e) {
                    // 忽略无效行
                }
            }
        }
        return vertices;
    }

    // ==================== 绑图方法 ====================

    /**
     * 执行多边形填充
     */
    private void fillPolygon() {
        List<Point> vertices = parseVertices();
        if (vertices.size() < 3) {
            JOptionPane.showMessageDialog(this, "需要至少3个顶点", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 先画边框
        drawOutline();

        // 使用扫描线算法填充
        canvas.scanLineFill(vertices, fillColor);
    }

    /**
     * 只绘制多边形边框（不填充）
     * 使用Bresenham直线算法连接相邻顶点
     */
    private void drawOutline() {
        List<Point> vertices = parseVertices();
        if (vertices.size() < 2)
            return;

        // 绘制每条边
        for (int i = 0; i < vertices.size(); i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % vertices.size()); // 最后一个顶点连回第一个
            canvas.drawLineBresenham(p1.x, p1.y, p2.x, p2.y, lineColor);
        }

        // 显示顶点坐标标签
        canvas.showVertexCoordinates(vertices);
    }
}
