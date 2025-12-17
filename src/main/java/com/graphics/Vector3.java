package com.graphics;

/**
 * ====================================================================
 * Vector3.java - 3D向量工具类
 * ====================================================================
 * 
 * 【功能说明】
 * 这个类提供了3D向量的各种数学运算。
 * 在计算机图形学中，向量用于表示方向、位置差、法线等。
 * 
 * 【什么是向量？】
 * 向量是一个有大小和方向的量，用(x, y, z)三个分量表示。
 * 例如：(1, 0, 0)表示指向X轴正方向的单位向量
 * 
 * 【向量 vs 点】
 * - 点(Point): 表示空间中的一个位置，如(3, 4, 5)
 * - 向量(Vector): 表示一个方向和距离，如"向右走3米"
 * - 两点相减得到向量: 向量 = 终点 - 起点
 * 
 * 【本类中使用double[]表示向量】
 * double[] v = {x, y, z}; // 一个3D向量
 * v[0] = x分量, v[1] = y分量, v[2] = z分量
 * 
 * @author Computer Graphics Course
 */
public class Vector3 {

    // ==================== 基本运算 ====================

    /**
     * 向量加法: A + B
     * 
     * 【计算公式】
     * 结果 = (a.x + b.x, a.y + b.y, a.z + b.z)
     * 
     * 【几何意义】
     * 将两个向量首尾相连，得到的向量
     * 
     * @param a 第一个向量
     * @param b 第二个向量
     * @return 两个向量的和
     */
    public static double[] add(double[] a, double[] b) {
        return new double[] {
                a[0] + b[0], // x分量相加
                a[1] + b[1], // y分量相加
                a[2] + b[2] // z分量相加
        };
    }

    /**
     * 向量减法: A - B
     * 
     * 【计算公式】
     * 结果 = (a.x - b.x, a.y - b.y, a.z - b.z)
     * 
     * 【几何意义】
     * 从点B指向点A的向量
     * 常用于计算两点之间的方向
     * 
     * @param a 被减向量（或终点）
     * @param b 减去的向量（或起点）
     * @return 结果向量
     */
    public static double[] subtract(double[] a, double[] b) {
        return new double[] {
                a[0] - b[0],
                a[1] - b[1],
                a[2] - b[2]
        };
    }

    /**
     * 向量数乘（标量乘法）: V * s
     * 
     * 【计算公式】
     * 结果 = (v.x * s, v.y * s, v.z * s)
     * 
     * 【几何意义】
     * - s > 1: 向量变长
     * - 0 < s < 1: 向量变短
     * - s < 0: 向量反向
     * 
     * @param v 原向量
     * @param s 缩放因子（标量）
     * @return 缩放后的向量
     */
    public static double[] scale(double[] v, double s) {
        return new double[] {
                v[0] * s,
                v[1] * s,
                v[2] * s
        };
    }

    // ==================== 重要运算 ====================

    /**
     * 向量点积（内积、数量积）: A · B
     * 
     * 【计算公式】
     * 结果 = a.x*b.x + a.y*b.y + a.z*b.z
     * 
     * 【几何意义】
     * A·B = |A| * |B| * cos(θ)
     * 其中θ是两向量之间的夹角
     * 
     * 【用途】
     * 1. 判断两向量夹角:
     * - 点积 > 0: 夹角 < 90° (大致同向)
     * - 点积 = 0: 夹角 = 90° (垂直)
     * - 点积 < 0: 夹角 > 90° (大致反向)
     * 2. 计算光照强度: 光线·法线 = 光照因子
     * 
     * @param a 第一个向量
     * @param b 第二个向量
     * @return 点积结果（一个数，不是向量）
     */
    public static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    /**
     * 向量叉积（外积、向量积）: A × B
     * 
     * 【计算公式】
     * 结果.x = a.y * b.z - a.z * b.y
     * 结果.y = a.z * b.x - a.x * b.z
     * 结果.z = a.x * b.y - a.y * b.x
     * 
     * 【几何意义】
     * 叉积结果是一个新向量，垂直于A和B所在的平面
     * 
     * 【用途】
     * 1. 计算平面的法向量: 法线 = 边1 × 边2
     * 2. 判断点在三角形内外
     * 
     * 【右手定则】
     * 用右手从A握向B，大拇指指向的方向就是A×B的方向
     * 
     * @param a 第一个向量
     * @param b 第二个向量
     * @return 叉积结果向量
     */
    public static double[] cross(double[] a, double[] b) {
        return new double[] {
                a[1] * b[2] - a[2] * b[1], // x分量
                a[2] * b[0] - a[0] * b[2], // y分量
                a[0] * b[1] - a[1] * b[0] // z分量
        };
    }

    // ==================== 长度相关 ====================

    /**
     * 向量长度（模）: |V|
     * 
     * 【计算公式】
     * |V| = √(x² + y² + z²)
     * 这是3D版本的勾股定理
     * 
     * @param v 输入向量
     * @return 向量的长度（非负数）
     */
    public static double length(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    /**
     * 向量归一化（单位化）
     * 
     * 【作用】
     * 将向量缩放到长度为1，但保持方向不变
     * 
     * 【计算公式】
     * 归一化向量 = V / |V|
     * 
     * 【用途】
     * 1. 法向量必须是单位向量才能正确计算光照
     * 2. 方向向量通常需要归一化
     * 
     * @param v 输入向量
     * @return 单位向量（长度为1）
     */
    public static double[] normalize(double[] v) {
        double len = length(v);

        // 防止除以零：如果向量太短，返回零向量
        if (len < 0.0001) {
            return new double[] { 0, 0, 0 };
        }

        return new double[] {
                v[0] / len,
                v[1] / len,
                v[2] / len
        };
    }

    // ==================== 其他运算 ====================

    /**
     * 向量取反: -V
     * 
     * 【几何意义】
     * 方向相反，长度相同
     * 
     * @param v 输入向量
     * @return 反向向量
     */
    public static double[] negate(double[] v) {
        return new double[] { -v[0], -v[1], -v[2] };
    }

    /**
     * 向量反射
     * 
     * 【用途】
     * 计算镜面反射方向，用于Phong光照模型中的镜面高光
     * 
     * 【公式】
     * R = I - 2 * (I·N) * N
     * 其中I是入射向量，N是法线，R是反射向量
     * 
     * @param incident 入射向量（指向表面）
     * @param normal   表面法线（必须是单位向量）
     * @return 反射向量
     */
    public static double[] reflect(double[] incident, double[] normal) {
        double d = 2 * dot(incident, normal);
        return new double[] {
                incident[0] - d * normal[0],
                incident[1] - d * normal[1],
                incident[2] - d * normal[2]
        };
    }

    /**
     * 线性插值（Lerp）
     * 
     * 【公式】
     * 结果 = A + (B - A) * t = A * (1-t) + B * t
     * 
     * 【用途】
     * 1. 动画：在两个位置之间平滑过渡
     * 2. Gouraud着色：在顶点颜色之间插值
     * 
     * @param a 起始向量
     * @param b 结束向量
     * @param t 插值参数 (0.0 = A, 1.0 = B, 0.5 = 中点)
     * @return 插值结果
     */
    public static double[] lerp(double[] a, double[] b, double t) {
        return new double[] {
                a[0] + (b[0] - a[0]) * t,
                a[1] + (b[1] - a[1]) * t,
                a[2] + (b[2] - a[2]) * t
        };
    }

    /**
     * 计算两点间的距离
     * 
     * 【计算方法】
     * 距离 = |B - A| = 两点相减得到的向量的长度
     * 
     * @param a 第一个点
     * @param b 第二个点
     * @return 两点之间的距离
     */
    public static double distance(double[] a, double[] b) {
        return length(subtract(a, b));
    }

    /**
     * 打印向量（调试用）
     * 
     * @param v 要打印的向量
     */
    public static void print(double[] v) {
        System.out.printf("(%.3f, %.3f, %.3f)%n", v[0], v[1], v[2]);
    }
}
