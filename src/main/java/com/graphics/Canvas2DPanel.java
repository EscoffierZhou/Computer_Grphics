package com.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * ====================================================================
 * Canvas2DPanel.java - 2D绘图画布面板
 * ====================================================================
 * 
 * 【功能说明】
 * 这是2D图形学算法的主要演示画布。
 * 实现了课程大纲要求的多种光栅化算法和填充算法。
 * 
 * 【实现的算法】
 * 1. 直线扫描转换
 * - Bresenham直线算法（整数运算）
 * - DDA算法（浮点运算）
 * 
 * 2. 圆的扫描转换
 * - Bresenham中点圆算法（八路对称）
 * - 正负法
 * - 多边形逼近法
 * 
 * 3. 多边形填充
 * - 扫描线填充算法
 * - 种子填充算法（4连通）
 * 
 * 【交互模式】
 * - LINE: 点击两点画直线
 * - CIRCLE: 点击并拖拽画圆
 * - POLYGON: 连续点击画多边形
 * - FILL: 点击区域进行填充
 * - TEXT: 点击放置文字
 * 
 * 【快捷键】
 * - ESC: 取消当前绑制
 * - Enter: 完成多边形
 * - Ctrl+C: 清空画布
 * 
 * @author Computer Graphics Course
 */
public class Canvas2DPanel extends JPanel implements MouseListener, MouseMotionListener {

    // ==================== 画布核心 ====================

    /**
     * 离屏缓冲区 - 所有绘图操作都在这上面进行
     * 使用TYPE_INT_ARGB支持透明通道
     */
    private BufferedImage canvas;

    /**
     * 画布的Graphics2D对象，用于高级绘图操作
     */
    private Graphics2D canvasGraphics;

    // ==================== 绘图模式 ====================

    /**
     * 绘图模式枚举
     */
    public enum DrawMode {
        LINE, // 直线模式
        CIRCLE, // 圆模式
        POLYGON, // 多边形模式
        FILL, // 填充模式
        CLIP, // 裁剪模式
        TEXT // 文字模式
    }

    /** 当前绘图模式 */
    private DrawMode currentMode = DrawMode.LINE;

    // ==================== 绘图参数 ====================

    /** 绘制颜色 */
    private Color drawColor = Color.WHITE;

    /** 填充颜色 */
    private Color fillColor = Color.CYAN;

    /** 线条宽度 */
    private int lineWidth = 2;

    /** 文字字体 */
    private Font currentFont = new Font("SansSerif", Font.PLAIN, 16);

    /** 要绘制的文字内容 */
    private String textToDraw = "Text";

    // ==================== 交互状态 ====================

    /** 当前正在绑制的点集合（用于多边形等需要多个点的图形） */
    private List<Point> currentPoints = new ArrayList<>();

    /** 当前鼠标位置（用于实时预览） */
    private Point currentMousePos = null;

    /** 是否正在绑制中 */
    private boolean isDrawing = false;

    // ==================== 裁剪窗口 ====================

    /** 裁剪窗口矩形 */
    private Rectangle clipWindow = new Rectangle(100, 100, 400, 300);

    /** 是否显示裁剪窗口 */
    private boolean showClipWindow = false;

    // ==================== 绘制历史（用于撤销） ====================

    /** 绑图命令历史记录 */
    private List<DrawCommand> history = new ArrayList<>();

    // ==================== 构造函数 ====================

    public Canvas2DPanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));

        // 添加鼠标监听器
        addMouseListener(this);
        addMouseMotionListener(this);

        // 添加键盘监听 - 支持快捷键操作
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelDrawing(); // ESC取消绑制
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    finishDrawing(); // Enter完成多边形
                } else if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
                    clearCanvas(); // Ctrl+C清空画布
                }
            }
        });
    }

    /**
     * 初始化画布缓冲区
     * 当窗口大小改变时，会重新创建缓冲区
     */
    private void initCanvas() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        if (canvas == null || canvas.getWidth() != w || canvas.getHeight() != h) {
            canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            canvasGraphics = canvas.createGraphics();
            canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            clearCanvas();
        }
    }

    // ==================== 绑制方法 ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        initCanvas();

        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(canvas, 0, 0, null);

        // 绑制当前交互状态（预览效果）
        drawCurrentInteraction(g2d);

        // 绑制裁剪窗口（虚线矩形）
        if (showClipWindow) {
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10, new float[] { 5, 5 }, 0));
            g2d.draw(clipWindow);
        }

        // 绘制提示信息
        drawHelp(g2d);
    }

    /**
     * 绑制当前交互状态（实时预览）
     * 在正式绑制前，显示用户正在绑制的图形预览
     */
    private void drawCurrentInteraction(Graphics2D g) {
        if (currentPoints.isEmpty())
            return;

        g.setColor(drawColor);
        g.setStroke(new BasicStroke(lineWidth));

        switch (currentMode) {
            case LINE:
                // 直线预览：显示从起点到当前鼠标位置的直线
                if (currentPoints.size() == 1 && currentMousePos != null) {
                    Point p1 = currentPoints.get(0);
                    g.drawLine(p1.x, p1.y, currentMousePos.x, currentMousePos.y);
                    drawCoordLabel(g, p1, "P1");
                    drawCoordLabel(g, currentMousePos, "P2");
                }
                break;

            case CIRCLE:
                // 圆预览：显示以点击点为圆心，到鼠标距离为半径的圆
                if (currentPoints.size() == 1 && currentMousePos != null) {
                    Point center = currentPoints.get(0);
                    int radius = (int) center.distance(currentMousePos);
                    g.drawOval(center.x - radius, center.y - radius, radius * 2, radius * 2);
                    drawCoordLabel(g, center, "C");
                    g.setColor(Color.YELLOW);
                    g.drawString("r=" + radius, center.x + 10, center.y - 10);
                }
                break;

            case POLYGON:
                // 多边形预览：显示已有的边和到鼠标位置的预览边
                for (int i = 0; i < currentPoints.size() - 1; i++) {
                    Point p1 = currentPoints.get(i);
                    Point p2 = currentPoints.get(i + 1);
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
                if (!currentPoints.isEmpty() && currentMousePos != null) {
                    Point last = currentPoints.get(currentPoints.size() - 1);
                    g.drawLine(last.x, last.y, currentMousePos.x, currentMousePos.y);
                }
                // 绘制顶点标记
                for (int i = 0; i < currentPoints.size(); i++) {
                    Point p = currentPoints.get(i);
                    g.setColor(Color.RED);
                    g.fillOval(p.x - 4, p.y - 4, 8, 8);
                    drawCoordLabel(g, p, "V" + (i + 1));
                }
                break;

            case TEXT:
                // 文字预览：在鼠标位置显示文字
                if (currentMousePos != null) {
                    g.setColor(drawColor);
                    g.setFont(currentFont);
                    g.drawString(textToDraw, currentMousePos.x, currentMousePos.y);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 绘制坐标标签
     * 在点旁边显示标签和坐标值
     */
    private void drawCoordLabel(Graphics2D g, Point p, String label) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        String text = String.format("%s(%d,%d)", label, p.x, p.y);
        g.drawString(text, p.x + 8, p.y - 8);
    }

    /**
     * 显示顶点坐标（供PolygonScanDialog调用）
     */
    public void showVertexCoordinates(List<Point> vertices) {
        initCanvas();
        Graphics2D g = canvasGraphics;
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));

        for (int i = 0; i < vertices.size(); i++) {
            Point p = vertices.get(i);
            g.setColor(Color.RED);
            g.fillOval(p.x - 5, p.y - 5, 10, 10);
            g.setColor(Color.YELLOW);
            String text = String.format("V%d(%d,%d)", i + 1, p.x, p.y);
            g.drawString(text, p.x + 8, p.y - 8);
        }
        repaint();
    }

    /**
     * 绘制底部帮助提示
     */
    private void drawHelp(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 180));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));

        String help = switch (currentMode) {
            case LINE -> "直线模式: 点击起点，再点击终点";
            case CIRCLE -> "圆模式: 点击圆心，拖拽确定半径";
            case POLYGON -> "多边形模式: 点击添加顶点，Enter完成，Esc取消";
            case FILL -> "填充模式: 点击要填充的区域";
            case CLIP -> "裁剪模式: 绘制直线进行裁剪";
            case TEXT -> "文字模式: 点击位置放置文字";
        };

        g.drawString(help, 10, getHeight() - 10);
    }

    // ==================== 直线算法 ====================

    /**
     * Bresenham直线算法
     * 
     * 【算法原理】
     * 使用整数加法来判断下一个像素的位置，避免浮点运算。
     * 
     * 【核心思想】
     * 对于斜率|m|<1的直线，每次X加1，判断Y是否需要加1。
     * 使用误差项err来累积误差，当误差超过阈值时调整Y。
     * 
     * 【优势】
     * - 只使用整数加法和比较
     * - 效率高，适合硬件实现
     * 
     * @param x0    起点X
     * @param y0    起点Y
     * @param x1    终点X
     * @param y1    终点Y
     * @param color 绘制颜色
     */
    public void drawLineBresenham(int x0, int y0, int x1, int y1, Color color) {
        initCanvas();

        // Step 1: 计算dx和dy的绝对值
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        // Step 2: 确定步进方向（+1或-1）
        int sx = x0 < x1 ? 1 : -1; // X轴步进方向
        int sy = y0 < y1 ? 1 : -1; // Y轴步进方向

        // Step 3: 初始化误差项
        // err = dx - dy，用于判断偏向X还是Y
        int err = dx - dy;

        // Step 4: 主循环，逐像素绘制
        while (true) {
            setPixel(x0, y0, color); // 绘制当前点

            // 检查是否到达终点
            if (x0 == x1 && y0 == y1)
                break;

            // Step 5: 计算2倍误差，用于判断
            int e2 = 2 * err;

            // 如果e2 > -dy，说明需要向X方向移动
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            // 如果e2 < dx，说明需要向Y方向移动
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        repaint();
    }

    /**
     * DDA直线算法（数字微分分析器）
     * 
     * 【算法原理】
     * 使用直线的微分方程，每步增加固定的增量。
     * 
     * 【公式】
     * xInc = dx / steps
     * yInc = dy / steps
     * 其中 steps = max(|dx|, |dy|)
     * 
     * 【优缺点】
     * + 原理简单直观
     * - 需要浮点运算
     * - 需要四舍五入
     * 
     * @param x0    起点X
     * @param y0    起点Y
     * @param x1    终点X
     * @param y1    终点Y
     * @param color 绘制颜色
     */
    public void drawLineDDA(int x0, int y0, int x1, int y1, Color color) {
        initCanvas();

        // Step 1: 计算dx和dy
        int dx = x1 - x0;
        int dy = y1 - y0;

        // Step 2: 计算步数（取dx和dy中较大的绝对值）
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        // 处理特殊情况：起点和终点重合
        if (steps == 0) {
            setPixel(x0, y0, color);
            repaint();
            return;
        }

        // Step 3: 计算每步的增量（浮点数）
        double xInc = (double) dx / steps;
        double yInc = (double) dy / steps;

        // Step 4: 从起点开始迭代
        double x = x0;
        double y = y0;

        // Step 5: 绘制每个像素
        for (int i = 0; i <= steps; i++) {
            // 四舍五入到最近的整数坐标
            setPixel((int) Math.round(x), (int) Math.round(y), color);
            x += xInc;
            y += yInc;
        }

        repaint();
    }

    // ==================== 圆算法 ====================

    /**
     * Bresenham中点圆算法
     * 
     * 【算法原理】
     * 利用圆的八路对称性，只需计算1/8圆弧。
     * 使用中点判断来决定下一个像素位置。
     * 
     * 【八路对称性】
     * 对于圆上的点(x,y)，以下8个点也在圆上：
     * (x,y), (-x,y), (x,-y), (-x,-y)
     * (y,x), (-y,x), (y,-x), (-y,-x)
     * 
     * 【决策变量】
     * d = 3 - 2r（初始值）
     * d < 0: 选择E点（x++）
     * d >= 0: 选择SE点（x++, y--）
     * 
     * @param xc    圆心X
     * @param yc    圆心Y
     * @param r     半径
     * @param color 绘制颜色
     */
    public void drawCircleBresenham(int xc, int yc, int r, Color color) {
        initCanvas();

        // 从(0, r)开始
        int x = 0;
        int y = r;

        // 初始决策参数 d = 3 - 2r
        int d = 3 - 2 * r;

        // 绘制初始的8个对称点
        drawCirclePoints(xc, yc, x, y, color);

        // 主循环：只需计算第一个八分圆
        while (y >= x) {
            x++;

            // 根据决策参数选择下一个点
            if (d > 0) {
                // 中点在圆外，选择SE方向（y--）
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                // 中点在圆内，选择E方向（y不变）
                d = d + 4 * x + 6;
            }

            // 利用对称性绘制8个点
            drawCirclePoints(xc, yc, x, y, color);
        }

        repaint();
    }

    /**
     * 正负法画圆
     * 
     * 【算法原理】
     * 根据点在圆内(F<0)还是圆外(F>0)来决定下一步走向。
     * F(x,y) = x² + y² - r²
     * 
     * 【决策规则】
     * - F >= 0: 点在圆外或圆上，向内走（y--）
     * - F < 0: 点在圆内，向外走（x++）
     */
    public void drawCirclePNMethod(int xc, int yc, int r, Color color) {
        initCanvas();

        int x = 0;
        int y = r;
        double f = 0; // 初始判别式值

        while (x <= y) {
            drawCirclePoints(xc, yc, x, y, color);

            if (f >= 0) {
                // F >= 0，向下走
                f = f - 2 * y + 1;
                y--;
            }
            // 无论如何都向右走
            f = f + 2 * x + 1;
            x++;
        }

        repaint();
    }

    /**
     * 多边形逼近法画圆
     * 
     * 【算法原理】
     * 用正n边形来逼近圆，边数越多越接近圆。
     * 
     * 【顶点计算】
     * 第i个顶点：(r*cos(2πi/n), r*sin(2πi/n))
     * 
     * @param xc    圆心X
     * @param yc    圆心Y
     * @param r     半径
     * @param sides 边数（建议36或更多）
     * @param color 绘制颜色
     */
    public void drawCirclePolygonApprox(int xc, int yc, int r, int sides, Color color) {
        initCanvas();

        // 计算角度增量
        double angleStep = 2 * Math.PI / sides;

        // 计算所有顶点
        int[] xPoints = new int[sides];
        int[] yPoints = new int[sides];

        for (int i = 0; i < sides; i++) {
            double angle = i * angleStep;
            xPoints[i] = xc + (int) (r * Math.cos(angle));
            yPoints[i] = yc + (int) (r * Math.sin(angle));
        }

        // 用Bresenham直线连接相邻顶点
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            drawLineBresenham(xPoints[i], yPoints[i], xPoints[next], yPoints[next], color);
        }

        repaint();
    }

    /**
     * 绘制圆的8个对称点
     * 利用八路对称性，一次绘制8个点
     */
    private void drawCirclePoints(int xc, int yc, int x, int y, Color color) {
        setPixel(xc + x, yc + y, color); // 第1象限
        setPixel(xc - x, yc + y, color); // 第2象限
        setPixel(xc + x, yc - y, color); // 第4象限
        setPixel(xc - x, yc - y, color); // 第3象限
        setPixel(xc + y, yc + x, color); // 交换x,y
        setPixel(xc - y, yc + x, color);
        setPixel(xc + y, yc - x, color);
        setPixel(xc - y, yc - x, color);
    }

    // ==================== 填充算法 ====================

    /**
     * 扫描线填充算法
     * 
     * 【算法原理】
     * 1. 找到多边形Y坐标的范围[minY, maxY]
     * 2. 对每条扫描线y，计算多边形边与扫描线的交点
     * 3. 将交点排序，两两配对填充中间区域
     * 
     * 【交点计算】
     * 使用直线方程：x = x1 + (y - y1) * (x2 - x1) / (y2 - y1)
     * 
     * @param vertices 多边形顶点列表
     * @param color    填充颜色
     */
    public void scanLineFill(List<Point> vertices, Color color) {
        initCanvas();
        if (vertices.size() < 3)
            return;

        // Step 1: 找到Y的范围
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Point p : vertices) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        // Step 2: 对每条扫描线
        for (int y = minY; y <= maxY; y++) {
            List<Integer> intersections = new ArrayList<>();

            // Step 3: 找与每条边的交点
            for (int i = 0; i < vertices.size(); i++) {
                Point p1 = vertices.get(i);
                Point p2 = vertices.get((i + 1) % vertices.size());

                // 检查扫描线是否与边相交
                if ((p1.y <= y && p2.y > y) || (p2.y <= y && p1.y > y)) {
                    // 计算交点X坐标
                    double x = p1.x + (double) (y - p1.y) / (p2.y - p1.y) * (p2.x - p1.x);
                    intersections.add((int) x);
                }
            }

            // Step 4: 排序交点
            intersections.sort(Integer::compareTo);

            // Step 5: 两两配对填充
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int x1 = intersections.get(i);
                int x2 = intersections.get(i + 1);
                for (int x = x1; x <= x2; x++) {
                    setPixel(x, y, color);
                }
            }
        }

        repaint();
    }

    /**
     * 种子填充算法（4连通）
     * 
     * 【算法原理】
     * 从种子点开始，使用堆栈进行非递归填充。
     * 只向上下左右4个方向扩展（4连通）。
     * 
     * 【终止条件】
     * - 遇到边界颜色
     * - 遇到已填充的颜色
     * - 超出画布范围
     * 
     * @param x             种子点X
     * @param y             种子点Y
     * @param fillColor     填充颜色
     * @param boundaryColor 边界颜色
     */
    public void seedFill4(int x, int y, Color fillColor, Color boundaryColor) {
        initCanvas();

        // 边界检查
        if (x < 0 || x >= canvas.getWidth() || y < 0 || y >= canvas.getHeight())
            return;

        // 检查种子点颜色
        Color currentColor = new Color(canvas.getRGB(x, y), true);
        if (currentColor.equals(fillColor) || currentColor.equals(boundaryColor))
            return;

        // 使用栈代替递归（避免栈溢出）
        java.util.Stack<Point> stack = new java.util.Stack<>();
        stack.push(new Point(x, y));

        while (!stack.isEmpty()) {
            Point p = stack.pop();

            // 边界检查
            if (p.x < 0 || p.x >= canvas.getWidth() || p.y < 0 || p.y >= canvas.getHeight())
                continue;

            // 颜色检查
            Color c = new Color(canvas.getRGB(p.x, p.y), true);
            if (c.equals(fillColor) || c.equals(boundaryColor))
                continue;

            // 填充当前像素
            setPixel(p.x, p.y, fillColor);

            // 向4个方向扩展
            stack.push(new Point(p.x + 1, p.y)); // 右
            stack.push(new Point(p.x - 1, p.y)); // 左
            stack.push(new Point(p.x, p.y + 1)); // 下
            stack.push(new Point(p.x, p.y - 1)); // 上
        }

        repaint();
    }

    // ==================== 像素操作 ====================

    /**
     * 设置单个像素
     * 这是所有绘图算法的基础操作
     */
    public void setPixel(int x, int y, Color color) {
        if (x >= 0 && x < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
            canvas.setRGB(x, y, color.getRGB());
        }
    }

    /**
     * 清空画布
     */
    public void clearCanvas() {
        if (canvasGraphics != null) {
            canvasGraphics.setColor(getBackground());
            canvasGraphics.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }
        currentPoints.clear();
        history.clear();
        repaint();
    }

    // ==================== 属性设置方法 ====================

    public void setDrawMode(DrawMode mode) {
        this.currentMode = mode;
        currentPoints.clear();
        repaint();
    }

    public void setDrawColor(Color color) {
        this.drawColor = color;
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    public void setClipWindow(Rectangle rect) {
        this.clipWindow = rect;
        repaint();
    }

    public void setShowClipWindow(boolean show) {
        this.showClipWindow = show;
        repaint();
    }

    public Rectangle getClipWindow() {
        return clipWindow;
    }

    private void cancelDrawing() {
        currentPoints.clear();
        repaint();
    }

    private void finishDrawing() {
        if (currentMode == DrawMode.POLYGON && currentPoints.size() >= 3) {
            // 绘制多边形（闭合）
            for (int i = 0; i < currentPoints.size(); i++) {
                Point p1 = currentPoints.get(i);
                Point p2 = currentPoints.get((i + 1) % currentPoints.size());
                drawLineBresenham(p1.x, p1.y, p2.x, p2.y, drawColor);
            }
        }
        currentPoints.clear();
        repaint();
    }

    // ==================== 鼠标事件处理 ====================

    @Override
    public void mousePressed(MouseEvent e) {
        requestFocusInWindow(); // 获取焦点以接收键盘事件
        Point p = e.getPoint();

        switch (currentMode) {
            case LINE:
                if (currentPoints.isEmpty()) {
                    currentPoints.add(p); // 记录起点
                } else {
                    Point start = currentPoints.get(0);
                    drawLineBresenham(start.x, start.y, p.x, p.y, drawColor);
                    currentPoints.clear();
                }
                break;

            case CIRCLE:
                if (currentPoints.isEmpty()) {
                    currentPoints.add(p); // 记录圆心
                }
                break;

            case POLYGON:
                currentPoints.add(p); // 添加顶点
                break;

            case FILL:
                seedFill4(p.x, p.y, fillColor, drawColor);
                break;

            case TEXT:
                Graphics2D g2d = canvas.createGraphics();
                g2d.setColor(drawColor);
                g2d.setFont(currentFont);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawString(textToDraw, p.x, p.y);
                g2d.dispose();
                repaint();
                break;
        }

        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentMode == DrawMode.CIRCLE && currentPoints.size() == 1) {
            Point center = currentPoints.get(0);
            int radius = (int) center.distance(e.getPoint());
            drawCircleBresenham(center.x, center.y, radius, drawColor);
            currentPoints.clear();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        currentMousePos = e.getPoint();
        if (!currentPoints.isEmpty()) {
            repaint(); // 更新预览
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMousePos = e.getPoint();
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

    // ==================== 绘制命令记录（用于撤销） ====================

    private static class DrawCommand {
        enum Type {
            LINE, CIRCLE, POLYGON, FILL, TEXT
        }

        Type type;
        List<Point> points;
        Color color;
    }

    // ==================== 额外属性设置 ====================

    public void setLineWidth(int width) {
        this.lineWidth = width;
    }

    public void setCurrentFont(Font font) {
        this.currentFont = font;
    }

    public void setTextToDraw(String text) {
        this.textToDraw = text;
    }

    /**
     * 保存画布为图片文件
     */
    public void saveImage(File file) throws IOException {
        String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1);
        ImageIO.write(canvas, ext, file);
    }
}
