package com.graphics;

import java.awt.Color;

/**
 * ====================================================================
 * Polygon3D.java - 3D多边形类
 * ====================================================================
 * 
 * 【功能说明】
 * 这个类用于表示3D空间中的一个多边形面（如三角形、四边形）。
 * 在3D图形学中，所有的3D物体都是由多个多边形面组成的。
 * 例如：一个立方体由6个正方形面组成，每个面就是一个Polygon3D对象。
 * 
 * 【核心属性】
 * - vertices: 顶点数组，存储多边形的每个顶点的(x,y,z)坐标
 * - color: 多边形的颜色
 * - doubleSided: 是否双面渲染（地板需要双面可见）
 * 
 * 【重要概念】
 * - 法向量(Normal): 垂直于多边形表面的向量，用于计算光照和判断正反面
 * - 中心点(Center): 多边形所有顶点的平均位置，用于深度排序
 * 
 * @author Computer Graphics Course
 */
public class Polygon3D {

    // ==================== 公共属性 ====================

    /**
     * 顶点数组 - 存储多边形的所有顶点坐标
     * 格式: vertices[顶点索引][坐标分量]
     * 例如: vertices[0] = {x, y, z} 表示第一个顶点的坐标
     * 
     * 一个三角形有3个顶点: vertices.length = 3
     * 一个四边形有4个顶点: vertices.length = 4
     */
    public double[][] vertices;

    /**
     * 多边形的颜色
     * 这个颜色会被光照计算影响，最终显示可能会变亮或变暗
     */
    public Color color;

    /**
     * 双面渲染标志
     * - false(默认): 只渲染正面，背面不可见（如机器人的部件）
     * - true: 正反两面都渲染（如地板，从上面和下面都能看到）
     */
    public boolean doubleSided = false;

    // ==================== 私有属性(缓存) ====================

    // 缓存计算结果，避免重复计算
    private double[] normal = null; // 法向量缓存
    private double[] center = null; // 中心点缓存

    // ==================== 构造函数 ====================

    /**
     * 构造函数 - 创建一个3D多边形
     * 
     * @param vertices 顶点坐标数组，格式为 double[顶点数][3]
     *                 例如: new double[][]{{0,0,0}, {1,0,0}, {1,1,0}, {0,1,0}}
     *                 表示XY平面上的一个正方形
     * @param color    多边形的颜色
     */
    public Polygon3D(double[][] vertices, Color color) {
        this.vertices = vertices;
        this.color = color;
    }

    // ==================== 公共方法 ====================

    /**
     * 计算多边形的法向量
     * 
     * 【什么是法向量？】
     * 法向量是垂直于平面的向量，指向平面的"正面"方向。
     * 
     * 【计算方法】
     * 使用叉积公式: N = (V1 - V0) × (V2 - V0)
     * 其中V0, V1, V2是多边形的前三个顶点
     * 
     * 【用途】
     * 1. 判断多边形的正反面（背面消除）
     * 2. 计算光照强度（光线与法向量的夹角）
     * 
     * @return 归一化的法向量 [x, y, z]，长度为1
     */
    public double[] getNormal() {
        // 如果还没计算过，且至少有3个顶点
        if (normal == null && vertices.length >= 3) {
            // 计算两条边的向量
            double[] edge1 = Vector3.subtract(vertices[1], vertices[0]); // V1 - V0
            double[] edge2 = Vector3.subtract(vertices[2], vertices[0]); // V2 - V0

            // 叉积得到法向量，归一化使长度为1
            normal = Vector3.normalize(Vector3.cross(edge1, edge2));
        }
        // 如果计算失败，返回默认的向上法向量
        return normal != null ? normal : new double[] { 0, 1, 0 };
    }

    /**
     * 计算多边形的中心点（几何中心）
     * 
     * 【计算方法】
     * 所有顶点坐标的平均值
     * Center = (V0 + V1 + V2 + ... + Vn) / n
     * 
     * 【用途】
     * 用于深度排序：距离相机远的多边形先画，近的后画
     * 
     * @return 中心点坐标 [x, y, z]
     */
    public double[] getCenter() {
        if (center == null) {
            double x = 0, y = 0, z = 0;

            // 累加所有顶点的坐标
            for (double[] v : vertices) {
                x += v[0];
                y += v[1];
                z += v[2];
            }

            // 除以顶点数得到平均值
            int n = vertices.length;
            center = new double[] { x / n, y / n, z / n };
        }
        return center;
    }

    /**
     * 获取多边形在视图空间中的平均Z值
     * 
     * 【用途】
     * 用于画家算法的深度排序：Z值大的（离相机远）先画
     * 
     * @param viewMatrix 视图变换矩阵（将世界坐标转换为相机坐标）
     * @return 所有顶点Z坐标的平均值
     */
    public double getAverageZ(double[][] viewMatrix) {
        double sum = 0;
        for (double[] v : vertices) {
            // 将顶点从世界坐标变换到视图坐标
            double[] transformed = Matrix4.transformPoint(viewMatrix, v);
            sum += transformed[2]; // 累加Z坐标
        }
        return sum / vertices.length;
    }

    /**
     * 清除缓存
     * 
     * 当多边形的顶点被修改后，需要调用此方法
     * 下次调用getNormal()或getCenter()会重新计算
     */
    public void invalidateCache() {
        normal = null;
        center = null;
    }
}
