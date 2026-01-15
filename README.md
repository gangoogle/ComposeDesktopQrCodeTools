开发目的：
用于仓库条码系统快速生成二维码来测试
# QR码生成器

这是一个使用 Kotlin 和 Compose Desktop 构建的QR码生成器应用程序。
<img width="2383" height="1558" alt="image" src="https://github.com/user-attachments/assets/fe58d801-65a5-44a9-a7a5-1b966f579291" />


## 功能特性

- ✅ 文本输入和QR码生成
- ✅ 实时QR码预览
- ✅ 历史记录管理
- ✅ 本地文件缓存
- ✅ 清空历史记录
- ✅ 点击历史记录重新生成QR码
- ✅ 支持中文字符
- ✅ 错误纠正级别设置
- ✅ **回车键快速生成QR码**

## 技术栈

- **Kotlin**: 主要开发语言
- **Compose Desktop**: UI框架
- **ZXing**: QR码生成库
- **Kotlinx Serialization**: JSON序列化
- **Kotlinx Coroutines**: 异步处理
- **Kotlinx DateTime**: 时间处理

## 如何运行

### 前提条件
- JDK 17 或更高版本（推荐JDK 21）

### 运行步骤

**方法1：使用批处理文件（推荐）**
- 双击 `run.bat` 文件即可启动应用程序

**方法2：命令行**
```bash
# Windows PowerShell
.\gradlew.bat run

# 或者不使用守护进程
.\gradlew.bat run --no-daemon
```

**方法3：使用IDE**
- 使用IntelliJ IDEA或其他支持Kotlin的IDE打开项目
- 运行Main.kt文件中的main函数

### 构建可执行文件

```bash
# Windows
.\gradlew.bat packageMsi

# macOS
./gradlew packageDmg

# Linux
./gradlew packageDeb
```

## 使用说明

1. **生成QR码**：
   - 在左侧文本框中输入或粘贴要生成QR码的文本
   - 支持中文、英文和特殊字符
   - 点击"生成QR码"按钮 **或直接按回车键快速生成**
   - QR码将显示在下方的预览区域

2. **历史记录**：
   - 每次生成的QR码文本会自动保存到右侧的历史记录
   - 点击历史记录项可以重新生成该QR码并填充到输入框
   - 相同内容的记录会更新时间戳而不重复添加
   - 历史记录会自动保存到本地文件

3. **清空历史**：
   - 点击历史记录区域右上角的清空按钮可以删除所有历史记录

4. **快捷键**：
   - 在文本输入框中按 **回车键(Enter)** 可以快速生成QR码，无需点击按钮

## 文件存储

历史记录保存在用户主目录下的 `.qrcode_history.json` 文件中。
- Windows: `C:\Users\[用户名]\.qrcode_history.json`
- macOS: `/Users/[用户名]/.qrcode_history.json`
- Linux: `/home/[用户名]/.qrcode_history.json`

## 项目结构

```
src/
└── main/
    └── kotlin/
        └── com/qrcode/generator/
            ├── Main.kt                 # 主应用程序和UI
            ├── QRCodeGenerator.kt      # QR码生成逻辑
            ├── QRCodeHistoryItem.kt    # 历史记录数据模型
            └── HistoryManager.kt       # 历史记录管理器
build.gradle.kts               # 构建配置
settings.gradle.kts            # 项目设置
gradle.properties             # Gradle属性
run.bat                       # Windows运行脚本
```

## 故障排除

1. **应用无法启动**：
   - 确保安装了JDK 17或更高版本
   - 运行 `java -version` 检查Java版本

2. **构建失败**：
   - 删除 `build` 文件夹
   - 重新运行 `.\gradlew.bat clean build`

3. **历史记录丢失**：
   - 检查用户主目录下是否存在 `.qrcode_history.json` 文件
   - 确保应用有写入权限

## 许可证

本项目仅供学习和演示使用。
