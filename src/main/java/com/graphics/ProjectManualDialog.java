package com.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * ====================================================================
 * ProjectManualDialog.java - 项目说明书对话框
 * ====================================================================
 * 
 * 【功能说明】
 * 这个对话框以选项卡形式展示项目的所有功能和算法说明。
 * 包含7个选项卡，覆盖了计算机图形学课程的全部11个模块。
 * 
 * 【选项卡内容】
 * 1. 概述 - 项目整体介绍和功能列表
 * 2. 光栅图形 - 直线和圆的扫描转换算法
 * 3. 几何变换 - 平移、旋转、缩放的矩阵表示
 * 4. 裁剪算法 - Cohen-Sutherland和Cyrus-Beck算法
 * 5. 投影变换 - 透视投影和平行投影
 * 6. 消隐算法 - Z-Buffer和背面剔除
 * 7. 光照模型 - Phong光照和明暗处理
 * 
 * @author Computer Graphics Course
 */
public class ProjectManualDialog extends JDialog {

    // ==================== 构造函数 ====================

    public ProjectManualDialog(JFrame parent) {
        super(parent, "项目说明书 - Project Manual", false);
        initUI();
    }

    // ==================== 界面初始化 ====================

    private void initUI() {
        setSize(700, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 添加各个选项卡
        tabbedPane.addTab("概述", createOverviewPanel());
        tabbedPane.addTab("光栅图形", createRasterPanel());
        tabbedPane.addTab("几何变换", createTransformPanel());
        tabbedPane.addTab("裁剪算法", createClippingPanel());
        tabbedPane.addTab("投影变换", createProjectionPanel());
        tabbedPane.addTab("消隐算法", createHSRPanel());
        tabbedPane.addTab("光照模型", createLightingPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // ==================== 概述面板 ====================

    private JScrollPane createOverviewPanel() {
        String content = """
                计算机图形学课程大作业
                ========================

                技术栈: Java2D / Java3D (Swing)

                本项目实现了计算机图形学课程大纲要求的全部11个模块:

                1. 直线扫描转换
                   - 基本增量算法 (DDA)
                   - Bresenham算法

                2. 圆的扫描转换
                   - 正负法
                   - Bresenham算法 (中点圆)
                   - 多边形逼近法

                3. 多边形扫描算法
                   - 扫描线算法 (ET/AEL数据结构)
                   - 边缘填充算法
                   - 边界标志算法

                4. 区域填充算法
                   - 递归填充
                   - 种子填充 (4连通/8连通)
                   - 扫描线种子填充

                5. 几何变换
                   - 平移/缩放/旋转/对称
                   - 组合变换与矩阵运算

                6. 裁剪算法
                   - Sutherland-Cohen直线裁剪
                   - Cyrus-Beck直线裁剪
                   - 多边形裁剪

                7. 投影变换
                   - 透视投影 (Frustum/Perspective)
                   - 平行投影 (正交)
                   - 视点变换

                8. 视见体变换
                   - 规范视见体变换
                   - 视口窗口转换

                9. 可见面判断
                   - Z-Buffer算法
                   - 扫描线消隐
                   - 后向面消除

                10. 光照模型
                    - 环境光
                    - 漫反射
                    - 镜面反射
                    - Phong光照模型

                11. 明暗处理
                    - Gouraud明暗处理
                    - Phong明暗处理

                附加功能:
                - 多边形机器人模型
                - 关节动画系统
                - 舞台场景
                """;
        return createScrollableText(content);
    }

    // ==================== 光栅图形面板 ====================

    private JScrollPane createRasterPanel() {
        String content = """
                光栅图形算法
                ==============

                1. 直线扫描转换
                ----------------

                【DDA算法 (Digital Differential Analyzer)】
                基本思想: 利用直线的微分方程逐点计算

                步骤:
                1. 计算 dx = x2-x1, dy = y2-y1
                2. 取 steps = max(|dx|, |dy|)
                3. 计算增量 xInc = dx/steps, yInc = dy/steps
                4. 从起点开始，每步 x += xInc, y += yInc
                5. 对坐标四舍五入后绘制像素

                特点: 使用浮点运算，直观但效率较低

                【Bresenham算法】
                基本思想: 只用整数运算判断下一像素位置

                对于斜率 |m| < 1 的情况:
                - 决策变量 d = 2*dy - dx
                - 若 d < 0: y不变, d += 2*dy
                - 若 d >= 0: y++, d += 2*(dy-dx)

                特点: 只用整数加减法，效率高

                2. 圆的扫描转换
                ----------------

                【中点圆算法 (Bresenham)】
                利用圆的八分对称性，只需计算1/8圆弧

                决策变量: d = 1 - r
                若 d < 0: 选择E点, d += 2x + 1
                若 d >= 0: 选择SE点, d += 2(x-y) + 1

                【正负法】
                根据点在圆内(F<0)或圆外(F>0)决定走向:
                - F >= 0: 向下走 (y--)
                - F < 0: 向右走 (x++)

                【多边形逼近法】
                用正n边形逼近圆，边数越多越接近圆
                顶点: (r*cos(2πk/n), r*sin(2πk/n)), k=0,1,...,n-1
                """;
        return createScrollableText(content);
    }

    // ==================== 几何变换面板 ====================

    private JScrollPane createTransformPanel() {
        String content = """
                几何变换
                ==========

                使用4x4齐次坐标矩阵表示3D变换

                1. 平移变换
                -----------
                [1 0 0 tx]   [x]   [x+tx]
                [0 1 0 ty] × [y] = [y+ty]
                [0 0 1 tz]   [z]   [z+tz]
                [0 0 0 1 ]   [1]   [ 1  ]

                2. 缩放变换
                -----------
                [sx 0  0  0]   [x]   [sx*x]
                [0  sy 0  0] × [y] = [sy*y]
                [0  0  sz 0]   [z]   [sz*z]
                [0  0  0  1]   [1]   [ 1  ]

                3. 旋转变换
                -----------
                绕X轴:
                [1   0      0    0]
                [0  cos θ -sin θ 0]
                [0  sin θ  cos θ 0]
                [0   0      0    1]

                绕Y轴:
                [ cos θ 0 sin θ 0]
                [  0    1   0   0]
                [-sin θ 0 cos θ 0]
                [  0    0   0   1]

                绕Z轴:
                [cos θ -sin θ 0 0]
                [sin θ  cos θ 0 0]
                [  0      0   1 0]
                [  0      0   0 1]

                4. 对称变换
                -----------
                关于XY平面: z → -z
                关于XZ平面: y → -y
                关于YZ平面: x → -x

                5. 组合变换
                -----------
                多个变换的组合 = 矩阵相乘
                M_total = M_n × ... × M_2 × M_1
                注意: 矩阵乘法不满足交换律!
                先做的变换矩阵在右边
                """;
        return createScrollableText(content);
    }

    // ==================== 裁剪算法面板 ====================

    private JScrollPane createClippingPanel() {
        String content = """
                裁剪算法
                ==========

                1. Sutherland-Cohen直线裁剪
                ---------------------------
                核心思想: 使用区域编码快速判断

                区域编码 (4位):
                - bit 3 (上): y > ymax
                - bit 2 (下): y < ymin
                - bit 1 (右): x > xmax
                - bit 0 (左): x < xmin

                判断规则:
                - code1 | code2 = 0: 完全可见
                - code1 & code2 ≠ 0: 完全不可见
                - 否则: 需要裁剪

                裁剪过程:
                1. 计算两端点编码
                2. 如果需要裁剪，选择窗口外的端点
                3. 与相应的窗口边界求交
                4. 用交点替换原端点
                5. 重复直到完全可见或不可见

                2. Cyrus-Beck直线裁剪
                ----------------------
                核心思想: 使用参数化直线方程

                直线表示: P(t) = P1 + t*(P2-P1), t∈[0,1]

                对每条裁剪边计算:
                t = (P_edge - P1) · N / (P2-P1) · N

                根据N·D的符号判断是进入还是离开边界

                3. 多边形裁剪 (Sutherland-Hodgman)
                -----------------------------------
                依次对裁剪窗口的每条边进行裁剪

                对于每条边的4种情况:
                - 内→内: 输出终点
                - 内→外: 输出交点
                - 外→外: 无输出
                - 外→内: 输出交点和终点
                """;
        return createScrollableText(content);
    }

    // ==================== 投影变换面板 ====================

    private JScrollPane createProjectionPanel() {
        String content = """
                投影变换
                ==========

                1. 透视投影
                -----------
                模拟人眼或相机的成像原理
                远处物体看起来更小

                【Perspective矩阵】
                f = 1 / tan(fov/2)

                [f/aspect  0      0              0     ]
                [   0      f      0              0     ]
                [   0      0  (f+n)/(n-f)  2fn/(n-f)   ]
                [   0      0     -1              0     ]

                参数:
                - fov: 视场角
                - aspect: 宽高比
                - n, f: 近裁剪面和远裁剪面

                【Frustum矩阵】
                更通用的形式，直接指定视锥体六个面

                2. 平行投影 (正交)
                ------------------
                投影线互相平行
                物体大小与距离无关

                [2/(r-l)    0       0    -(r+l)/(r-l)]
                [   0    2/(t-b)    0    -(t+b)/(t-b)]
                [   0       0   -2/(f-n) -(f+n)/(f-n)]
                [   0       0       0          1     ]

                3. 视点变换
                -----------
                将世界坐标转换到相机坐标

                步骤:
                1. 平移到相机位置的负值
                2. 旋转使相机朝向-Z轴

                View Matrix = R × T
                """;
        return createScrollableText(content);
    }

    // ==================== 消隐算法面板 ====================

    private JScrollPane createHSRPanel() {
        String content = """
                可见面判定 (Hidden Surface Removal)
                ====================================

                1. Z-Buffer算法
                ----------------
                核心思想: 维护每个像素的深度值

                数据结构:
                - 帧缓冲: 存储每个像素的颜色
                - 深度缓冲: 存储每个像素的Z值

                算法:
                for 每个多边形 P:
                    for P覆盖的每个像素(x,y):
                        计算该像素处的深度z
                        if z < zBuffer[x][y]:
                            zBuffer[x][y] = z
                            frameBuffer[x][y] = P的颜色

                优点: 简单、易于硬件实现
                缺点: 需要额外内存

                2. 扫描线消隐
                --------------
                将Z-Buffer与扫描线结合
                每次只处理一条扫描线

                3. 后向面消除 (Backface Culling)
                ---------------------------------
                核心思想: 背向观察者的面不可见

                判断方法:
                计算面的法向量N
                计算观察方向V
                if N · V < 0:
                    该面背向观察者，不绘制

                优点: 快速剔除约50%的面
                缺点: 不能处理相互遮挡

                4. 画家算法
                -----------
                按深度排序后从远到近绘制
                近处物体覆盖远处物体

                问题: 部分情况无法正确排序
                """;
        return createScrollableText(content);
    }

    // ==================== 光照模型面板 ====================

    private JScrollPane createLightingPanel() {
        String content = """
                光照模型与明暗处理
                ==================

                1. Phong光照模型
                -----------------
                I = I_ambient + I_diffuse + I_specular

                【环境光 Ambient】
                I_a = k_a × I_ambient
                - k_a: 环境光反射系数
                - 模拟来自四面八方的间接光照

                【漫反射 Diffuse】
                I_d = k_d × I_light × max(0, N·L)
                - k_d: 漫反射系数
                - N: 表面法向量
                - L: 光源方向
                - 符合Lambert定律

                【镜面反射 Specular】
                I_s = k_s × I_light × max(0, R·V)^n
                - k_s: 镜面反射系数
                - R: 反射光方向
                - V: 观察方向
                - n: 光泽度 (shininess)

                2. Gouraud明暗处理
                -------------------
                步骤:
                1. 计算每个顶点的法向量 (邻面法向量平均)
                2. 使用光照模型计算顶点颜色
                3. 扫描转换时插值顶点颜色

                优点: 计算简单
                缺点: 高光可能丢失

                3. Phong明暗处理
                -----------------
                步骤:
                1. 计算每个顶点的法向量
                2. 扫描转换时插值法向量
                3. 对每个像素使用插值法向量计算光照

                优点: 高光效果好
                缺点: 计算量大
                """;
        return createScrollableText(content);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建可滚动的文本区域
     */
    private JScrollPane createScrollableText(String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setCaretPosition(0); // 滚动到顶部
        return new JScrollPane(textArea);
    }
}
