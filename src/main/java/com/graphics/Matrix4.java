package com.graphics;

/**
 * ====================================================================
 * Matrix4.java - 4x4矩阵工具类
 * ====================================================================
 * 
 * 【功能说明】
 * 这个类提供了3D图形学中最核心的数学工具：4x4变换矩阵。
 * 所有的3D变换（平移、旋转、缩放、投影）都通过矩阵来实现。
 * 
 * 【为什么使用4x4矩阵？】
 * 在3D图形学中，我们使用"齐次坐标"来表示点：(x, y, z, 1)
 * 这样可以用一个统一的4x4矩阵来表示所有类型的变换，包括平移。
 * 如果只用3x3矩阵，平移就无法表示为矩阵乘法。
 * 
 * 【矩阵的结构】
 * 一个4x4矩阵看起来像这样：
 * | m00 m01 m02 m03 |
 * | m10 m11 m12 m13 |
 * | m20 m21 m22 m23 |
 * | m30 m31 m32 m33 |
 * 
 * 在Java中用 double[4][4] 表示：
 * matrix[行][列]，例如 matrix[0][3] = m03
 * 
 * 【变换的组合】
 * 多个变换可以通过矩阵乘法组合成一个矩阵。
 * 例如：先平移再旋转 = 旋转矩阵 * 平移矩阵
 * 注意：矩阵乘法不满足交换律，顺序很重要！
 * 
 * @author Computer Graphics Course
 */
public class Matrix4 {

    // ==================== 基本矩阵 ====================

    /**
     * 创建单位矩阵（Identity Matrix）
     * 
     * 【什么是单位矩阵？】
     * 单位矩阵是矩阵中的"1"，任何矩阵乘以单位矩阵都等于它本身。
     * 对角线上是1，其他位置是0。
     * 
     * 【结构】
     * | 1 0 0 0 |
     * | 0 1 0 0 |
     * | 0 0 1 0 |
     * | 0 0 0 1 |
     * 
     * 【用途】
     * 作为变换的起点，或者表示"不做任何变换"
     * 
     * @return 4x4单位矩阵
     */
    public static double[][] identity() {
        return new double[][] {
                { 1, 0, 0, 0 },
                { 0, 1, 0, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 1 }
        };
    }

    // ==================== 基本变换矩阵 ====================

    /**
     * 创建平移矩阵（Translation Matrix）
     * 
     * 【作用】
     * 将物体沿着(tx, ty, tz)方向移动
     * 
     * 【结构】
     * | 1 0 0 tx | 变换后：
     * | 0 1 0 ty | x' = x + tx
     * | 0 0 1 tz | y' = y + ty
     * | 0 0 0 1 | z' = z + tz
     * 
     * 【示例】
     * translate(3, 0, 0) 将物体向右移动3个单位
     * 
     * @param tx X方向的移动距离
     * @param ty Y方向的移动距离
     * @param tz Z方向的移动距离
     * @return 平移矩阵
     */
    public static double[][] translate(double tx, double ty, double tz) {
        return new double[][] {
                { 1, 0, 0, tx },
                { 0, 1, 0, ty },
                { 0, 0, 1, tz },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 创建缩放矩阵（Scale Matrix）
     * 
     * 【作用】
     * 将物体在各轴方向上缩放
     * 
     * 【结构】
     * | sx 0 0 0 | 变换后：
     * | 0 sy 0 0 | x' = x * sx
     * | 0 0 sz 0 | y' = y * sy
     * | 0 0 0 1 | z' = z * sz
     * 
     * 【示例】
     * scale(2, 2, 2) 将物体放大到原来的2倍
     * scale(0.5, 0.5, 0.5) 将物体缩小到原来的一半
     * scale(1, 2, 1) 只在Y方向拉伸2倍
     * 
     * @param sx X方向的缩放因子
     * @param sy Y方向的缩放因子
     * @param sz Z方向的缩放因子
     * @return 缩放矩阵
     */
    public static double[][] scale(double sx, double sy, double sz) {
        return new double[][] {
                { sx, 0, 0, 0 },
                { 0, sy, 0, 0 },
                { 0, 0, sz, 0 },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 创建绕X轴旋转矩阵
     * 
     * 【作用】
     * 将物体绕X轴旋转指定角度
     * 想象一个烤肉架，X轴是串肉的棍子，物体绕着它转
     * 
     * 【结构】
     * | 1 0 0 0 | 其中：
     * | 0 cos -sin 0 | c = cos(angle)
     * | 0 sin cos 0 | s = sin(angle)
     * | 0 0 0 1 |
     * 
     * @param angle 旋转角度（弧度制，不是角度制！）
     *              例如：90度 = Math.PI/2 弧度
     * @return 绕X轴的旋转矩阵
     */
    public static double[][] rotateX(double angle) {
        double c = Math.cos(angle); // 余弦值
        double s = Math.sin(angle); // 正弦值
        return new double[][] {
                { 1, 0, 0, 0 },
                { 0, c, -s, 0 },
                { 0, s, c, 0 },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 创建绕Y轴旋转矩阵
     * 
     * 【作用】
     * 将物体绕Y轴旋转指定角度
     * 想象一个旋转木马，Y轴是中心的柱子，物体绕着它水平旋转
     * 
     * 【结构】
     * | cos 0 sin 0 |
     * | 0 1 0 0 |
     * | -sin 0 cos 0 |
     * | 0 0 0 1 |
     * 
     * @param angle 旋转角度（弧度制）
     * @return 绕Y轴的旋转矩阵
     */
    public static double[][] rotateY(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new double[][] {
                { c, 0, s, 0 },
                { 0, 1, 0, 0 },
                { -s, 0, c, 0 },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 创建绕Z轴旋转矩阵
     * 
     * 【作用】
     * 将物体绕Z轴旋转指定角度
     * 就像2D平面上的旋转（因为Z轴指向屏幕外）
     * 
     * 【结构】
     * | cos -sin 0 0 |
     * | sin cos 0 0 |
     * | 0 0 1 0 |
     * | 0 0 0 1 |
     * 
     * @param angle 旋转角度（弧度制）
     * @return 绕Z轴的旋转矩阵
     */
    public static double[][] rotateZ(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new double[][] {
                { c, -s, 0, 0 },
                { s, c, 0, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 1 }
        };
    }

    // ==================== 投影矩阵 ====================

    /**
     * 创建透视投影矩阵
     * 
     * 【作用】
     * 实现"近大远小"的效果，模拟人眼看世界的方式
     * 
     * 【参数说明】
     * 
     * @param fov    视野角度（Field of View），单位是弧度
     *               一般设为 Math.PI/4 (45度) 或 Math.PI/3 (60度)
     * @param aspect 宽高比 = 窗口宽度 / 窗口高度
     * @param near   近裁剪面距离（太近的物体会被裁掉）
     * @param far    远裁剪面距离（太远的物体会被裁掉）
     * @return 透视投影矩阵
     */
    public static double[][] perspective(double fov, double aspect, double near, double far) {
        double tanHalfFov = Math.tan(fov / 2);
        double f = 1.0 / tanHalfFov;

        return new double[][] {
                { f / aspect, 0, 0, 0 },
                { 0, f, 0, 0 },
                { 0, 0, (far + near) / (near - far), (2 * far * near) / (near - far) },
                { 0, 0, -1, 0 }
        };
    }

    /**
     * 创建正交投影矩阵（平行投影）
     * 
     * 【作用】
     * 没有"近大远小"效果，物体无论远近大小都一样
     * 常用于工程制图、CAD软件
     * 
     * 【参数说明】
     * 定义一个长方体视见体（可见范围）
     * 
     * @param left   左边界
     * @param right  右边界
     * @param bottom 下边界
     * @param top    上边界
     * @param near   近裁剪面
     * @param far    远裁剪面
     * @return 正交投影矩阵
     */
    public static double[][] orthographic(double left, double right,
            double bottom, double top,
            double near, double far) {
        return new double[][] {
                { 2 / (right - left), 0, 0, -(right + left) / (right - left) },
                { 0, 2 / (top - bottom), 0, -(top + bottom) / (top - bottom) },
                { 0, 0, -2 / (far - near), -(far + near) / (far - near) },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 创建视锥体投影矩阵 (Frustum)
     * 
     * 【作用】
     * 另一种定义透视投影的方式，直接指定视锥体的六个面
     * 
     * @param left   左边界
     * @param right  右边界
     * @param bottom 下边界
     * @param top    上边界
     * @param near   近裁剪面
     * @param far    远裁剪面
     * @return 视锥体投影矩阵
     */
    public static double[][] frustum(double left, double right,
            double bottom, double top,
            double near, double far) {
        return new double[][] {
                { 2 * near / (right - left), 0, (right + left) / (right - left), 0 },
                { 0, 2 * near / (top - bottom), (top + bottom) / (top - bottom), 0 },
                { 0, 0, -(far + near) / (far - near), -2 * far * near / (far - near) },
                { 0, 0, -1, 0 }
        };
    }

    // ==================== 对称变换矩阵 ====================

    /**
     * 关于XY平面的镜像（Z坐标取反）
     * 
     * 【效果】
     * z' = -z，物体关于XY平面翻转
     */
    public static double[][] reflectXY() {
        return new double[][] {
                { 1, 0, 0, 0 },
                { 0, 1, 0, 0 },
                { 0, 0, -1, 0 },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 关于XZ平面的镜像（Y坐标取反）
     * 
     * 【效果】
     * y' = -y，物体上下翻转
     */
    public static double[][] reflectXZ() {
        return new double[][] {
                { 1, 0, 0, 0 },
                { 0, -1, 0, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 关于YZ平面的镜像（X坐标取反）
     * 
     * 【效果】
     * x' = -x，物体左右翻转
     */
    public static double[][] reflectYZ() {
        return new double[][] {
                { -1, 0, 0, 0 },
                { 0, 1, 0, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 1 }
        };
    }

    /**
     * 创建错切矩阵（Shear Matrix）
     * 
     * 【作用】
     * 使物体发生倾斜变形，像把正方形变成平行四边形
     * 
     * @param shxy X随Y变化的系数
     * @param shxz X随Z变化的系数
     * @param shyx Y随X变化的系数
     * @param shyz Y随Z变化的系数
     * @param shzx Z随X变化的系数
     * @param shzy Z随Y变化的系数
     * @return 错切矩阵
     */
    public static double[][] shear(double shxy, double shxz,
            double shyx, double shyz,
            double shzx, double shzy) {
        return new double[][] {
                { 1, shxy, shxz, 0 },
                { shyx, 1, shyz, 0 },
                { shzx, shzy, 1, 0 },
                { 0, 0, 0, 1 }
        };
    }

    // ==================== 核心运算方法 ====================

    /**
     * 矩阵乘法 - 这是整个类最重要的方法！
     * 
     * 【公式】
     * result[i][j] = A的第i行 · B的第j列（点积）
     * = A[i][0]*B[0][j] + A[i][1]*B[1][j] + A[i][2]*B[2][j] + A[i][3]*B[3][j]
     * 
     * 【用途】
     * 将多个变换合并为一个矩阵
     * 例如：先平移(T)再旋转(R)再缩放(S) = S * R * T
     * 注意：从右往左读！最后写的变换最先执行
     * 
     * 【注意】
     * 矩阵乘法不满足交换律：A*B ≠ B*A
     * 
     * @param a 第一个矩阵（左边）
     * @param b 第二个矩阵（右边）
     * @return 两个矩阵的乘积
     */
    public static double[][] multiply(double[][] a, double[][] b) {
        double[][] result = new double[4][4];

        // 遍历结果矩阵的每一个元素
        for (int i = 0; i < 4; i++) { // 行
            for (int j = 0; j < 4; j++) { // 列
                result[i][j] = 0;
                // 计算第i行和第j列的点积
                for (int k = 0; k < 4; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    /**
     * 用矩阵变换一个3D点 - 这是应用变换的核心方法！
     * 
     * 【公式】
     * P' = M * P
     * 其中 P = (x, y, z, 1) 是齐次坐标
     * 
     * 【计算过程】
     * | m00 m01 m02 m03 | | x | | m00*x + m01*y + m02*z + m03 |
     * | m10 m11 m12 m13 | * | y | = | m10*x + m11*y + m12*z + m13 |
     * | m20 m21 m22 m23 | | z | | m20*x + m21*y + m22*z + m23 |
     * | m30 m31 m32 m33 | | 1 | | m30*x + m31*y + m32*z + m33 |
     * 
     * @param matrix 变换矩阵
     * @param point  原始点坐标 [x, y, z]
     * @return 变换后的点 [x', y', z', w']
     */
    public static double[] transformPoint(double[][] matrix, double[] point) {
        // 获取原始坐标，如果数组太短则补0
        double x = point.length > 0 ? point[0] : 0;
        double y = point.length > 1 ? point[1] : 0;
        double z = point.length > 2 ? point[2] : 0;
        double w = 1; // 齐次坐标的w分量，点的w=1

        // 矩阵乘以列向量
        return new double[] {
                matrix[0][0] * x + matrix[0][1] * y + matrix[0][2] * z + matrix[0][3] * w,
                matrix[1][0] * x + matrix[1][1] * y + matrix[1][2] * z + matrix[1][3] * w,
                matrix[2][0] * x + matrix[2][1] * y + matrix[2][2] * z + matrix[2][3] * w,
                matrix[3][0] * x + matrix[3][1] * y + matrix[3][2] * z + matrix[3][3] * w
        };
    }

    /**
     * 打印矩阵（调试用）
     * 
     * @param m 要打印的矩阵
     */
    public static void print(double[][] m) {
        for (int i = 0; i < 4; i++) {
            System.out.printf("[%.3f, %.3f, %.3f, %.3f]%n",
                    m[i][0], m[i][1], m[i][2], m[i][3]);
        }
    }
}
