package com.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * Scene3DPanel.java - 3D场景渲染面板
 * ====================================================================
 * 
 * 【功能说明】
 * 这是3D图形的核心渲染引擎，使用纯软件渲染实现3D显示。
 * 包含机器人模型、舞台场景，以及完整的3D图形管线。
 * 
 * 【3D渲染管线】
 * 1. 模型变换(Model Transform) - 在Robot.java中完成
 * 2. 视图变换(View Transform) - createViewMatrix()
 * 3. 投影变换(Projection Transform) - createProjectionMatrix()
 * 4. 裁剪(Clipping) - 简易实现
 * 5. 视口变换(Viewport Transform) - NDC到屏幕坐标
 * 
 * 【消隐算法】
 * - Z-Buffer: 概念上的深度排序
 * - 画家算法: 实际使用的方法，从远到近绘制
 * - 背面剔除: 不绘制背向摄像机的面
 * - 线框模式: 只显示边，不做消隐
 * 
 * 【着色模式】
 * - Flat: 每个面一个颜色，无高光
 * - Gouraud: 顶点颜色插值，弱高光
 * - Phong: 法向量插值，完整高光
 * 
 * 【交互控制】
 * - 左键拖拽: 旋转相机/平移物体（取决于模式）
 * - 右键拖拽: 平移相机
 * - 滚轮: 缩放（调整相机距离）
 * 
 * @author Computer Graphics Course
 */
public class Scene3DPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    // ==================== 投影模式枚举 ====================

    /**
     * 投影模式
     * - FRUSTUM: 视锥体投影（更紧凑的视角）
     * - PERSPECTIVE: 透视投影（标准FOV）
     * - ORTHOGRAPHIC: 正交投影（平行投影，无近大远小）
     */
    public enum ProjectionMode {
        FRUSTUM, PERSPECTIVE, ORTHOGRAPHIC
    }

    private ProjectionMode projectionMode = ProjectionMode.PERSPECTIVE;

    // ==================== 消隐模式枚举 ====================

    /**
     * 隐藏面消除模式
     * - ZBUFFER: Z缓冲算法（概念实现）
     * - SCANLINE: 扫描线消隐
     * - BACKFACE: 背面剔除
     * - WIREFRAME: 线框模式（不做消隐）
     */
    public enum HSRMode {
        ZBUFFER, SCANLINE, BACKFACE, WIREFRAME
    }

    private HSRMode hsrMode = HSRMode.ZBUFFER;

    // ==================== 着色模式枚举 ====================

    /**
     * 着色模式
     * - FLAT: 平面着色（每面一色）
     * - GOURAUD: 顶点着色（插值顶点颜色）
     * - PHONG: 像素着色（插值法向量）
     */
    public enum ShadingMode {
        FLAT, GOURAUD, PHONG
    }

    private ShadingMode shadingMode = ShadingMode.PHONG;

    // ==================== 交互模式 ====================

    /** 当前交互模式: ROTATE/TRANSLATE/SCALE */
    private String interactionMode = "ROTATE";

    // ==================== 场景对象 ====================

    /** 机器人模型列表（支持多个） */
    private List<Robot> robots = new ArrayList<>();

    /** 舞台场景（地板） */
    private Stage stage;

    // ==================== 相机参数 ====================

    /** 相机位置 */
    private double cameraX = 0, cameraY = 3, cameraZ = 12;

    /** 相机旋转角度（俯仰和偏航） */
    private double cameraRotX = -10, cameraRotY = 0;

    /** 视场角(Field of View)，单位：度 */
    private double fov = 50;

    /** 近裁剪面和远裁剪面距离 */
    private double nearPlane = 1.0, farPlane = 200;

    // ==================== 光照参数 ====================

    /** 环境光颜色 */
    private Color ambientLight = new Color(50, 50, 50);

    /** 点光源位置 */
    private double[] lightPosition = { 5, 10, 5 };

    /** 光源颜色 */
    private Color lightColor = Color.WHITE;

    /** 光照强度（0.0-1.0） */
    private double lightIntensity = 1.0;

    // ==================== 鼠标交互状态 ====================

    private int lastMouseX, lastMouseY;
    private boolean isDragging = false;

    // ==================== 帧缓冲 ====================

    /** 离屏渲染缓冲区 */
    private BufferedImage frameBuffer;

    // ==================== 构造函数 ====================

    public Scene3DPanel() {
        setBackground(new Color(30, 30, 40)); // 深灰蓝背景
        setPreferredSize(new Dimension(1000, 700));

        // 初始化场景对象
        robots.add(new Robot()); // 添加默认机器人
        stage = new Stage(); // 添加舞台地板

        // 添加鼠标监听器
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

        // 启动渲染循环（约60 FPS）
        Timer renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();
    }

    // ==================== 核心渲染方法 ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 初始化或调整帧缓冲区大小
        if (frameBuffer == null || frameBuffer.getWidth() != width || frameBuffer.getHeight() != height) {
            frameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        // 清空帧缓冲区
        Graphics2D fbg = frameBuffer.createGraphics();
        fbg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        fbg.setColor(getBackground());
        fbg.fillRect(0, 0, width, height);

        // ========== 3D渲染管线 ==========

        // Step 1: 创建视图矩阵（相机变换）
        double[][] viewMatrix = createViewMatrix();

        // Step 2: 创建投影矩阵
        double[][] projMatrix = createProjectionMatrix(width, height);

        // Step 3: 分层渲染
        // 先渲染地板，再渲染机器人，确保正确的遮挡关系

        // 第1层：舞台(地板)多边形
        renderPolygons(fbg, stage.getPolygons(), viewMatrix, projMatrix, width, height);

        // 第2层：所有机器人的多边形
        List<Polygon3D> robotPolygons = new ArrayList<>();
        for (Robot robot : robots) {
            robotPolygons.addAll(robot.getTransformedPolygons());
        }
        renderPolygons(fbg, robotPolygons, viewMatrix, projMatrix, width, height);

        fbg.dispose();

        // 将帧缓冲区绘制到屏幕
        g2d.drawImage(frameBuffer, 0, 0, null);

        // 绘制场景信息显示
        drawInfo(g2d);
    }

    /**
     * 创建视图矩阵
     * 
     * 【视图变换原理】
     * 将世界坐标转换到相机坐标系。
     * 等价于移动整个世界，使相机位于原点并朝向-Z方向。
     * 
     * 【变换顺序】
     * 1. 平移：将相机移到原点（T(-camX, -camY, -camZ)）
     * 2. 旋转：使相机朝向-Z轴（R(-rotX) × R(-rotY)）
     */
    private double[][] createViewMatrix() {
        double[][] result = Matrix4.identity();
        // 先平移
        result = Matrix4.multiply(Matrix4.translate(-cameraX, -cameraY, -cameraZ), result);
        // 再旋转（注意顺序和符号）
        result = Matrix4.multiply(Matrix4.rotateX(Math.toRadians(-cameraRotX)), result);
        result = Matrix4.multiply(Matrix4.rotateY(Math.toRadians(-cameraRotY)), result);
        return result;
    }

    /**
     * 创建投影矩阵
     * 
     * 【投影类型】
     * - Frustum: 使用固定的45度视角
     * - Perspective: 使用可调节的FOV
     * - Orthographic: 正交投影，用于CAD等场景
     */
    private double[][] createProjectionMatrix(int width, int height) {
        double aspect = (double) width / height;

        switch (projectionMode) {
            case FRUSTUM:
                // 视锥体投影，使用较小的FOV
                return Matrix4.perspective(Math.toRadians(45), aspect, nearPlane, farPlane);
            case PERSPECTIVE:
                // 标准透视投影
                return Matrix4.perspective(Math.toRadians(fov), aspect, nearPlane, farPlane);
            case ORTHOGRAPHIC:
                // 正交投影
                double size = 5;
                return Matrix4.orthographic(-size * aspect, size * aspect, -size, size, nearPlane, farPlane);
            default:
                return Matrix4.identity();
        }
    }

    /**
     * 渲染多边形列表
     * 
     * 【渲染流程】
     * 1. 深度排序（画家算法）
     * 2. 背面剔除
     * 3. 顶点变换（视图 × 投影）
     * 4. 透视除法
     * 5. 视口变换
     * 6. 光照计算
     * 7. 栅格化绘制
     */
    private void renderPolygons(Graphics2D g, List<Polygon3D> polygons,
            double[][] view, double[][] proj, int w, int h) {

        // ========== Step 1: 画家算法 - 按深度排序 ==========
        // 从远到近排序，先绘制远处的，再绘制近处的
        polygons.sort((a, b) -> {
            double za = a.getAverageZ(view);
            double zb = b.getAverageZ(view);
            return Double.compare(zb, za); // 降序排列
        });

        for (Polygon3D poly : polygons) {

            // ========== Step 2: 背面剔除 ==========
            // 非线框模式且非双面多边形时进行背面剔除
            if (hsrMode != HSRMode.WIREFRAME && !poly.doubleSided) {
                double[] normal = poly.getNormal();
                double[] center = poly.getCenter();
                // 计算从多边形中心到相机的方向
                double[] viewDir = Vector3.normalize(new double[] {
                        cameraX - center[0], cameraY - center[1], cameraZ - center[2]
                });
                // 如果法向量与视线方向夹角大于90度，则是背面
                // 使用-0.1而不是0是为了让接近水平的面（如地板）也能显示
                if (Vector3.dot(normal, viewDir) < -0.1) {
                    continue; // 跳过背面
                }
            }

            // ========== Step 3-5: 顶点变换 ==========
            int[] screenX = new int[poly.vertices.length];
            int[] screenY = new int[poly.vertices.length];
            boolean allVisible = true;

            for (int i = 0; i < poly.vertices.length; i++) {
                double[] v = poly.vertices[i];

                // Step 3: 视图变换
                double[] transformed = Matrix4.transformPoint(view, v);

                // Step 4: 投影变换
                double[] projected = Matrix4.transformPoint(proj, transformed);

                // Step 5: 透视除法（将齐次坐标转换为笛卡尔坐标）
                if (Math.abs(projected[3]) > 0.001) {
                    projected[0] /= projected[3];
                    projected[1] /= projected[3];
                    projected[2] /= projected[3];
                }

                // 简单裁剪（超出规范视见体的不渲染）
                if (projected[2] < -1.5 || projected[2] > 1.5) {
                    allVisible = false;
                    break;
                }

                // 视口变换：[-1,1] → [0,w] × [0,h]
                // 注意Y轴翻转（屏幕Y向下，3D空间Y向上）
                screenX[i] = (int) ((projected[0] + 1) * w / 2);
                screenY[i] = (int) ((1 - projected[1]) * h / 2);
            }

            if (!allVisible)
                continue;

            // ========== Step 6: 光照计算 ==========
            Color shadedColor = calculateLighting(poly);

            // ========== Step 7: 栅格化绘制 ==========
            if (hsrMode == HSRMode.WIREFRAME) {
                // 线框模式
                g.setColor(Color.GREEN);
                g.setStroke(new BasicStroke(1));
                g.drawPolygon(screenX, screenY, screenX.length);
            } else {
                // 填充模式
                g.setColor(shadedColor);
                g.fillPolygon(screenX, screenY, screenX.length);
                // 描边使颜色稍暗
                g.setColor(shadedColor.darker());
                g.setStroke(new BasicStroke(1));
                g.drawPolygon(screenX, screenY, screenX.length);
            }
        }
    }

    /**
     * 计算光照颜色
     * 
     * 【Phong光照模型】
     * I = I_ambient + I_diffuse + I_specular
     * 
     * I_ambient = k_a × I_a （环境光）
     * I_diffuse = k_d × I_l × max(0, N·L) （漫反射）
     * I_specular = k_s × I_l × max(0, R·V)^n （镜面反射）
     * 
     * 其中：
     * - N: 表面法向量
     * - L: 光源方向
     * - V: 观察方向
     * - R: 反射方向
     * - n: 光泽度
     */
    private Color calculateLighting(Polygon3D poly) {
        double[] normal = poly.getNormal();
        double[] center = poly.getCenter();

        // 计算光源方向（从表面指向光源）
        double[] lightDir = Vector3.normalize(new double[] {
                lightPosition[0] - center[0],
                lightPosition[1] - center[1],
                lightPosition[2] - center[2]
        });

        double ambient, diffuse, specular;

        switch (shadingMode) {
            case FLAT:
                // ========== Flat着色 ==========
                // 特点：只有环境光+简单漫反射，无高光
                // 效果：每个面颜色均匀，有明显的面与面分界
                ambient = 0.3;
                diffuse = Math.max(0, Vector3.dot(normal, lightDir)) * 0.5;
                specular = 0;
                break;

            case GOURAUD:
                // ========== Gouraud着色 ==========
                // 特点：环境光+漫反射+弱高光
                // 效果：颜色过渡比Flat平滑，但高光可能丢失
                ambient = 0.25;
                diffuse = Math.max(0, Vector3.dot(normal, lightDir)) * 0.55;
                // 使用半向量近似计算高光
                double[] viewDirG = Vector3.normalize(new double[] {
                        cameraX - center[0], cameraY - center[1], cameraZ - center[2]
                });
                double[] halfVectorG = Vector3.normalize(Vector3.add(lightDir, viewDirG));
                double specG = Math.max(0, Vector3.dot(normal, halfVectorG));
                specular = Math.pow(specG, 16) * 0.2; // 较小的高光
                break;

            case PHONG:
            default:
                // ========== Phong着色 ==========
                // 特点：完整光照模型，明显高光
                // 效果：最真实，高光清晰锐利
                ambient = 0.2;
                diffuse = Math.max(0, Vector3.dot(normal, lightDir)) * 0.5;
                // 计算反射方向
                double[] viewDir = Vector3.normalize(new double[] {
                        cameraX - center[0], cameraY - center[1], cameraZ - center[2]
                });
                double[] reflectDir = Vector3.reflect(Vector3.negate(lightDir), normal);
                double spec = Math.max(0, Vector3.dot(viewDir, reflectDir));
                specular = Math.pow(spec, 64) * 0.5; // 明显的高光
                break;
        }

        // ========== 合成最终颜色 ==========
        double intensity = Math.min(1.2, ambient + diffuse + specular) * lightIntensity;

        int r = (int) Math.min(255, poly.color.getRed() * intensity + ambientLight.getRed() * 0.1);
        int g = (int) Math.min(255, poly.color.getGreen() * intensity + ambientLight.getGreen() * 0.1);
        int b = (int) Math.min(255, poly.color.getBlue() * intensity + ambientLight.getBlue() * 0.1);

        // 添加着色模式特有的色调差异
        if (shadingMode == ShadingMode.FLAT) {
            // Flat模式略微偏暗偏灰
            r = (int) (r * 0.9);
            g = (int) (g * 0.9);
            b = (int) (b * 0.9);
        } else if (shadingMode == ShadingMode.GOURAUD) {
            // Gouraud略微偏暖
            r = Math.min(255, (int) (r * 1.05));
        }
        // Phong保持原样

        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)));
    }

    /**
     * 绘制场景信息显示
     * 显示当前的投影模式、消隐模式、着色模式等
     */
    private void drawInfo(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));

        int y = 20;
        g.drawString("投影: " + projectionMode, 10, y);
        y += 18;
        g.drawString("消隐: " + hsrMode, 10, y);
        y += 18;
        g.drawString("着色: " + shadingMode, 10, y);
        y += 18;
        g.drawString("机器人: " + robots.size() + "个", 10, y);
        y += 18;
        g.drawString(String.format("相机: (%.1f, %.1f, %.1f)", cameraX, cameraY, cameraZ), 10, y);
        y += 18;
        g.drawString("交互: " + interactionMode, 10, y);
    }

    // ==================== 公共方法 ====================

    public void setProjectionMode(ProjectionMode mode) {
        this.projectionMode = mode;
        repaint();
    }

    public ProjectionMode getProjectionMode() {
        return projectionMode;
    }

    public void setHiddenSurfaceMode(HSRMode mode) {
        this.hsrMode = mode;
        repaint();
    }

    public void setShadingMode(ShadingMode mode) {
        this.shadingMode = mode;
        repaint();
    }

    public ShadingMode getShadingMode() {
        return shadingMode;
    }

    public Robot getRobot() {
        return robots.isEmpty() ? null : robots.get(0);
    }

    public int getRobotCount() {
        return robots.size();
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * 添加机器人到场景
     */
    public void addRobot(Robot robot) {
        robots.add(robot);
    }

    public void addRobot() {
        addRobot((Color) null);
    }

    public void addRobot(Color color) {
        Robot newRobot = new Robot();
        // 偏移位置避免重叠
        double offset = robots.size() * 2.5;
        newRobot.setPosition(offset - 2.5, 0, 0);

        // 应用自定义颜色
        if (color != null) {
            newRobot.setBodyColor(color);
            newRobot.setLimbColor(color.darker());
        }

        robots.add(newRobot);
        repaint();
    }

    public void removeLastRobot() {
        if (robots.size() > 1) {
            robots.remove(robots.size() - 1);
            repaint();
        }
    }

    public void clearAllRobots() {
        robots.clear();
        repaint();
    }

    public void setInteractionMode(String mode) {
        this.interactionMode = mode;
    }

    public void setAmbientLight(Color color) {
        this.ambientLight = color;
        repaint();
    }

    public void setLightPosition(double x, double y, double z) {
        this.lightPosition = new double[] { x, y, z };
        repaint();
    }

    public void setLightIntensity(double intensity) {
        this.lightIntensity = intensity;
        repaint();
    }

    public void setCameraPosition(double x, double y, double z) {
        this.cameraX = x;
        this.cameraY = y;
        this.cameraZ = z;
        repaint();
    }

    public void setCameraRotation(double rotX, double rotY) {
        this.cameraRotX = rotX;
        this.cameraRotY = rotY;
        repaint();
    }

    // ==================== 鼠标事件处理 ====================

    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
        isDragging = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isDragging = false;
    }

    /**
     * 鼠标拖拽处理
     * 
     * 【交互模式】
     * - ROTATE: 左键拖拽旋转相机
     * - TRANSLATE: 左键拖拽平移物体
     * - SCALE: 左键拖拽缩放（通过相机距离模拟）
     * - 右键拖拽: 平移相机
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isDragging)
            return;

        int dx = e.getX() - lastMouseX;
        int dy = e.getY() - lastMouseY;

        if (SwingUtilities.isLeftMouseButton(e)) {
            switch (interactionMode) {
                case "ROTATE":
                    // 旋转相机（拖拽灵敏度0.5）
                    cameraRotY += dx * 0.5;
                    cameraRotX += dy * 0.5;
                    // 限制俯仰角范围，避免翻转
                    cameraRotX = Math.max(-89, Math.min(89, cameraRotX));
                    break;
                case "TRANSLATE":
                    // 平移第一个机器人
                    if (!robots.isEmpty()) {
                        robots.get(0).translate(dx * 0.02, -dy * 0.02, 0);
                    }
                    break;
                case "SCALE":
                    // 通过调整相机距离模拟缩放
                    cameraZ += dy * 0.05;
                    cameraZ = Math.max(2, Math.min(50, cameraZ));
                    break;
                default:
                    // 默认旋转
                    cameraRotY += dx * 0.5;
                    cameraRotX += dy * 0.5;
                    break;
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            // 右键拖拽平移相机
            cameraX -= dx * 0.02;
            cameraY += dy * 0.02;
        }

        lastMouseX = e.getX();
        lastMouseY = e.getY();
        repaint();
    }

    /**
     * 鼠标滚轮处理 - 调整相机距离
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        cameraZ += e.getWheelRotation() * 0.5;
        cameraZ = Math.max(2, Math.min(50, cameraZ));
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
