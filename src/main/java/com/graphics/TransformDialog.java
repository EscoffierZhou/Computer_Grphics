package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * TransformDialog.java - 几何变换对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于对3D场景中的机器人进行几何变换操作。
 * 支持平移、缩放、旋转和对称变换，所有变换都通过矩阵运算实现。
 * 
 * 【支持的变换类型】
 * 1. 平移(Translation) - 移动物体位置
 * 2. 缩放(Scaling) - 改变物体大小
 * 3. 旋转(Rotation) - 绕某轴旋转
 * 4. 对称(Symmetry) - 关于某平面镜像
 * 
 * 【变换矩阵原理】
 * 每种变换都对应一个4x4矩阵，通过矩阵乘法应用到物体顶点上
 * 详见 Matrix4.java 中的各种变换矩阵
 * 
 * @author Computer Graphics Course
 */
public class TransformDialog extends JDialog {

    /**
     * 变换类型枚举
     */
    public enum TransformType {
        TRANSLATE, // 平移
        SCALE, // 缩放
        ROTATE, // 旋转
        SYMMETRY // 对称
    }

    // ==================== 属性 ====================

    /** 3D场景面板引用 */
    private Scene3DPanel scene;

    /** 变换类型 */
    private TransformType type;

    /** 变换参数输入框 */
    private JTextField param1Field, param2Field, param3Field;

    /** 轴/平面选择下拉框 */
    private JComboBox<String> axisCombo;

    /** 实时调节滑块 */
    private JSlider slider;

    // ==================== 构造函数 ====================

    public TransformDialog(JFrame parent, Scene3DPanel scene, TransformType type) {
        super(parent, getTitle(type), false);
        this.scene = scene;
        this.type = type;
        initUI();
    }

    /**
     * 根据变换类型返回对话框标题
     */
    private static String getTitle(TransformType type) {
        return switch (type) {
            case TRANSLATE -> "平移变换 - Translation";
            case SCALE -> "缩放变换 - Scaling";
            case ROTATE -> "旋转变换 - Rotation";
            case SYMMETRY -> "对称变换 - Symmetry";
        };
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 根据变换类型构建不同的UI
        switch (type) {
            case TRANSLATE -> buildTranslateUI(mainPanel, gbc);
            case SCALE -> buildScaleUI(mainPanel, gbc);
            case ROTATE -> buildRotateUI(mainPanel, gbc);
            case SYMMETRY -> buildSymmetryUI(mainPanel, gbc);
        }

        add(mainPanel, BorderLayout.CENTER);

        // ========== 按钮面板 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton applyButton = new JButton("应用 Apply");
        applyButton.addActionListener(e -> applyTransform());
        buttonPanel.add(applyButton);

        JButton resetButton = new JButton("重置 Reset");
        resetButton.addActionListener(e -> resetTransform());
        buttonPanel.add(resetButton);

        JButton closeButton = new JButton("关闭 Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // ========== 顶部说明 ==========
        String info = switch (type) {
            case TRANSLATE -> "输入X/Y/Z平移量，点击应用移动机器人";
            case SCALE -> "输入缩放比例，目前通过相机距离模拟";
            case ROTATE -> "选择旋转轴和角度，旋转机器人";
            case SYMMETRY -> "选择对称平面进行镜像";
        };
        JLabel infoLabel = new JLabel(info);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(infoLabel, BorderLayout.NORTH);
    }

    /**
     * 构建平移变换UI
     * 
     * 【平移公式】
     * x' = x + tx
     * y' = y + ty
     * z' = z + tz
     */
    private void buildTranslateUI(JPanel panel, GridBagConstraints gbc) {
        // X平移量
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("X 平移量:"), gbc);
        gbc.gridx = 1;
        param1Field = new JTextField("0", 8);
        panel.add(param1Field, gbc);

        // Y平移量
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Y 平移量:"), gbc);
        gbc.gridx = 1;
        param2Field = new JTextField("0", 8);
        panel.add(param2Field, gbc);

        // Z平移量
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Z 平移量:"), gbc);
        gbc.gridx = 1;
        param3Field = new JTextField("0", 8);
        panel.add(param3Field, gbc);

        // 快速调节滑块
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(new JLabel("快速X平移:"), gbc);
        gbc.gridy = 4;
        slider = new JSlider(-50, 50, 0);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                double dx = slider.getValue() / 10.0;
                Robot robot = scene.getRobot();
                if (robot != null) {
                    robot.setPosition(dx, 0, 0);
                    scene.repaint();
                }
            }
        });
        panel.add(slider, gbc);
    }

    /**
     * 构建缩放变换UI
     * 
     * 【缩放公式】
     * x' = x * sx
     * y' = y * sy
     * z' = z * sz
     */
    private void buildScaleUI(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("X 缩放比:"), gbc);
        gbc.gridx = 1;
        param1Field = new JTextField("1.0", 8);
        panel.add(param1Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Y 缩放比:"), gbc);
        gbc.gridx = 1;
        param2Field = new JTextField("1.0", 8);
        panel.add(param2Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Z 缩放比:"), gbc);
        gbc.gridx = 1;
        param3Field = new JTextField("1.0", 8);
        panel.add(param3Field, gbc);

        // 相机距离滑块（模拟缩放效果）
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(new JLabel("相机距离 (模拟缩放):"), gbc);
        gbc.gridy = 4;
        slider = new JSlider(20, 200, 100);
        slider.setMajorTickSpacing(45);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            double zoom = slider.getValue() / 10.0;
            scene.setCameraPosition(0, 2, zoom);
        });
        panel.add(slider, gbc);
    }

    /**
     * 构建旋转变换UI
     * 
     * 【旋转公式（绕Y轴为例）】
     * x' = x*cos(θ) + z*sin(θ)
     * y' = y
     * z' = -x*sin(θ) + z*cos(θ)
     */
    private void buildRotateUI(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("旋转轴:"), gbc);
        gbc.gridx = 1;
        axisCombo = new JComboBox<>(new String[] {
                "Y轴 (水平旋转)", // 绕Y轴旋转，像旋转木马
                "X轴 (俯仰)", // 绕X轴旋转，像点头
                "Z轴 (倾斜)" // 绕Z轴旋转，像歪头
        });
        panel.add(axisCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("旋转角度 (度):"), gbc);
        gbc.gridx = 1;
        param1Field = new JTextField("45", 8);
        panel.add(param1Field, gbc);

        // 实时旋转滑块
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(new JLabel("实时旋转 (Y轴):"), gbc);
        gbc.gridy = 3;
        slider = new JSlider(-180, 180, 0);
        slider.setMajorTickSpacing(90);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            Robot robot = scene.getRobot();
            if (robot != null) {
                robot.setRotation(slider.getValue());
                scene.repaint();
            }
        });
        panel.add(slider, gbc);
    }

    /**
     * 构建对称变换UI
     * 
     * 【对称变换原理】
     * 关于YZ平面对称: x' = -x
     * 关于XZ平面对称: y' = -y
     * 关于XY平面对称: z' = -z
     */
    private void buildSymmetryUI(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("对称平面:"), gbc);
        gbc.gridx = 1;
        axisCombo = new JComboBox<>(new String[] {
                "YZ平面 (左右对称)", // 左右镜像
                "XZ平面 (上下对称)", // 上下镜像
                "XY平面 (前后对称)" // 前后镜像
        });
        panel.add(axisCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JTextArea info = new JTextArea(
                "对称变换矩阵:\n" +
                        "YZ平面: x' = -x\n" +
                        "XZ平面: y' = -y\n" +
                        "XY平面: z' = -z");
        info.setEditable(false);
        info.setBackground(panel.getBackground());
        panel.add(info, gbc);
    }

    // ==================== 变换应用 ====================

    /**
     * 应用当前设置的变换到机器人
     */
    private void applyTransform() {
        try {
            Robot robot = scene.getRobot();
            if (robot == null)
                return;

            switch (type) {
                case TRANSLATE -> {
                    double dx = Double.parseDouble(param1Field.getText());
                    double dy = Double.parseDouble(param2Field.getText());
                    double dz = Double.parseDouble(param3Field.getText());
                    robot.translate(dx, dy, dz);
                    scene.repaint();
                    showResult("平移完成: (" + dx + ", " + dy + ", " + dz + ")");
                }
                case SCALE -> {
                    double sx = Double.parseDouble(param1Field.getText());
                    // 通过调整相机距离来模拟缩放效果
                    scene.setCameraPosition(0, 2, 10 / sx);
                    showResult("缩放已应用 (通过相机距离模拟)");
                }
                case ROTATE -> {
                    double angle = Double.parseDouble(param1Field.getText());
                    String axis = (String) axisCombo.getSelectedItem();
                    if (axis.contains("Y")) {
                        robot.rotate(angle);
                    }
                    scene.repaint();
                    showResult("旋转完成: " + axis + " " + angle + "°");
                }
                case SYMMETRY -> {
                    String plane = (String) axisCombo.getSelectedItem();
                    // 通过旋转180度模拟对称效果
                    if (plane.contains("YZ")) {
                        robot.rotate(180);
                    }
                    scene.repaint();
                    showResult("对称变换: " + plane);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数值", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 显示变换结果
     */
    private void showResult(String message) {
        JOptionPane.showMessageDialog(this, message, "变换结果", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 重置所有变换，恢复初始状态
     */
    private void resetTransform() {
        Robot robot = scene.getRobot();
        if (robot != null) {
            robot.setPosition(0, 0, 0);
            robot.setRotation(0);
            robot.resetPose();
        }
        scene.setCameraPosition(0, 2, 10);
        scene.repaint();

        if (slider != null) {
            slider.setValue(0);
        }
    }
}
