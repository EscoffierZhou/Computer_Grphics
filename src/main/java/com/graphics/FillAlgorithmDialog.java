package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * FillAlgorithmDialog.java - 区域填充算法对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于演示区域填充算法。
 * 用户需要先在画布上绘制一个封闭区域的边界，
 * 然后指定一个种子点（区域内部的任意一点），
 * 算法会从种子点开始向四周扩散，填充整个区域。
 * 
 * 【支持的算法】
 * 1. 种子填充(4连通) - 只向上下左右四个方向扩展
 * 2. 种子填充(8连通) - 向八个方向扩展（包括对角线）
 * 3. 扫描线种子填充 - 优化版本，按扫描线批量填充
 * 
 * 【使用步骤】
 * 1. 先在画布上用直线或圆绘制一个封闭边界
 * 2. 输入种子点坐标（边界内部的一点）
 * 3. 选择填充颜色和边界颜色
 * 4. 点击"填充"按钮
 * 
 * @author Computer Graphics Course
 */
public class FillAlgorithmDialog extends JDialog {

    // ==================== 属性 ====================

    /** 2D画布的引用 */
    private Canvas2DPanel canvas;

    /** 算法选择下拉框 */
    private JComboBox<String> algorithmCombo;

    /** 填充颜色和边界颜色的选择按钮 */
    private JButton fillColorButton, boundaryColorButton;

    /** 填充颜色（默认紫色） */
    private Color fillColor = Color.MAGENTA;

    /** 边界颜色（默认白色，需要与画布上绑制的边界颜色一致） */
    private Color boundaryColor = Color.WHITE;

    /** 种子点坐标输入框 */
    private JTextField seedXField, seedYField;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * 
     * @param parent 父窗口
     * @param canvas 2D画布
     */
    public FillAlgorithmDialog(JFrame parent, Canvas2DPanel canvas) {
        super(parent, "区域填充 - Fill Algorithm", false);
        this.canvas = canvas;
        initUI();
    }

    // ==================== 界面初始化 ====================

    /**
     * 初始化用户界面
     */
    private void initUI() {
        setSize(380, 300);
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
        mainPanel.add(new JLabel("算法:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        algorithmCombo = new JComboBox<>(new String[] {
                "种子填充 (4连通)", // 只向上下左右扩展
                "种子填充 (8连通)", // 向8个方向扩展
                "扫描线种子填充" // 优化的扫描线版本
        });
        mainPanel.add(algorithmCombo, gbc);
        gbc.gridwidth = 1;

        // ========== 第2行：种子点坐标 ==========
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("种子点:"), gbc);
        gbc.gridx = 1;
        seedXField = new JTextField("200", 6); // 默认x=200
        mainPanel.add(seedXField, gbc);
        gbc.gridx = 2;
        seedYField = new JTextField("200", 6); // 默认y=200
        mainPanel.add(seedYField, gbc);

        // ========== 第3行：填充颜色 ==========
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("填充色:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        fillColorButton = new JButton();
        fillColorButton.setBackground(fillColor);
        fillColorButton.setPreferredSize(new Dimension(100, 25));
        fillColorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择填充颜色", fillColor);
            if (c != null) {
                fillColor = c;
                fillColorButton.setBackground(c);
            }
        });
        mainPanel.add(fillColorButton, gbc);
        gbc.gridwidth = 1;

        // ========== 第4行：边界颜色 ==========
        // 注意：这里选择的边界颜色必须与画布上绑制的边界颜色一致
        // 否则算法无法正确识别边界
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("边界色:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        boundaryColorButton = new JButton();
        boundaryColorButton.setBackground(boundaryColor);
        boundaryColorButton.setPreferredSize(new Dimension(100, 25));
        boundaryColorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择边界颜色", boundaryColor);
            if (c != null) {
                boundaryColor = c;
                boundaryColorButton.setBackground(c);
            }
        });
        mainPanel.add(boundaryColorButton, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ========== 底部按钮 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton fillButton = new JButton("填充 Fill");
        fillButton.addActionListener(e -> performFill());
        buttonPanel.add(fillButton);

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
                        "• 先在画布上绘制封闭区域边界\n" +
                        "• 输入种子点坐标（区域内部一点）\n" +
                        "• 点击填充开始区域填充");
        info.setEditable(false);
        info.setBackground(getBackground());
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(info, BorderLayout.NORTH);
    }

    // ==================== 填充方法 ====================

    /**
     * 执行区域填充
     * 从种子点开始，向四周扩展填充颜色，直到遇到边界
     */
    private void performFill() {
        try {
            // 解析种子点坐标
            int x = Integer.parseInt(seedXField.getText().trim());
            int y = Integer.parseInt(seedYField.getText().trim());

            // 调用4连通种子填充算法
            // 参数：种子点(x,y)、填充颜色、边界颜色
            canvas.seedFill4(x, y, fillColor, boundaryColor);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的坐标", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
