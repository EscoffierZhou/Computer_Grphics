package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * LineDrawingDialog.java - 直线绘制对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框让用户输入直线的起点和终点坐标，
 * 选择算法（Bresenham或DDA），然后在2D画布上绘制直线。
 * 
 * 【支持的算法】
 * 1. Bresenham算法 - 只使用整数运算，效率高
 * 2. DDA算法 - 使用浮点运算，原理简单易懂
 * 
 * 【界面组件】
 * - 算法选择下拉框
 * - 起点坐标输入框 (x1, y1)
 * - 终点坐标输入框 (x2, y2)
 * - 颜色选择按钮
 * - 绘制/清除/关闭按钮
 * 
 * @author Computer Graphics Course
 */
public class LineDrawingDialog extends JDialog {

    // ==================== 属性 ====================

    /**
     * 2D画布的引用，用于调用绑图方法
     */
    private Canvas2DPanel canvas;

    /**
     * 起点和终点坐标的输入框
     */
    private JTextField x1Field, y1Field, x2Field, y2Field;

    /**
     * 算法选择下拉框
     */
    private JComboBox<String> algorithmCombo;

    /**
     * 颜色选择按钮（点击后弹出颜色选择器）
     */
    private JButton colorButton;

    /**
     * 当前选择的绑图颜色
     */
    private Color currentColor = Color.WHITE;

    // ==================== 构造函数 ====================

    /**
     * 构造函数
     * 
     * @param parent 父窗口（MainFrame）
     * @param canvas 2D画布对象
     */
    public LineDrawingDialog(JFrame parent, Canvas2DPanel canvas) {
        // 调用父类构造函数，设置标题，false表示非模态对话框（可以同时操作主窗口）
        super(parent, "直线绘制 - Line Drawing", false);
        this.canvas = canvas;
        initUI(); // 初始化用户界面
    }

    // ==================== 界面初始化 ====================

    /**
     * 初始化用户界面
     * 使用GridBagLayout布局管理器来排列组件
     */
    private void initUI() {
        // 设置对话框大小和位置
        setSize(350, 280);
        setLocationRelativeTo(getParent()); // 居中显示
        setLayout(new BorderLayout(10, 10)); // 边界布局，组件间距10像素

        // 创建主面板，使用GridBagLayout实现网格布局
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // GridBagConstraints用于控制组件在网格中的位置和大小
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 组件周围的边距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平方向填充

        // ========== 第1行：算法选择 ==========
        gbc.gridx = 0; // 列号
        gbc.gridy = 0; // 行号
        mainPanel.add(new JLabel("算法 Algorithm:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2; // 跨两列
        algorithmCombo = new JComboBox<>(new String[] {
                "Bresenham", // Bresenham算法
                "DDA (基本增量法)" // DDA算法
        });
        mainPanel.add(algorithmCombo, gbc);
        gbc.gridwidth = 1; // 恢复为1列

        // ========== 第2行：起点坐标 ==========
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("起点 P1:"), gbc);

        gbc.gridx = 1;
        x1Field = new JTextField("100", 6); // 默认值100，宽度6个字符
        mainPanel.add(x1Field, gbc);

        gbc.gridx = 2;
        y1Field = new JTextField("100", 6);
        mainPanel.add(y1Field, gbc);

        // ========== 第3行：终点坐标 ==========
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("终点 P2:"), gbc);

        gbc.gridx = 1;
        x2Field = new JTextField("400", 6);
        mainPanel.add(x2Field, gbc);

        gbc.gridx = 2;
        y2Field = new JTextField("300", 6);
        mainPanel.add(y2Field, gbc);

        // ========== 第4行：颜色选择 ==========
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("颜色 Color:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        colorButton = new JButton();
        colorButton.setBackground(currentColor); // 用背景色显示当前颜色
        colorButton.setPreferredSize(new Dimension(100, 25));
        // 点击按钮时弹出颜色选择器
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

        // 绑图按钮
        JButton drawButton = new JButton("绘制 Draw");
        drawButton.addActionListener(e -> drawLine()); // 点击时调用drawLine方法
        buttonPanel.add(drawButton);

        // 清除按钮
        JButton clearButton = new JButton("清除 Clear");
        clearButton.addActionListener(e -> canvas.clearCanvas());
        buttonPanel.add(clearButton);

        // 关闭按钮
        JButton closeButton = new JButton("关闭 Close");
        closeButton.addActionListener(e -> dispose()); // 关闭对话框
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 顶部说明文字 ==========
        JTextArea info = new JTextArea(
                "说明:\n" +
                        "• Bresenham算法: 整数运算，效率高\n" +
                        "• DDA算法: 浮点运算，直观易理解");
        info.setEditable(false); // 不可编辑
        info.setBackground(getBackground()); // 背景色与对话框一致
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(info, BorderLayout.NORTH);
    }

    // ==================== 绑图方法 ====================

    /**
     * 执行直线绘制
     * 从输入框获取坐标，调用相应的绘图算法
     */
    private void drawLine() {
        try {
            // 解析输入的坐标值
            int x1 = Integer.parseInt(x1Field.getText().trim());
            int y1 = Integer.parseInt(y1Field.getText().trim());
            int x2 = Integer.parseInt(x2Field.getText().trim());
            int y2 = Integer.parseInt(y2Field.getText().trim());

            // 获取选择的算法
            String algorithm = (String) algorithmCombo.getSelectedItem();

            // 根据算法选择调用相应的方法
            if (algorithm.startsWith("Bresenham")) {
                // 调用Bresenham直线算法
                canvas.drawLineBresenham(x1, y1, x2, y2, currentColor);
            } else {
                // 调用DDA直线算法
                canvas.drawLineDDA(x1, y1, x2, y2, currentColor);
            }
        } catch (NumberFormatException e) {
            // 如果输入的不是有效整数，显示错误提示
            JOptionPane.showMessageDialog(this, "请输入有效的整数坐标", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
