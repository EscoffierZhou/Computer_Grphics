package com.graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * Stage.java - 舞台/地板类
 * ====================================================================
 * 
 * 【功能说明】
 * 这个类负责创建3D场景中的地板。
 * 地板使用棋盘格图案，让用户更容易感知3D空间的深度和透视效果。
 * 
 * 【设计思路】
 * 1. 地板是一个大的平面，位于机器人的脚下
 * 2. 使用8x8的棋盘格，交替使用两种颜色
 * 3. 每个格子是一个四边形（Polygon3D）
 * 
 * 【坐标系说明】
 * - X轴: 左右方向
 * - Y轴: 上下方向（地板在Y=-1.7的位置）
 * - Z轴: 前后方向
 * 
 * @author Computer Graphics Course
 */
public class Stage {

    // ==================== 属性 ====================

    /**
     * 存储组成地板的所有多边形
     * 每个格子是一个Polygon3D对象
     */
    private List<Polygon3D> polygons = new ArrayList<>();

    /**
     * 舞台宽度（X方向），以原点为中心
     * 实际范围是 -stageWidth/2 到 +stageWidth/2
     */
    private double stageWidth = 16;

    /**
     * 舞台深度（Z方向）
     */
    private double stageDepth = 16;

    /**
     * 地板的主色（棋盘格的深色格子）
     */
    private Color floorColor = new Color(50, 50, 60); // 深灰色

    /**
     * 地板的次色（棋盘格的浅色格子）
     */
    private Color gridColor = new Color(35, 35, 45); // 更深的灰色

    // ==================== 构造函数 ====================

    /**
     * 构造函数 - 创建舞台
     * 自动构建棋盘格地板
     */
    public Stage() {
        buildStage();
    }

    // ==================== 私有方法 ====================

    /**
     * 构建舞台
     * 
     * 【算法步骤】
     * 1. 清除旧的多边形
     * 2. 计算每个格子的大小
     * 3. 用双重循环创建8x8=64个格子
     * 4. 每个格子根据位置(i+j)决定颜色（奇偶交替）
     */
    private void buildStage() {
        // 清除之前的多边形
        polygons.clear();

        // 地板的Y坐标 - 位于机器人脚底下方
        // 机器人脚底约在Y=-1.65，所以地板放在Y=-1.7
        double floorY = -1.7;

        // 计算舞台的边界
        double halfWidth = stageWidth / 2; // 半宽
        double halfDepth = stageDepth / 2; // 半深

        // 棋盘格的格子数量
        int gridSize = 8; // 8x8的棋盘

        // 每个格子的尺寸
        double cellWidth = stageWidth / gridSize;
        double cellDepth = stageDepth / gridSize;

        // 双重循环创建每个格子
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {

                // 计算当前格子的四个角的坐标
                // 左下角
                double x1 = -halfWidth + i * cellWidth;
                double z1 = -halfDepth + j * cellDepth;
                // 右上角
                double x2 = x1 + cellWidth;
                double z2 = z1 + cellDepth;

                // 根据(i+j)的奇偶性选择颜色，形成棋盘图案
                // (0,0)用floorColor, (0,1)用gridColor, (1,0)用gridColor...
                Color cellColor = (i + j) % 2 == 0 ? floorColor : gridColor;

                // 创建格子的四个顶点（逆时针顺序）
                // 从上往下看:
                // (x1,z2) ---- (x2,z2)
                // | |
                // (x1,z1) ---- (x2,z1)
                double[][] vertices = {
                        { x1, floorY, z1 }, // 左下
                        { x2, floorY, z1 }, // 右下
                        { x2, floorY, z2 }, // 右上
                        { x1, floorY, z2 } // 左上
                };

                // 创建多边形对象
                Polygon3D floor = new Polygon3D(vertices, cellColor);

                // 设置为双面渲染，这样从地板下方也能看到
                floor.doubleSided = true;

                // 添加到多边形列表
                polygons.add(floor);
            }
        }
    }

    // ==================== 公共方法 ====================

    /**
     * 获取所有地板多边形
     * Scene3DPanel会调用此方法来获取需要渲染的多边形
     * 
     * @return 多边形列表
     */
    public List<Polygon3D> getPolygons() {
        return polygons;
    }

    /**
     * 设置地板主色
     * 会立即重建地板以应用新颜色
     * 
     * @param color 新的主色
     */
    public void setFloorColor(Color color) {
        this.floorColor = color;
        buildStage(); // 重新构建以应用新颜色
    }

    /**
     * 设置地板次色
     * 会立即重建地板以应用新颜色
     * 
     * @param color 新的次色
     */
    public void setGridColor(Color color) {
        this.gridColor = color;
        buildStage(); // 重新构建以应用新颜色
    }
}
