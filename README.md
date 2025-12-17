# 计算机图形学课程大作业

> **Computer Graphics Course Project**

山东财经大学 · 计算机图形学

## 项目简介

本项目是计算机图形学课程的综合实践作业，使用纯Java2D/Swing实现软件渲染。涵盖课程大纲要求的全部11个模块，包括2D光栅化算法、3D渲染管线、光照模型等核心内容。

## 功能模块

| 模块 | 实现内容 |
|------|----------|
| **直线扫描转换** | DDA算法、Bresenham算法 |
| **圆的扫描转换** | Bresenham中点圆、正负法、多边形逼近法 |
| **多边形扫描填充** | 扫描线算法、边缘填充 |
| **区域填充** | 种子填充（4连通/8连通） |
| **几何变换** | 平移、缩放、旋转、对称、组合变换 |
| **裁剪算法** | Cohen-Sutherland、Cyrus-Beck |
| **投影变换** | 透视投影、正交投影、视点变换 |
| **可见面判定** | Z-Buffer、背面剔除、线框模式 |
| **光照模型** | 环境光、漫反射、镜面反射 (Phong模型) |
| **明暗处理** | Flat、Gouraud、Phong着色 |
| **机器人动画** | 层级模型、关节动画 |

## 项目结构

```
cg_final/
├── src/main/java/com/graphics/
│   ├── MainFrame.java          # 主窗口
│   ├── Canvas2DPanel.java      # 2D画布 (光栅化算法)
│   ├── Scene3DPanel.java       # 3D场景 (渲染管线)
│   ├── Robot.java              # 机器人模型
│   ├── Matrix4.java            # 4×4变换矩阵
│   ├── Vector3.java            # 向量运算
│   ├── Polygon3D.java          # 3D多边形
│   └── *Dialog.java            # 各功能对话框
├── pom.xml                     # Maven配置
└── README.md
```

## 运行方式

```bash
# 编译
mvn compile

# 运行
mvn exec:java -Dexec.mainClass="com.graphics.MainFrame"
```

或直接在IDE中运行 `MainFrame.java`

## 操作说明

### 2D模式
- 点击两点绘制直线
- 点击圆心+拖拽确定半径
- 多边形模式按Enter完成

### 3D模式
- **左键拖拽**: 旋转视角
- **右键拖拽**: 平移相机
- **滚轮**: 缩放

## 代码注释

所有22个Java文件均包含详细的教育性注释：
- 算法原理和公式推导
- 中英文双语说明
- ASCII示意图

## 技术栈

- Java 17
- Swing/Java2D
- Maven

## 团队成员

周兴 · 王小雨 · 纪昕池

---

*山东财经大学 计算机图形学课程*
