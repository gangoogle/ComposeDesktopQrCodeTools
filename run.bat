@echo off
echo 正在构建和运行QR码生成器...
echo.

:: 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java。请安装JDK 11或更高版本。
    pause
    exit /b 1
)

:: 使用Gradle Wrapper运行
if exist gradlew.bat (
    echo 使用Gradle Wrapper运行应用程序...
    echo 请等待应用程序窗口打开...
    gradlew.bat run
) else (
    echo 错误: 未找到gradlew.bat文件
    echo 请确保您在项目根目录中运行此脚本
    pause
)

echo.
echo 应用程序已关闭
pause
