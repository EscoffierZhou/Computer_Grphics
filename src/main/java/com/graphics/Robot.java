package com.graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * Robot.java - 3D机器人模型类
 * ====================================================================
 * 
 * 【功能说明】
 * 这个类定义了一个由长方体组成的3D机器人模型。
 * 机器人包含6个部件：头部、躯干、左臂、右臂、左腿、右腿。
 * 每个部件都可以独立旋转，实现关节动画效果。
 * 
 * 【层级结构】
 * 机器人采用"层级模型"（场景图）结构：
 * - 躯干是根节点，其他部件都是躯干的子节点
 * - 当躯干移动时，所有子部件跟随移动
 * - 每个部件可以相对于其父节点进行局部变换
 * 
 * 【关节动画原理】
 * 1. 先将关节点平移到原点
 * 2. 在原点进行旋转
 * 3. 再平移回原来的位置
 * 这样旋转就是绕关节点进行的
 * 
 * 【坐标系】
 * - X轴：左右方向（正向为右）
 * - Y轴：上下方向（正向为上）
 * - Z轴：前后方向（正向为前）
 * 
 * @author Computer Graphics Course
 */
public class Robot {

    // ==================== 机器人部件 ====================

    /** 躯干 - 机器人的核心部件 */
    private RobotPart body;

    /** 头部 - 可以左右转动 */
    private RobotPart head;

    /** 左臂 - 可以前后和侧向摆动 */
    private RobotPart leftArm;

    /** 右臂 */
    private RobotPart rightArm;

    /** 左腿 - 可以前后摆动 */
    private RobotPart leftLeg;

    /** 右腿 */
    private RobotPart rightLeg;

    // ==================== 关节角度（单位：度） ====================

    /** 头部绕Y轴旋转角度（左右转头） */
    private double headRotY = 0;

    /** 左臂绕X轴旋转（前后摆动）和绕Z轴旋转（侧向摆动） */
    private double leftArmRotX = 0, leftArmRotZ = 0;

    /** 右臂旋转角度 */
    private double rightArmRotX = 0, rightArmRotZ = 0;

    /** 左腿绕X轴旋转（前后摆动） */
    private double leftLegRotX = 0;

    /** 右腿旋转角度 */
    private double rightLegRotX = 0;

    // ==================== 整体位置和朝向 ====================

    /** 机器人在世界坐标系中的位置 */
    private double posX = 0, posY = 0, posZ = 0;

    /** 机器人整体绕Y轴的旋转角度（朝向） */
    private double rotY = 0;

    // ==================== 动画状态 ====================

    /** 是否正在播放动画 */
    private boolean isAnimating = false;

    /** 动画计时器 */
    private double animationTime = 0;

    // ==================== 颜色设置 ====================

    /** 躯干颜色 - 钢蓝色 */
    private Color bodyColor = new Color(70, 130, 180);

    /** 头部颜色 - 肤色 */
    private Color headColor = new Color(255, 200, 150);

    /** 四肢颜色 - 矢车菊蓝 */
    private Color limbColor = new Color(100, 149, 237);

    // ==================== 自定义模型支持 ====================

    /**
     * 自定义模型部件列表
     * 如果不为空，则渲染这个而不是标准机器人
     * 用于支持形状设计器创建的自定义形状
     */
    private List<Polygon3D> customParts = null;

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数 - 创建标准机器人
     */
    public Robot() {
        buildRobot();
    }

    /**
     * 自定义模型构造函数
     * 
     * @param customParts 自定义的多边形列表
     */
    public Robot(List<Polygon3D> customParts) {
        this.customParts = customParts;
    }

    // ==================== 模型构建 ====================

    /**
     * 构建机器人各部件
     * 每个部件使用createBox方法创建长方体
     * 参数说明：中心位置(cx, cy, cz), 尺寸(width, height, depth)
     */
    private void buildRobot() {
        // 躯干 - 位于原点，是最大的部件
        body = new RobotPart("body", createBox(0, 0, 0, 0.8, 1.2, 0.5), bodyColor);

        // 头部 - 在躯干上方
        head = new RobotPart("head", createBox(0, 1.0, 0, 0.5, 0.5, 0.5), headColor);

        // 左臂 - 在躯干左侧，Y坐标稍高表示从肩膀开始
        leftArm = new RobotPart("leftArm", createBox(-0.65, 0.3, 0, 0.25, 0.8, 0.25), limbColor);

        // 右臂 - 在躯干右侧
        rightArm = new RobotPart("rightArm", createBox(0.65, 0.3, 0, 0.25, 0.8, 0.25), limbColor);

        // 左腿 - 在躯干下方左侧
        leftLeg = new RobotPart("leftLeg", createBox(-0.25, -1.2, 0, 0.3, 0.9, 0.3), limbColor);

        // 右腿 - 在躯干下方右侧
        rightLeg = new RobotPart("rightLeg", createBox(0.25, -1.2, 0, 0.3, 0.9, 0.3), limbColor);
    }

    /**
     * 创建长方体的8个顶点
     * 
     * 【长方体顶点编号】
     * 顶点按照以下方式编号（从前面看）：
     * 7----6
     * /| /|
     * 3----2 |
     * | 4--|-5
     * |/ |/
     * 0----1
     * 
     * @param cx     中心X坐标
     * @param cy     中心Y坐标
     * @param cz     中心Z坐标
     * @param width  宽度（X方向）
     * @param height 高度（Y方向）
     * @param depth  深度（Z方向）
     * @return 8个顶点的坐标数组
     */
    private double[][] createBox(double cx, double cy, double cz,
            double width, double height, double depth) {
        double w = width / 2; // 半宽
        double h = height / 2; // 半高
        double d = depth / 2; // 半深

        return new double[][] {
                // 前面四个顶点 (z正方向)
                { cx - w, cy - h, cz + d }, // 0: 左下前
                { cx + w, cy - h, cz + d }, // 1: 右下前
                { cx + w, cy + h, cz + d }, // 2: 右上前
                { cx - w, cy + h, cz + d }, // 3: 左上前
                // 后面四个顶点 (z负方向)
                { cx - w, cy - h, cz - d }, // 4: 左下后
                { cx + w, cy - h, cz - d }, // 5: 右下后
                { cx + w, cy + h, cz - d }, // 6: 右上后
                { cx - w, cy + h, cz - d } // 7: 左上后
        };
    }

    // ==================== 多边形获取（核心渲染方法） ====================

    /**
     * 获取所有变换后的多边形
     * 这是渲染机器人的核心方法，会被Scene3DPanel调用
     * 
     * 【处理流程】
     * 1. 计算机器人整体的世界变换矩阵
     * 2. 对每个部件，计算其局部变换（关节旋转）
     * 3. 将局部变换与世界变换组合
     * 4. 应用变换到部件顶点，生成多边形
     * 
     * @return 所有部件的多边形列表
     */
    public List<Polygon3D> getTransformedPolygons() {
        List<Polygon3D> allPolygons = new ArrayList<>();

        // ========== 1. 计算世界变换矩阵 ==========
        // 先平移到世界位置，再绕Y轴旋转
        double[][] worldMatrix = Matrix4.identity();
        worldMatrix = Matrix4.multiply(Matrix4.translate(posX, posY, posZ), worldMatrix);
        worldMatrix = Matrix4.multiply(Matrix4.rotateY(Math.toRadians(rotY)), worldMatrix);

        // ========== 2. 处理自定义模型 ==========
        if (customParts != null && !customParts.isEmpty()) {
            for (Polygon3D poly : customParts) {
                double[][] transformedVerts = new double[poly.vertices.length][3];
                for (int i = 0; i < poly.vertices.length; i++) {
                    double[] v = poly.vertices[i];
                    double[] t = Matrix4.transformPoint(worldMatrix, v);
                    transformedVerts[i] = new double[] { t[0], t[1], t[2] };
                }
                allPolygons.add(new Polygon3D(transformedVerts, poly.color));
            }
            return allPolygons;
        }

        // ========== 3. 处理标准机器人各部件 ==========

        // 躯干：直接使用世界矩阵
        allPolygons.addAll(body.getPolygons(worldMatrix));

        // 头部：在世界矩阵基础上，先平移到头部位置，再绕Y轴旋转
        double[][] headMatrix = Matrix4.multiply(worldMatrix,
                Matrix4.multiply(Matrix4.translate(0, 0.85, 0), Matrix4.rotateY(Math.toRadians(headRotY))));
        allPolygons.addAll(transformPart(head, headMatrix));

        // 左臂：关节在肩部，需要先移动到关节位置，旋转，再移回
        double[][] leftArmMatrix = Matrix4.multiply(worldMatrix,
                Matrix4.translate(-0.525, 0.5, 0)); // 移到肩关节
        leftArmMatrix = Matrix4.multiply(leftArmMatrix, Matrix4.rotateX(Math.toRadians(leftArmRotX))); // 前后摆动
        leftArmMatrix = Matrix4.multiply(leftArmMatrix, Matrix4.rotateZ(Math.toRadians(leftArmRotZ))); // 侧向摆动
        leftArmMatrix = Matrix4.multiply(leftArmMatrix, Matrix4.translate(0, -0.4, 0)); // 手臂中心偏移
        allPolygons.addAll(transformPart(leftArm, leftArmMatrix));

        // 右臂
        double[][] rightArmMatrix = Matrix4.multiply(worldMatrix,
                Matrix4.translate(0.525, 0.5, 0));
        rightArmMatrix = Matrix4.multiply(rightArmMatrix, Matrix4.rotateX(Math.toRadians(rightArmRotX)));
        rightArmMatrix = Matrix4.multiply(rightArmMatrix, Matrix4.rotateZ(Math.toRadians(rightArmRotZ)));
        rightArmMatrix = Matrix4.multiply(rightArmMatrix, Matrix4.translate(0, -0.4, 0));
        allPolygons.addAll(transformPart(rightArm, rightArmMatrix));

        // 左腿：关节在髋部
        double[][] leftLegMatrix = Matrix4.multiply(worldMatrix,
                Matrix4.translate(-0.2, -0.6, 0)); // 移到髋关节
        leftLegMatrix = Matrix4.multiply(leftLegMatrix, Matrix4.rotateX(Math.toRadians(leftLegRotX))); // 前后摆动
        leftLegMatrix = Matrix4.multiply(leftLegMatrix, Matrix4.translate(0, -0.45, 0)); // 腿中心偏移
        allPolygons.addAll(transformPart(leftLeg, leftLegMatrix));

        // 右腿
        double[][] rightLegMatrix = Matrix4.multiply(worldMatrix,
                Matrix4.translate(0.2, -0.6, 0));
        rightLegMatrix = Matrix4.multiply(rightLegMatrix, Matrix4.rotateX(Math.toRadians(rightLegRotX)));
        rightLegMatrix = Matrix4.multiply(rightLegMatrix, Matrix4.translate(0, -0.45, 0));
        allPolygons.addAll(transformPart(rightLeg, rightLegMatrix));

        return allPolygons;
    }

    /**
     * 将部件顶点应用变换矩阵，生成多边形列表
     * 
     * @param part   机器人部件
     * @param matrix 变换矩阵
     * @return 该部件的6个面（长方体有6个面）
     */
    private List<Polygon3D> transformPart(RobotPart part, double[][] matrix) {
        List<Polygon3D> polygons = new ArrayList<>();
        double[][] transformedVertices = new double[part.vertices.length][3];

        // 对每个顶点应用变换
        for (int i = 0; i < part.vertices.length; i++) {
            double[] v = part.vertices[i];
            double[] transformed = Matrix4.transformPoint(matrix, v);
            transformedVertices[i] = new double[] { transformed[0], transformed[1], transformed[2] };
        }

        // 定义长方体的6个面（每个面4个顶点索引）
        int[][] faces = {
                { 0, 1, 2, 3 }, // 前面
                { 5, 4, 7, 6 }, // 后面
                { 4, 0, 3, 7 }, // 左面
                { 1, 5, 6, 2 }, // 右面
                { 3, 2, 6, 7 }, // 顶面
                { 4, 5, 1, 0 } // 底面
        };

        // 为每个面创建多边形
        for (int[] face : faces) {
            double[][] faceVerts = new double[4][3];
            for (int i = 0; i < 4; i++) {
                faceVerts[i] = transformedVertices[face[i]];
            }
            polygons.add(new Polygon3D(faceVerts, part.color));
        }

        return polygons;
    }

    // ==================== 动画方法 ====================

    /**
     * 重置机器人姿态到默认状态
     */
    public void resetPose() {
        headRotY = 0;
        leftArmRotX = 0;
        leftArmRotZ = 0;
        rightArmRotX = 0;
        rightArmRotZ = 0;
        leftLegRotX = 0;
        rightLegRotX = 0;
        animationTime = 0;
        isAnimating = false;
    }

    /**
     * 头部摇头动画
     * 使用正弦函数产生平滑的左右摆动
     */
    public void animateHead() {
        new Thread(() -> {
            for (int i = 0; i < 60; i++) {
                headRotY = 30 * Math.sin(i * Math.PI / 15); // 振幅30度
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                }
            }
            headRotY = 0;
        }).start();
    }

    /**
     * 手臂摆动动画
     * 左右手臂反向摆动
     */
    public void animateArms() {
        new Thread(() -> {
            for (int i = 0; i < 120; i++) {
                double t = i * Math.PI / 30;
                leftArmRotX = 45 * Math.sin(t); // 左臂
                rightArmRotX = -45 * Math.sin(t); // 右臂反向
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                }
            }
            leftArmRotX = 0;
            rightArmRotX = 0;
        }).start();
    }

    /**
     * 腿部摆动动画
     */
    public void animateLegs() {
        new Thread(() -> {
            for (int i = 0; i < 120; i++) {
                double t = i * Math.PI / 30;
                leftLegRotX = 30 * Math.sin(t);
                rightLegRotX = -30 * Math.sin(t);
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                }
            }
            leftLegRotX = 0;
            rightLegRotX = 0;
        }).start();
    }

    /**
     * 行走动画
     * 腿部和手臂协调摆动，同时向前移动
     */
    public void walkAnimation() {
        new Thread(() -> {
            isAnimating = true;
            for (int i = 0; i < 180 && isAnimating; i++) {
                double t = i * Math.PI / 20;
                // 腿部摆动
                leftLegRotX = 35 * Math.sin(t);
                rightLegRotX = -35 * Math.sin(t);
                // 手臂反向摆动（自然行走姿态）
                leftArmRotX = -25 * Math.sin(t);
                rightArmRotX = 25 * Math.sin(t);
                // 向前移动
                posZ -= 0.02;
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                }
            }
            resetPose();
        }).start();
    }

    /**
     * 挥手动画
     * 先举起右臂，然后挥动，最后放下
     */
    public void waveAnimation() {
        new Thread(() -> {
            // 阶段1：举起右臂
            for (int i = 0; i < 30; i++) {
                rightArmRotZ = -i * 4; // 向上抬起
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
            // 阶段2：挥手
            for (int i = 0; i < 90; i++) {
                rightArmRotX = 20 * Math.sin(i * Math.PI / 10);
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                }
            }
            // 阶段3：放下手臂
            for (int i = 30; i >= 0; i--) {
                rightArmRotZ = -i * 4;
                rightArmRotX = 0;
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
            rightArmRotZ = 0;
        }).start();
    }

    /**
     * 停止当前动画
     */
    public void stopAnimation() {
        isAnimating = false;
    }

    // ==================== Getter/Setter 方法 ====================

    /**
     * 设置机器人在世界坐标系中的位置
     */
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    /**
     * 设置机器人的朝向（绕Y轴旋转角度）
     */
    public void setRotation(double rotY) {
        this.rotY = rotY;
    }

    /**
     * 相对平移机器人
     */
    public void translate(double dx, double dy, double dz) {
        this.posX += dx;
        this.posY += dy;
        this.posZ += dz;
    }

    /**
     * 相对旋转机器人
     */
    public void rotate(double dRotY) {
        this.rotY += dRotY;
    }

    /**
     * 设置躯干颜色
     */
    public void setBodyColor(Color color) {
        this.bodyColor = color;
        body.color = color;
    }

    /**
     * 设置四肢颜色
     */
    public void setLimbColor(Color color) {
        this.limbColor = color;
        leftArm.color = color;
        rightArm.color = color;
        leftLeg.color = color;
        rightLeg.color = color;
    }

    // ==================== 内部类 ====================

    /**
     * 机器人部件类
     * 每个部件包含名称、顶点数据和颜色
     */
    private static class RobotPart {
        String name; // 部件名称
        double[][] vertices; // 顶点坐标（8个顶点）
        Color color; // 部件颜色

        RobotPart(String name, double[][] vertices, Color color) {
            this.name = name;
            this.vertices = vertices;
            this.color = color;
        }

        /**
         * 获取该部件的多边形列表（应用变换矩阵后）
         */
        List<Polygon3D> getPolygons(double[][] matrix) {
            List<Polygon3D> polygons = new ArrayList<>();
            double[][] transformedVertices = new double[vertices.length][3];

            // 变换所有顶点
            for (int i = 0; i < vertices.length; i++) {
                double[] v = vertices[i];
                double[] transformed = Matrix4.transformPoint(matrix, v);
                transformedVertices[i] = new double[] { transformed[0], transformed[1], transformed[2] };
            }

            // 长方体的6个面
            int[][] faces = {
                    { 0, 1, 2, 3 }, { 5, 4, 7, 6 }, { 4, 0, 3, 7 },
                    { 1, 5, 6, 2 }, { 3, 2, 6, 7 }, { 4, 5, 1, 0 }
            };

            for (int[] face : faces) {
                double[][] faceVerts = new double[4][3];
                for (int i = 0; i < 4; i++) {
                    faceVerts[i] = transformedVertices[face[i]];
                }
                polygons.add(new Polygon3D(faceVerts, color));
            }

            return polygons;
        }
    }
}
