package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * ClippingDialog.java - 裁剪算法对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于演示直线裁剪算法。
 * 裁剪是指只显示在指定矩形窗口内的部分，窗口外的部分被"剪掉"。
 * 
 * 【支持的算法】
 * 1. Cohen-Sutherland算法 - 使用区域编码快速判断直线与窗口的关系
 * 2. Cyrus-Beck算法 - 使用参数方程求交点
 * 3. 多边形裁剪 - Sutherland-Hodgman算法
 * 
 * 【Cohen-Sutherland算法原理】
 * 1. 将平面分成9个区域，每个区域用4位编码表示：
 * 上(8) 下(4) 右(2) 左(1)
 * 
 * 1001 | 1000 | 1010
 * -----+------+-----
 * 0001 | 0000 | 0010 ← 0000表示在窗口内
 * -----+------+-----
 * 0101 | 0100 | 0110
 * 
 * 2. 根据两端点的编码判断：
 * - 两端点编码都为0：完全在窗口内，直接显示
 * - 两端点编码AND不为0：完全在窗口外同一侧，直接丢弃
 * - 其他情况：需要计算交点进行裁剪
 * 
 * @author Computer Graphics Course
 */
public class ClippingDialog extends JDialog {

    /**
     * 裁剪类型枚举
     */
    public enum ClipType {
        COHEN_SUTHERLAND, // 编码裁剪法
        CYRUS_BECK, // 参数化裁剪法
        POLYGON // 多边形裁剪
    }

    // ==================== 属性 ====================

    /** 2D画布引用 */
    private Canvas2DPanel canvas;

    /** 裁剪算法类型 */
    private ClipType type;

    /** 直线端点坐标输入框 */
    private JTextField x1Field, y1Field, x2Field, y2Field;

    /** 裁剪窗口边界输入框 */
    private JTextField wxMinField, wyMinField, wxMaxField, wyMaxField;

    // ==================== 构造函数 ====================

    public ClippingDialog(JFrame parent, Canvas2DPanel canvas, ClipType type) {
        super(parent, getTitle(type), false);
        this.canvas = canvas;
        this.type = type;
        initUI();
    }

    /**
     * 根据裁剪类型返回对话框标题
     */
    private static String getTitle(ClipType type) {
        return switch (type) {
            case COHEN_SUTHERLAND -> "Sutherland-Cohen 裁剪";
            case CYRUS_BECK -> "Cyrus-Beck 裁剪";
            case POLYGON -> "多边形裁剪";
        };
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        setSize(400, 350);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ========== 裁剪窗口设置 ==========
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        mainPanel.add(new JLabel("裁剪窗口 Clip Window:"), gbc);
        gbc.gridwidth = 1;

        // 窗口左下角坐标
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("X最小:"), gbc);
        gbc.gridx = 1;
        wxMinField = new JTextField("100", 6);
        mainPanel.add(wxMinField, gbc);
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Y最小:"), gbc);
        gbc.gridx = 3;
        wyMinField = new JTextField("100", 6);
        mainPanel.add(wyMinField, gbc);

        // 窗口右上角坐标
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("X最大:"), gbc);
        gbc.gridx = 1;
        wxMaxField = new JTextField("500", 6);
        mainPanel.add(wxMaxField, gbc);
        gbc.gridx = 2;
        mainPanel.add(new JLabel("Y最大:"), gbc);
        gbc.gridx = 3;
        wyMaxField = new JTextField("400", 6);
        mainPanel.add(wyMaxField, gbc);

        // ========== 直线端点设置 ==========
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        mainPanel.add(new JLabel("直线 Line:"), gbc);
        gbc.gridwidth = 1;

        // 起点
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("P1:"), gbc);
        gbc.gridx = 1;
        x1Field = new JTextField("50", 6);
        mainPanel.add(x1Field, gbc);
        gbc.gridx = 2;
        y1Field = new JTextField("150", 6);
        mainPanel.add(y1Field, gbc);

        // 终点
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("P2:"), gbc);
        gbc.gridx = 1;
        x2Field = new JTextField("550", 6);
        mainPanel.add(x2Field, gbc);
        gbc.gridx = 2;
        y2Field = new JTextField("350", 6);
        mainPanel.add(y2Field, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ========== 按钮面板 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton showWindowButton = new JButton("显示窗口");
        showWindowButton.addActionListener(e -> showClipWindow());
        buttonPanel.add(showWindowButton);

        JButton drawOriginalButton = new JButton("绘制原线");
        drawOriginalButton.addActionListener(e -> drawOriginalLine());
        buttonPanel.add(drawOriginalButton);

        JButton clipButton = new JButton("裁剪");
        clipButton.addActionListener(e -> performClipping());
        buttonPanel.add(clipButton);

        JButton clearButton = new JButton("清除");
        clearButton.addActionListener(e -> {
            canvas.clearCanvas();
            canvas.setShowClipWindow(false);
        });
        buttonPanel.add(clearButton);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 算法说明 ==========
        String info = switch (type) {
            case COHEN_SUTHERLAND -> "Sutherland-Cohen算法:\n使用区域编码快速判断直线与窗口关系\n编码: 上下左右 (TBRL)";
            case CYRUS_BECK -> "Cyrus-Beck算法:\n使用参数化直线与窗口边界求交\n适用于凸多边形裁剪窗口";
            case POLYGON -> "Sutherland-Hodgman算法:\n依次对多边形各边进行裁剪\n生成新的裁剪后多边形";
        };
        JTextArea infoArea = new JTextArea(info);
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(infoArea, BorderLayout.NORTH);
    }

    // ==================== 操作方法 ====================

    /**
     * 在画布上显示裁剪窗口（红色矩形边框）
     */
    private void showClipWindow() {
        try {
            int xMin = Integer.parseInt(wxMinField.getText().trim());
            int yMin = Integer.parseInt(wyMinField.getText().trim());
            int xMax = Integer.parseInt(wxMaxField.getText().trim());
            int yMax = Integer.parseInt(wyMaxField.getText().trim());

            canvas.setClipWindow(new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin));
            canvas.setShowClipWindow(true);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效数值", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 绘制原始直线（灰色，用于对比）
     */
    private void drawOriginalLine() {
        try {
            int x1 = Integer.parseInt(x1Field.getText().trim());
            int y1 = Integer.parseInt(y1Field.getText().trim());
            int x2 = Integer.parseInt(x2Field.getText().trim());
            int y2 = Integer.parseInt(y2Field.getText().trim());

            canvas.drawLineBresenham(x1, y1, x2, y2, Color.GRAY);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效数值", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 执行裁剪操作
     */
    private void performClipping() {
        try {
            int x1 = Integer.parseInt(x1Field.getText().trim());
            int y1 = Integer.parseInt(y1Field.getText().trim());
            int x2 = Integer.parseInt(x2Field.getText().trim());
            int y2 = Integer.parseInt(y2Field.getText().trim());

            int xMin = Integer.parseInt(wxMinField.getText().trim());
            int yMin = Integer.parseInt(wyMinField.getText().trim());
            int xMax = Integer.parseInt(wxMaxField.getText().trim());
            int yMax = Integer.parseInt(wyMaxField.getText().trim());

            // 调用Cohen-Sutherland裁剪算法
            int[] result = cohenSutherlandClip(x1, y1, x2, y2, xMin, yMin, xMax, yMax);

            if (result != null) {
                // 裁剪成功，绘制红色裁剪后直线
                canvas.drawLineBresenham(result[0], result[1], result[2], result[3], Color.RED);
                JOptionPane.showMessageDialog(this,
                        "裁剪成功!\n裁剪后: (" + result[0] + "," + result[1] + ") - (" + result[2] + "," + result[3] + ")",
                        "结果", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "直线完全在窗口外，被完全裁剪", "结果", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效数值", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== Cohen-Sutherland算法实现 ====================

    /**
     * Cohen-Sutherland直线裁剪算法
     * 
     * 【算法步骤】
     * 1. 计算两端点的区域编码
     * 2. 如果两编码OR为0，完全可见
     * 3. 如果两编码AND不为0，完全不可见
     * 4. 否则，计算与窗口边界的交点，更新端点，重复判断
     * 
     * @return 裁剪后的端点坐标[x1,y1,x2,y2]，如果完全不可见返回null
     */
    private int[] cohenSutherlandClip(int x1, int y1, int x2, int y2,
            int xMin, int yMin, int xMax, int yMax) {

        // 区域编码常量
        final int INSIDE = 0; // 0000 - 在窗口内
        final int LEFT = 1; // 0001 - 在窗口左侧
        final int RIGHT = 2; // 0010 - 在窗口右侧
        final int BOTTOM = 4; // 0100 - 在窗口下方
        final int TOP = 8; // 1000 - 在窗口上方

        // 计算两个端点的区域编码
        int code1 = computeCode(x1, y1, xMin, yMin, xMax, yMax);
        int code2 = computeCode(x2, y2, xMin, yMin, xMax, yMax);

        // 循环处理，直到确定结果
        while (true) {
            if ((code1 | code2) == 0) {
                // 情况1: 两点都在窗口内（编码都为0）
                // 直接返回原坐标
                return new int[] { x1, y1, x2, y2 };
            } else if ((code1 & code2) != 0) {
                // 情况2: 两点在窗口同一侧（编码AND不为0）
                // 直线完全在窗口外，返回null
                return null;
            } else {
                // 情况3: 需要裁剪
                // 选择一个在窗口外的端点进行裁剪
                int x = 0, y = 0;
                int codeOut = code1 != 0 ? code1 : code2;

                // 计算与窗口边界的交点
                if ((codeOut & TOP) != 0) {
                    // 与上边界相交
                    x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
                    y = yMax;
                } else if ((codeOut & BOTTOM) != 0) {
                    // 与下边界相交
                    x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
                    y = yMin;
                } else if ((codeOut & RIGHT) != 0) {
                    // 与右边界相交
                    y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
                    x = xMax;
                } else if ((codeOut & LEFT) != 0) {
                    // 与左边界相交
                    y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
                    x = xMin;
                }

                // 更新裁剪后的端点并重新计算编码
                if (codeOut == code1) {
                    x1 = x;
                    y1 = y;
                    code1 = computeCode(x1, y1, xMin, yMin, xMax, yMax);
                } else {
                    x2 = x;
                    y2 = y;
                    code2 = computeCode(x2, y2, xMin, yMin, xMax, yMax);
                }
            }
        }
    }

    /**
     * 计算点的区域编码
     * 
     * 【编码规则】
     * - bit 0 (值1): 点在窗口左边 (x < xMin)
     * - bit 1 (值2): 点在窗口右边 (x > xMax)
     * - bit 2 (值4): 点在窗口下边 (y < yMin)
     * - bit 3 (值8): 点在窗口上边 (y > yMax)
     * 
     * @return 4位区域编码
     */
    private int computeCode(int x, int y, int xMin, int yMin, int xMax, int yMax) {
        int code = 0;
        if (x < xMin)
            code |= 1; // 左
        if (x > xMax)
            code |= 2; // 右
        if (y < yMin)
            code |= 4; // 下
        if (y > yMax)
            code |= 8; // 上
        return code;
    }
}
