# Java 3D Graphics System 设置说明
# Setup Instructions

## 重要提示 Important Notes

此项目需要Maven来管理依赖和构建。如果您的系统没有安装Maven，有以下几种方式运行项目：

This project requires Maven for dependency management and building. If you don't have Maven installed, here are your options:

---

## 方式一：安装Maven (推荐 Recommended)

### Windows安装步骤:

1. **下载Maven**
   - 访问: https://maven.apache.org/download.cgi
   - 下载 `apache-maven-3.x.x-bin.zip`

2. **解压安装**
   - 解压到: `C:\Program Files\Apache\maven`

3. **设置环境变量**
   ```powershell
   # 添加MAVEN_HOME
   setx MAVEN_HOME "C:\Program Files\Apache\maven"
   
   # 添加到PATH
   setx PATH "%PATH%;%MAVEN_HOME%\bin"
   ```

4. **验证安装**
   ```powershell
   # 重新打开命令行窗口
   mvn -version
   ```

5. **运行项目**
   ```powershell
   cd "F:\desktop\Work\_GitHub\SDUFE_AI2023_NOTE\大三上\(55p)计算机图形学\上机作业\FINAL"
   mvn clean compile
   mvn exec:java
   ```

---

## 方式二：使用IDE (最简单 Easiest)

### IntelliJ IDEA:

1. 打开 IntelliJ IDEA
2. File → Open → 选择 `FINAL` 文件夹
3. IDE会自动识别Maven项目并下载依赖
4. 等待依赖下载完成
5. 右键 `GraphicsSystem.java` → Run

### Eclipse:

1. 打开 Eclipse
2. File → Import → Maven → Existing Maven Projects
3. 选择 `FINAL` 文件夹
4. 等待依赖下载完成
5. 右键 项目 → Run As → Java Application
6. 选择 `GraphicsSystem` 主类

---

## 方式三：手动下载依赖 (不推荐 Not Recommended)

如果无法使用Maven，需要手动下载以下JAR文件：

### 需要的库文件:

1. **Java3D Core Libraries**
   - `java3d-core-1.7.0.jar`
   - `java3d-utils-1.7.0.jar`
   - `vecmath-1.7.0.jar`

2. **JOGL Libraries (Java3D backend)**
   - `jogl-all-2.3.2.jar`
   - `gluegen-rt-2.3.2.jar`

### 下载位置:

- Maven Central: https://repo1.maven.org/maven2/
- JOGAMP: https://jogamp.org/deployment/jogamp-current/archive/

### 手动编译步骤:

```powershell
# 1. 创建lib文件夹
mkdir lib
# 将下载的JAR文件放入lib文件夹

# 2. 编译
javac -cp "lib/*" -d bin -sourcepath src/main/java src/main/java/com/graphics/*.java

# 3. 运行
java -cp "bin;lib/*" com.graphics.GraphicsSystem
```

---

## 常见问题解决 Troubleshooting

### 问题1: "mvn不是内部或外部命令"

**原因**: Maven未安装或未配置到PATH

**解决**: 
- 按照"方式一"安装Maven
- 或使用IDE（方式二）

### 问题2: 依赖下载失败

**原因**: 网络问题或Maven仓库访问问题

**解决**:
- 检查网络连接
- 配置Maven镜像（阿里云镜像）:
  编辑 `C:\Users\{用户名}\.m2\settings.xml`:
  ```xml
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
  ```

### 问题3: Java版本不兼容

**当前检测到**: Java 23

**项目配置**: Java 8+

**解决**: Java 23可以运行Java 8项目，但如果遇到问题：
- 在pom.xml中修改:
  ```xml
  <maven.compiler.source>23</maven.compiler.source>
  <maven.compiler.target>23</maven.compiler.target>
  ```

### 问题4: 3D场景黑屏或不显示

**可能原因**:
- 显卡驱动过旧
- OpenGL支持问题

**解决**:
- 更新显卡驱动
- 确保系统支持OpenGL 2.0或更高

---

## 快速开始 Quick Start

### 如果您有IntelliJ IDEA (推荐):

```
1. 打开IntelliJ IDEA
2. File → Open → 选择FINAL文件夹
3. 等待Maven同步完成
4. 运行 GraphicsSystem.java
```

### 如果您想安装Maven:

```powershell
# 下载: https://maven.apache.org/download.cgi
# 解压到C:\Program Files\Apache\maven
# 配置环境变量后:

mvn -version  # 验证安装
cd "项目目录"
mvn clean compile exec:java
```

---

## 项目文件说明 Project Files

- `pom.xml` - Maven配置文件（定义依赖和构建方式）
- `src/main/java/com/graphics/` - Java源代码
- `README.md` - 项目说明和使用指南
- `SETUP.md` - 本文件，安装和配置说明

## 联系支持 Support

如果遇到问题，请检查:
1. Java是否正确安装 (java -version)
2. Maven是否正确安装 (mvn -version)
3. 网络连接是否正常
4. IDE是否正确识别Maven项目

建议使用IntelliJ IDEA或Eclipse等IDE，它们内置Maven支持，可以自动处理依赖。
