@echo off
echo ================================
echo QR Code Generator 构建工具
echo ================================
echo.

:: 设置编码
chcp 65001 >nul

:: 检查Java版本
echo 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java。请安装JDK 11或更高版本。
    pause
    exit /b 1
)
echo.

:: 清理之前的构建
echo 清理之前的构建...
if exist gradlew.bat (
    call gradlew.bat clean
) else (
    echo 错误: 未找到gradlew.bat
    pause
    exit /b 1
)
echo.

:: 编译项目
echo 编译项目...
call gradlew.bat build
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)
echo.

:: 创建可执行文件
echo 创建可执行文件...
call gradlew.bat createDistributable
if %errorlevel% neq 0 (
    echo 创建可执行文件失败！
    pause
    exit /b 1
)
echo.

:: 创建MSI安装包
echo 创建MSI安装包...
call gradlew.bat packageMsi
if %errorlevel% neq 0 (
    echo 创建MSI安装包失败！
    pause
    exit /b 1
)
echo.

echo ================================
echo 构建完成！
echo ================================
echo 可执行文件位置: build\compose\binaries\main\app\QRCodeGenerator\
echo MSI安装包位置: build\compose\binaries\main\msi\
echo.

echo 注意: 应用程序现在以窗口模式运行，不会显示控制台窗口
echo 错误日志会保存在用户主目录的 .qrcode_logs 文件夹中
echo.

pause
