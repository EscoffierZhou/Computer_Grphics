package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * MaterialDialog.java - 材质设置对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于设置3D物体的材质属性。
 * 材质决定了物体表面如何与光线相互作用。
 * 
 * 【Phong光照模型中的材质参数】
 * 1. 漫反射(Diffuse): 物体的基本颜色，受光照角度影响
 * - 面对光源的部分更亮
 * - 公式: I_d = k_d × I_l × max(N·L, 0)
 * 
 * 2. 镜面反射(Specular): 反射光产生的高光点
 * - 取决于观察角度和光源角度
 * - 公式: I_s = k_s × I_l × max(R·V, 0)^n
 * 
 * 3. 环境光(Ambient): 物体在阴影中的颜色
 * - 模拟间接光照
 * - 公式: I_a = k_a × I_a
 * 
 * 4. 光泽度(Shininess): 控制高光的大小
 * - 值越大，高光越小越集中
 * - 金属通常是64-128，塑料通常是32
 * 
 * @author Computer Graphics Course
 */
public class MaterialDialog extends JDialog {

    // ==================== 属性 ====================

    /** 3D场景面板引用 */
    private Scene3DPanel scene;

    /** 颜色选择按钮 */
    private JButton diffuseButton, specularButton, ambientButton;

    /** 漫反射颜色（物体的基本颜色） */
    private Color diffuseColor = new Color(70, 130, 180); // 钢蓝色

    /** 镜面反射颜色（高光的颜色） */
    private Color specularColor = Color.WHITE;

    /** 环境光颜色（阴影中的颜色） */
    private Color ambientColor = new Color(30, 50, 70);

    /** 光泽度滑块 */
    private JSlider shininessSlider;

    // ==================== 构造函数 ====================

    public MaterialDialog(JFrame parent, Scene3DPanel scene) {
        super(parent, "材质设置 - Material", false);
        this.scene = scene;
        initUI();
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        setSize(380, 320);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ========== 漫反射颜色 ==========
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("漫反射 Diffuse:"), gbc);
        gbc.gridx = 1;
        diffuseButton = new JButton();
        diffuseButton.setBackground(diffuseColor);
        diffuseButton.setPreferredSize(new Dimension(100, 25));
        diffuseButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "漫反射颜色", diffuseColor);
            if (c != null) {
                diffuseColor = c;
                diffuseButton.setBackground(c);
                updateMaterial();
            }
        });
        mainPanel.add(diffuseButton, gbc);

        // ========== 镜面反射颜色 ==========
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("镜面反射 Specular:"), gbc);
        gbc.gridx = 1;
        specularButton = new JButton();
        specularButton.setBackground(specularColor);
        specularButton.setPreferredSize(new Dimension(100, 25));
        specularButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "镜面反射颜色", specularColor);
            if (c != null) {
                specularColor = c;
                specularButton.setBackground(c);
                updateMaterial();
            }
        });
        mainPanel.add(specularButton, gbc);

        // ========== 环境光颜色 ==========
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("环境光 Ambient:"), gbc);
        gbc.gridx = 1;
        ambientButton = new JButton();
        ambientButton.setBackground(ambientColor);
        ambientButton.setPreferredSize(new Dimension(100, 25));
        ambientButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "环境光颜色", ambientColor);
            if (c != null) {
                ambientColor = c;
                ambientButton.setBackground(c);
                updateMaterial();
            }
        });
        mainPanel.add(ambientButton, gbc);

        // ========== 光泽度滑块 ==========
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("光泽度 Shininess:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        shininessSlider = new JSlider(1, 128, 32); // 1到128，默认32
        shininessSlider.setMajorTickSpacing(32);
        shininessSlider.setPaintTicks(true);
        shininessSlider.setPaintLabels(true);
        shininessSlider.addChangeListener(e -> updateMaterial());
        mainPanel.add(shininessSlider, gbc);
        gbc.weightx = 0;

        // ========== 预设材质按钮 ==========
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("预设材质:"), gbc);

        gbc.gridy = 5;
        JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // 金属：高光泽度，白色高光
        presetPanel.add(createPresetButton("金属", new Color(212, 175, 55), Color.WHITE, 64));
        // 塑料：中等光泽度
        presetPanel.add(createPresetButton("塑料", new Color(200, 50, 50), Color.WHITE, 32));
        // 橡胶：低光泽度，暗淡高光
        presetPanel.add(createPresetButton("橡胶", new Color(50, 50, 50), new Color(100, 100, 100), 8));
        // 玻璃：高光泽度，浅蓝色
        presetPanel.add(createPresetButton("玻璃", new Color(200, 220, 255), Color.WHITE, 96));
        mainPanel.add(presetPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ========== 按钮面板 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton applyButton = new JButton("应用到机器人");
        applyButton.addActionListener(e -> applyToRobot());
        buttonPanel.add(applyButton);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 顶部公式说明 ==========
        JTextArea info = new JTextArea(
                "Phong光照模型:\n" +
                        "I = ka*Ia + kd*Id*(N·L) + ks*Is*(R·V)^n\n" +
                        "n = 光泽度(Shininess)");
        info.setEditable(false);
        info.setBackground(getBackground());
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(info, BorderLayout.NORTH);
    }

    /**
     * 创建预设材质按钮
     * 
     * @param name      材质名称
     * @param diffuse   漫反射颜色
     * @param specular  镜面反射颜色
     * @param shininess 光泽度
     */
    private JButton createPresetButton(String name, Color diffuse, Color specular, int shininess) {
        JButton button = new JButton(name);
        button.addActionListener(e -> {
            diffuseColor = diffuse;
            specularColor = specular;
            diffuseButton.setBackground(diffuse);
            specularButton.setBackground(specular);
            shininessSlider.setValue(shininess);
            updateMaterial();
        });
        return button;
    }

    /**
     * 更新场景材质
     */
    private void updateMaterial() {
        scene.setAmbientLight(ambientColor);
        scene.repaint();
    }

    /**
     * 将当前材质应用到机器人模型
     */
    private void applyToRobot() {
        scene.getRobot().setBodyColor(diffuseColor);
        scene.repaint();
        JOptionPane.showMessageDialog(this, "材质已应用到机器人", "成功", JOptionPane.INFORMATION_MESSAGE);
    }
}
