package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * ViewpointDialog.java - 视点变换对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于控制3D场景中的相机（观察者视角）。
 * 通过调整相机的位置和旋转角度，可以从不同角度观察场景。
 * 
 * 【视点变换原理】
 * 在3D图形学中，有两种方式改变视角：
 * 1. 移动物体（让物体绕着观察者转）
 * 2. 移动相机（让观察者绕着物体转）
 * 
 * 本系统采用第二种方式，更符合现实世界的习惯。
 * 
 * 【相机参数】
 * 1. 位置(Position): 相机在世界坐标系中的坐标 (x, y, z)
 * 2. 俯仰(Pitch): 相机抬头/低头的角度（绕X轴旋转）
 * 3. 偏航(Yaw): 相机左右转动的角度（绕Y轴旋转）
 * 
 * 【预设视角】
 * - 俯视：从上往下看
 * - 正面：正对物体
 * - 侧面：从侧面观察
 * 
 * @author Computer Graphics Course
 */
public class ViewpointDialog extends JDialog {

    // ==================== 属性 ====================

    /** 3D场景面板引用 */
    private Scene3DPanel scene;

    /** 相机位置滑块 */
    private JSlider camXSlider, camYSlider, camZSlider;

    /** 相机旋转滑块 */
    private JSlider rotXSlider, rotYSlider;

    /** 当前参数显示标签 */
    private JLabel posLabel;

    // ==================== 构造函数 ====================

    public ViewpointDialog(JFrame parent, Scene3DPanel scene) {
        super(parent, "视点变换 - Viewpoint", false);
        this.scene = scene;
        initUI();
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

        // ========== 相机位置控制 ==========
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("相机位置 Camera Position:"), gbc);
        gbc.gridwidth = 1;

        // X位置（左右）
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("X:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        camXSlider = new JSlider(-10, 10, 0); // 范围-10到10，默认0
        camXSlider.addChangeListener(e -> updateCamera());
        mainPanel.add(camXSlider, gbc);
        gbc.weightx = 0;

        // Y位置（上下）
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Y:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        camYSlider = new JSlider(-5, 10, 2); // 默认Y=2，稍微在上面
        camYSlider.addChangeListener(e -> updateCamera());
        mainPanel.add(camYSlider, gbc);
        gbc.weightx = 0;

        // Z位置（远近）- 这是最常用的调节
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Z:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        camZSlider = new JSlider(2, 30, 10); // Z越大离得越远
        camZSlider.addChangeListener(e -> updateCamera());
        mainPanel.add(camZSlider, gbc);
        gbc.weightx = 0;

        // ========== 相机旋转控制 ==========
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("相机旋转 Camera Rotation:"), gbc);
        gbc.gridwidth = 1;

        // 俯仰角（上下看）
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("俯仰:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        rotXSlider = new JSlider(-89, 89, -15); // -90到90度，默认稍微向下看
        rotXSlider.addChangeListener(e -> updateCamera());
        mainPanel.add(rotXSlider, gbc);
        gbc.weightx = 0;

        // 偏航角（左右看）
        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("偏航:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        rotYSlider = new JSlider(-180, 180, 0); // -180到180度
        rotYSlider.addChangeListener(e -> updateCamera());
        mainPanel.add(rotYSlider, gbc);
        gbc.weightx = 0;

        // ========== 当前参数显示 ==========
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        posLabel = new JLabel("位置: (0.0, 2.0, 10.0) 旋转: (-15°, 0°)");
        mainPanel.add(posLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ========== 按钮面板 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 重置按钮
        JButton resetButton = new JButton("重置 Reset");
        resetButton.addActionListener(e -> {
            camXSlider.setValue(0);
            camYSlider.setValue(2);
            camZSlider.setValue(10);
            rotXSlider.setValue(-15);
            rotYSlider.setValue(0);
            updateCamera();
        });
        buttonPanel.add(resetButton);

        // 俯视按钮 - 从正上方向下看
        JButton topButton = new JButton("俯视");
        topButton.addActionListener(e -> {
            camXSlider.setValue(0);
            camYSlider.setValue(10); // 相机在高处
            camZSlider.setValue(2); // 靠近物体正上方
            rotXSlider.setValue(-80); // 几乎垂直向下看
            rotYSlider.setValue(0);
            updateCamera();
        });
        buttonPanel.add(topButton);

        // 正面按钮 - 正对物体
        JButton frontButton = new JButton("正面");
        frontButton.addActionListener(e -> {
            camXSlider.setValue(0);
            camYSlider.setValue(0); // 与物体同高
            camZSlider.setValue(10); // 在Z轴正方向
            rotXSlider.setValue(0); // 不俯仰
            rotYSlider.setValue(0); // 不偏航
            updateCamera();
        });
        buttonPanel.add(frontButton);

        // 侧面按钮 - 从侧面观察
        JButton sideButton = new JButton("侧面");
        sideButton.addActionListener(e -> {
            camXSlider.setValue(10); // 在X轴正方向
            camYSlider.setValue(0);
            camZSlider.setValue(2); // 靠近物体
            rotXSlider.setValue(0);
            rotYSlider.setValue(-90); // 向左转90度
            updateCamera();
        });
        buttonPanel.add(sideButton);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 顶部说明 ==========
        JTextArea info = new JTextArea("视点变换控制: Viewing Transformation\n相机位置和朝向决定3D场景的观察角度");
        info.setEditable(false);
        info.setBackground(getBackground());
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(info, BorderLayout.NORTH);
    }

    // ==================== 相机更新 ====================

    /**
     * 根据滑块值更新相机参数
     * 同时更新状态显示标签
     */
    private void updateCamera() {
        double x = camXSlider.getValue();
        double y = camYSlider.getValue();
        double z = camZSlider.getValue();
        double rotX = rotXSlider.getValue();
        double rotY = rotYSlider.getValue();

        // 应用相机变换到场景
        scene.setCameraPosition(x, y, z);
        scene.setCameraRotation(rotX, rotY);

        // 更新显示标签
        posLabel.setText(String.format("位置: (%.1f, %.1f, %.1f) 旋转: (%.0f°, %.0f°)", x, y, z, rotX, rotY));
    }
}
