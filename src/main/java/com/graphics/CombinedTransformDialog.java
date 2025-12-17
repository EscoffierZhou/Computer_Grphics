package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * CombinedTransformDialog.java - 组合变换对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框用于演示多个变换的组合。
 * 在计算机图形学中，多个变换可以通过矩阵乘法组合成一个矩阵。
 * 
 * 【矩阵组合原理】
 * 如果要对一个点依次进行变换T1、T2、T3，则：
 * P' = T3 × T2 × T1 × P
 * 
 * 注意矩阵乘法的顺序是从右往左的！
 * 最右边的变换最先执行。
 * 
 * 【组合变换的优势】
 * 1. 效率：多个变换组合成一个矩阵，每个顶点只需乘一次
 * 2. 灵活：可以任意组合平移、旋转、缩放等变换
 * 
 * 【示例】
 * 要实现"绕点(5,0,0)旋转45度"：
 * 1. 先平移到原点：T(-5, 0, 0)
 * 2. 在原点旋转：R(45°)
 * 3. 再平移回去：T(5, 0, 0)
 * 组合矩阵 = T(5,0,0) × R(45°) × T(-5,0,0)
 * 
 * @author Computer Graphics Course
 */
public class CombinedTransformDialog extends JDialog {

    // ==================== 属性 ====================

    /** 3D场景面板引用 */
    private Scene3DPanel scene;

    /** 变换列表的数据模型 */
    private DefaultListModel<String> transformListModel;

    /** 变换列表UI组件 */
    private JList<String> transformList;

    // ==================== 构造函数 ====================

    public CombinedTransformDialog(JFrame parent, Scene3DPanel scene) {
        super(parent, "组合变换 - Combined Transform", false);
        this.scene = scene;
        initUI();
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // ========== 变换列表 ==========
        transformListModel = new DefaultListModel<>();
        transformList = new JList<>(transformListModel);
        JScrollPane scrollPane = new JScrollPane(transformList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("变换序列 (从上到下执行)"));
        add(scrollPane, BorderLayout.CENTER);

        // ========== 添加变换按钮面板 ==========
        JPanel addPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("添加变换"));

        // 各种变换按钮
        addPanel.add(createAddButton("平移", () -> addTransform("平移(1, 0, 0)")));
        addPanel.add(createAddButton("缩放", () -> addTransform("缩放(1.5, 1.5, 1.5)")));
        addPanel.add(createAddButton("旋转X", () -> addTransform("旋转X(45°)")));
        addPanel.add(createAddButton("旋转Y", () -> addTransform("旋转Y(45°)")));
        addPanel.add(createAddButton("旋转Z", () -> addTransform("旋转Z(45°)")));
        addPanel.add(createAddButton("对称XY", () -> addTransform("对称(XY平面)")));
        addPanel.add(createAddButton("错切", () -> addTransform("错切(0.5, 0, 0)")));
        addPanel.add(createAddButton("删除选中", this::removeSelected));

        add(addPanel, BorderLayout.EAST);

        // ========== 结果显示区 ==========
        JTextArea resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        resultArea.setBorder(BorderFactory.createTitledBorder("组合矩阵"));
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        // ========== 操作按钮 ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 计算按钮：显示组合后的矩阵
        JButton computeButton = new JButton("计算 Compute");
        computeButton.addActionListener(e -> {
            String result = computeCombinedMatrix();
            resultArea.setText(result);
        });
        buttonPanel.add(computeButton);

        // 应用按钮：将变换应用到机器人
        JButton applyButton = new JButton("应用 Apply");
        applyButton.addActionListener(e -> {
            applyTransforms();
            resultArea.setText("变换已应用到机器人模型");
        });
        buttonPanel.add(applyButton);

        // 清空按钮
        JButton clearButton = new JButton("清空 Clear");
        clearButton.addActionListener(e -> transformListModel.clear());
        buttonPanel.add(clearButton);

        JButton closeButton = new JButton("关闭 Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.NORTH);
    }

    /**
     * 创建添加变换的按钮
     */
    private JButton createAddButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    /**
     * 向列表添加一个变换
     */
    private void addTransform(String transform) {
        transformListModel.addElement(transform);
    }

    /**
     * 删除选中的变换
     */
    private void removeSelected() {
        int index = transformList.getSelectedIndex();
        if (index >= 0) {
            transformListModel.remove(index);
        }
    }

    // ==================== 矩阵计算 ====================

    /**
     * 计算组合矩阵
     * 将列表中的所有变换矩阵相乘
     * 
     * @return 包含变换序列和最终矩阵的字符串
     */
    private String computeCombinedMatrix() {
        // 从单位矩阵开始
        double[][] result = Matrix4.identity();

        StringBuilder sb = new StringBuilder();
        sb.append("变换序列:\n");

        // 依次乘以每个变换矩阵
        for (int i = 0; i < transformListModel.size(); i++) {
            String transform = transformListModel.get(i);
            sb.append((i + 1) + ". " + transform + "\n");

            // 解析变换字符串，获取对应的变换矩阵
            double[][] m = parseTransform(transform);
            // 矩阵乘法：新变换矩阵 × 当前结果
            result = Matrix4.multiply(m, result);
        }

        // 显示最终的组合矩阵
        sb.append("\n组合矩阵 M = M").append(transformListModel.size());
        sb.append(" × ... × M1:\n");
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("[%7.3f %7.3f %7.3f %7.3f]\n",
                    result[i][0], result[i][1], result[i][2], result[i][3]));
        }

        return sb.toString();
    }

    /**
     * 将变换描述字符串解析为对应的变换矩阵
     * 
     * @param transform 变换描述，如"平移(1, 0, 0)"
     * @return 对应的4x4变换矩阵
     */
    private double[][] parseTransform(String transform) {
        if (transform.startsWith("平移")) {
            return Matrix4.translate(1, 0, 0);
        } else if (transform.startsWith("缩放")) {
            return Matrix4.scale(1.5, 1.5, 1.5);
        } else if (transform.startsWith("旋转X")) {
            return Matrix4.rotateX(Math.toRadians(45));
        } else if (transform.startsWith("旋转Y")) {
            return Matrix4.rotateY(Math.toRadians(45));
        } else if (transform.startsWith("旋转Z")) {
            return Matrix4.rotateZ(Math.toRadians(45));
        } else if (transform.startsWith("对称")) {
            return Matrix4.reflectXY();
        } else if (transform.startsWith("错切")) {
            return Matrix4.shear(0.5, 0, 0, 0, 0, 0);
        }
        return Matrix4.identity();
    }

    /**
     * 将变换序列应用到机器人模型
     */
    private void applyTransforms() {
        Robot robot = scene.getRobot();
        for (int i = 0; i < transformListModel.size(); i++) {
            String transform = transformListModel.get(i);
            if (transform.startsWith("平移")) {
                robot.translate(1, 0, 0);
            } else if (transform.startsWith("旋转Y")) {
                robot.rotate(45);
            }
        }
        scene.repaint();
    }
}
