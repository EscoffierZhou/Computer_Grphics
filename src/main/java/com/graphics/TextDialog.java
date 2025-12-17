package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * TextDialog.java - 文字输入工具对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框让用户输入文字，选择字体、字号和样式，
 * 然后点击画布任意位置来放置文字。
 * 
 * 【使用步骤】
 * 1. 输入要显示的文字
 * 2. 选择字体（SansSerif, Serif, Monospaced等）
 * 3. 设置字号（8-120）
 * 4. 可选：勾选粗体/斜体
 * 5. 在预览区确认效果
 * 6. 点击"开始绘制"
 * 7. 在画布上点击放置文字
 * 
 * 【Java字体系统】
 * Java提供了跨平台的逻辑字体：
 * - SansSerif: 无衬线字体（如黑体）
 * - Serif: 衬线字体（如宋体）
 * - Monospaced: 等宽字体（如Courier）
 * - Dialog: 对话框字体
 * 
 * @author Computer Graphics Course
 */
public class TextDialog extends JDialog {

    // ==================== 属性 ====================

    /** 2D画布引用 */
    private Canvas2DPanel canvas;

    /** 文字输入框 */
    private JTextField inputField;

    /** 字体选择下拉框 */
    private JComboBox<String> fontCombo;

    /** 粗体复选框 */
    private JCheckBox boldCheck;

    /** 斜体复选框 */
    private JCheckBox italicCheck;

    /** 字号调节器 */
    private JSpinner sizeSpinner;

    /** 实时预览标签 */
    private JLabel previewLabel;

    // ==================== 构造函数 ====================

    public TextDialog(Frame owner, Canvas2DPanel canvas) {
        super(owner, "文字工具", false);
        this.canvas = canvas;

        setSize(300, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // ========== 输入面板 ==========
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // 文字输入
        inputPanel.add(new JLabel("输入文字:"));
        inputField = new JTextField("Hello Graphics");
        inputPanel.add(inputField);

        // 字体选择
        inputPanel.add(new JLabel("字体:"));
        String[] fonts = { "SansSerif", "Serif", "Monospaced", "Dialog" };
        fontCombo = new JComboBox<>(fonts);
        inputPanel.add(fontCombo);

        // 字号设置
        inputPanel.add(new JLabel("字号:"));
        sizeSpinner = new JSpinner(new SpinnerNumberModel(24, 8, 120, 1));
        inputPanel.add(sizeSpinner);

        // 样式选择
        inputPanel.add(new JLabel("样式:"));
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        boldCheck = new JCheckBox("粗体");
        italicCheck = new JCheckBox("斜体");
        stylePanel.add(boldCheck);
        stylePanel.add(italicCheck);
        inputPanel.add(stylePanel);

        add(inputPanel, BorderLayout.CENTER);

        // ========== 按钮面板 ==========
        JPanel btnPanel = new JPanel();
        JButton okBtn = new JButton("开始绘制");
        okBtn.addActionListener(e -> applySettings());

        JButton cancelBtn = new JButton("取消");
        cancelBtn.addActionListener(e -> setVisible(false));

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // ========== 预览区域 ==========
        previewLabel = new JLabel("预览 Preview", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(300, 60));
        previewLabel.setBorder(BorderFactory.createTitledBorder("预览"));
        add(previewLabel, BorderLayout.NORTH);

        // ========== 监听变化以更新预览 ==========
        inputField.addActionListener(e -> updatePreview());
        fontCombo.addActionListener(e -> updatePreview());
        boldCheck.addActionListener(e -> updatePreview());
        italicCheck.addActionListener(e -> updatePreview());
        sizeSpinner.addChangeListener(e -> updatePreview());

        updatePreview(); // 初始化预览
    }

    /**
     * 更新预览标签的字体和文字
     */
    private void updatePreview() {
        String text = inputField.getText();
        String family = (String) fontCombo.getSelectedItem();

        // 计算字体样式
        int style = Font.PLAIN;
        if (boldCheck.isSelected())
            style |= Font.BOLD; // 添加粗体
        if (italicCheck.isSelected())
            style |= Font.ITALIC; // 添加斜体

        int size = (int) sizeSpinner.getValue();

        // 创建字体对象并应用到预览标签
        Font font = new Font(family, style, size);
        previewLabel.setFont(font);
        previewLabel.setText(text);
        previewLabel.repaint();
    }

    /**
     * 应用设置到画布
     * 设置画布的绘图模式为TEXT，然后等待用户点击画布
     */
    private void applySettings() {
        String text = inputField.getText();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入文字");
            return;
        }

        String family = (String) fontCombo.getSelectedItem();
        int style = Font.PLAIN;
        if (boldCheck.isSelected())
            style |= Font.BOLD;
        if (italicCheck.isSelected())
            style |= Font.ITALIC;
        int size = (int) sizeSpinner.getValue();

        Font font = new Font(family, style, size);

        // 将文字和字体设置到Canvas2DPanel
        canvas.setTextToDraw(text);
        canvas.setCurrentFont(font);
        canvas.setDrawMode(Canvas2DPanel.DrawMode.TEXT);

        setVisible(false);
        JOptionPane.showMessageDialog(getParent(), "点击画布任意位置放置文字");
    }
}
