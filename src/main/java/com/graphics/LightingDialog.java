package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * LightingDialog.java - 光照设置对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于控制3D场景中的光照效果。
 * 光照是使3D物体看起来有立体感的关键因素。
 * 
 * 【光照类型】
 * 1. 环境光(Ambient Light)
 * - 来自四面八方的均匀光照
 * - 模拟被墙壁、天空等反射的间接光
 * - 公式: I_ambient = k_a × I_a
 * 
 * 2. 点光源(Point Light)
 * - 从特定位置向所有方向发出的光
 * - 类似电灯泡
 * - 产生明暗变化和高光
 * 
 * 【Phong光照模型】
 * 本系统使用Phong模型计算光照：
 * I_total = I_ambient + I_diffuse + I_specular
 * 
 * 其中：
 * - I_ambient = k_a × I_a（环境光分量）
 * - I_diffuse = k_d × I_l × max(N·L, 0)（漫反射分量）
 * - I_specular = k_s × I_l × max(R·V, 0)^n（镜面反射分量）
 * 
 * @author Computer Graphics Course
 */
public class LightingDialog extends JDialog {

    /**
     * 光源类型枚举
     */
    public enum LightType {
        AMBIENT, // 环境光
        POINT // 点光源
    }

    // ==================== 属性 ====================

    /** 3D场景面板引用 */
    private Scene3DPanel scene;

    /** 光源类型 */
    private LightType type;

    /** 光照强度滑块 */
    private JSlider intensitySlider;

    /** 光源颜色选择按钮 */
    private JButton colorButton;

    /** 光源颜色 */
    private Color lightColor = Color.WHITE;

    /** 点光源位置滑块（仅点光源使用） */
    private JSlider posXSlider, posYSlider, posZSlider;

    // ==================== 构造函数 ====================

    public LightingDialog(JFrame parent, Scene3DPanel scene, LightType type) {
        super(parent, type == LightType.AMBIENT ? "环境光设置" : "点光源设置", false);
        this.scene = scene;
        this.type = type;
        initUI();
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        // 点光源需要更大的窗口来显示位置控制
        setSize(400, type == LightType.POINT ? 350 : 220);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ========== 光照颜色 ==========
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("颜色:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        colorButton = new JButton();
        colorButton.setBackground(lightColor);
        colorButton.setPreferredSize(new Dimension(100, 25));
        colorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "选择光照颜色", lightColor);
            if (c != null) {
                lightColor = c;
                colorButton.setBackground(c);
                updateLight();
            }
        });
        mainPanel.add(colorButton, gbc);
        gbc.gridwidth = 1;

        // ========== 光照强度 ==========
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("强度:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        intensitySlider = new JSlider(0, 100, 100); // 0-100%
        intensitySlider.setMajorTickSpacing(25);
        intensitySlider.setPaintTicks(true);
        intensitySlider.setPaintLabels(true);
        intensitySlider.addChangeListener(e -> updateLight());
        mainPanel.add(intensitySlider, gbc);
        gbc.weightx = 0;
        gbc.gridwidth = 1;

        // ========== 点光源位置（仅点光源显示） ==========
        if (type == LightType.POINT) {
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            mainPanel.add(new JLabel("光源位置:"), gbc);
            gbc.gridwidth = 1;

            // X位置
            gbc.gridx = 0;
            gbc.gridy = 3;
            mainPanel.add(new JLabel("X:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            posXSlider = new JSlider(-10, 10, 5); // 默认在右侧
            posXSlider.addChangeListener(e -> updateLight());
            mainPanel.add(posXSlider, gbc);
            gbc.weightx = 0;
            gbc.gridwidth = 1;

            // Y位置
            gbc.gridx = 0;
            gbc.gridy = 4;
            mainPanel.add(new JLabel("Y:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            posYSlider = new JSlider(-5, 15, 10); // 默认在上方
            posYSlider.addChangeListener(e -> updateLight());
            mainPanel.add(posYSlider, gbc);
            gbc.weightx = 0;
            gbc.gridwidth = 1;

            // Z位置
            gbc.gridx = 0;
            gbc.gridy = 5;
            mainPanel.add(new JLabel("Z:"), gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.weightx = 1;
            posZSlider = new JSlider(-10, 10, 5); // 默认在前方
            posZSlider.addChangeListener(e -> updateLight());
            mainPanel.add(posZSlider, gbc);
        }

        add(mainPanel, BorderLayout.CENTER);

        // ========== 按钮面板 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> resetLight());
        buttonPanel.add(resetButton);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 顶部说明 ==========
        String info = type == LightType.AMBIENT
                ? "环境光: 来自四面八方的均匀光照\nI_ambient = k_a * I_a"
                : "点光源: 从特定位置发出的光\nI = I_ambient + I_diffuse + I_specular";
        JTextArea infoArea = new JTextArea(info);
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(infoArea, BorderLayout.NORTH);
    }

    // ==================== 光照更新 ====================

    /**
     * 根据界面设置更新光照参数
     */
    private void updateLight() {
        double intensity = intensitySlider.getValue() / 100.0;

        if (type == LightType.AMBIENT) {
            // 环境光：根据强度调整颜色亮度
            int r = (int) (lightColor.getRed() * intensity);
            int g = (int) (lightColor.getGreen() * intensity);
            int b = (int) (lightColor.getBlue() * intensity);
            scene.setAmbientLight(new Color(r, g, b));
        } else {
            // 点光源：设置强度和位置
            scene.setLightIntensity(intensity);
            scene.setLightPosition(posXSlider.getValue(), posYSlider.getValue(), posZSlider.getValue());
        }
    }

    /**
     * 重置光照为默认值
     */
    private void resetLight() {
        lightColor = Color.WHITE;
        colorButton.setBackground(lightColor);
        intensitySlider.setValue(100);
        if (type == LightType.POINT) {
            posXSlider.setValue(5);
            posYSlider.setValue(10);
            posZSlider.setValue(5);
        }
        updateLight();
    }
}
