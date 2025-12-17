package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * CircleDrawingDialog.java - 圆绘制对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框让用户输入圆心坐标和半径，
 * 选择算法（Bresenham/正负法/多边形逼近），然后在2D画布上绘制圆。
 * 
 * 【支持的算法】
 * 1. Bresenham中点圆算法 - 利用圆的八路对称性，只计算1/8的圆弧
 * 2. 正负法 - 根据当前点在圆内还是圆外来决定下一步走向
 * 3. 多边形逼近法 - 用正多边形（如正36边形）来近似圆
 * 
 * @author Computer Graphics Course
 */
public class CircleDrawingDialog extends JDialog {

    // ==================== 属性 ====================

    /** 2D画布的引用 */
    private Canvas2DPanel canvas;

    /** 圆心坐标、半径、多边形边数的输入框 */
    private JTextField xcField, ycField, radiusField, sidesField;

    /** 算法选择下拉框 */
    private JComboBox<String> algorithmCombo;

    /** 颜色选择按钮 */
    private JButton colorButton;

    /** 当前选择的颜色 */
    private Color currentColor = Color.GREEN;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * 
     * @param parent 父窗口
     * @param canvas 2D画布
     */
    public CircleDrawingDialog(JFrame parent, Canvas2DPanel canvas) {
        super(parent, "圆绘制 - Circle Drawing", false);
        this.canvas = canvas;
        initUI();
    }

    // ==================== 界面初始化 ====================

    /**
     * 初始化用户界面
     */
    private void initUI() {
        setSize(380, 320);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ========== 第1行：算法选择 ==========
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("算法 Algorithm:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        algorithmCombo = new JComboBox<>(new String[] {
                "Bresenham (中点圆)", // 中点圆算法
                "正负法 (PN Method)", // 正负法
                "多边形逼近法" // 多边形逼近
        });
        // 当算法改变时，更新边数输入框的可用状态
        algorithmCombo.addActionListener(e -> updateSidesVisibility());
        mainPanel.add(algorithmCombo, gbc);
        gbc.gridwidth = 1;

        // ========== 第2行：圆心坐标 ==========
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("圆心 Center:"), gbc);
        gbc.gridx = 1;
        xcField = new JTextField("300", 6); // 默认圆心x=300
        mainPanel.add(xcField, gbc);
        gbc.gridx = 2;
        ycField = new JTextField("300", 6); // 默认圆心y=300
        mainPanel.add(ycField, gbc);

        // ========== 第3行：半径 ==========
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("半径 Radius:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        radiusField = new JTextField("100", 6); // 默认半径100
        mainPanel.add(radiusField, gbc);
        gbc.gridwidth = 1;

        // ========== 第4行：边数（仅多边形逼近法使用） ==========
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("边数 Sides:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        sidesField = new JTextField("36", 6); // 默认36边形
        sidesField.setEnabled(false); // 初始禁用，只有选择多边形逼近时才启用
        mainPanel.add(sidesField, gbc);
        gbc.gridwidth = 1;

        // ========== 第5行：颜色选择 ==========
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("颜色 Color:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        colorButton = new JButton();
        colorButton.setBackground(currentColor);
        colorButton.setPreferredSize(new Dimension(100, 25));
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择颜色", currentColor);
            if (c != null) {
                currentColor = c;
                colorButton.setBackground(c);
            }
        });
        mainPanel.add(colorButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ========== 底部按钮面板 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton drawButton = new JButton("绘制 Draw");
        drawButton.addActionListener(e -> drawCircle());
        buttonPanel.add(drawButton);

        JButton clearButton = new JButton("清除 Clear");
        clearButton.addActionListener(e -> canvas.clearCanvas());
        buttonPanel.add(clearButton);

        JButton closeButton = new JButton("关闭 Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 顶部说明 ==========
        JTextArea info = new JTextArea(
                "说明:\n" +
                        "• Bresenham: 中点圆算法，整数运算\n" +
                        "• 正负法: 根据函数正负值决定走向\n" +
                        "• 多边形逼近: 用正多边形逼近圆");
        info.setEditable(false);
        info.setBackground(getBackground());
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(info, BorderLayout.NORTH);
    }

    /**
     * 根据选择的算法更新边数输入框的可用状态
     * 只有选择"多边形逼近法"时，边数输入框才可用
     */
    private void updateSidesVisibility() {
        String algo = (String) algorithmCombo.getSelectedItem();
        sidesField.setEnabled(algo != null && algo.contains("多边形"));
    }

    // ==================== 绑图方法 ====================

    /**
     * 执行圆绘制
     */
    private void drawCircle() {
        try {
            // 解析圆心坐标和半径
            int xc = Integer.parseInt(xcField.getText().trim());
            int yc = Integer.parseInt(ycField.getText().trim());
            int r = Integer.parseInt(radiusField.getText().trim());

            String algorithm = (String) algorithmCombo.getSelectedItem();

            // 根据选择的算法调用相应的绘图方法
            if (algorithm.startsWith("Bresenham")) {
                // Bresenham中点圆算法
                canvas.drawCircleBresenham(xc, yc, r, currentColor);
            } else if (algorithm.startsWith("正负法")) {
                // 正负法画圆
                canvas.drawCirclePNMethod(xc, yc, r, currentColor);
            } else {
                // 多边形逼近法
                int sides = Integer.parseInt(sidesField.getText().trim());
                canvas.drawCirclePolygonApprox(xc, yc, r, sides, currentColor);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的整数", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
