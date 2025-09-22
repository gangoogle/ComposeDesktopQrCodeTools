@echo off
echo ================================
echo QR Code Generator 修复验证
echo ================================
echo.

:: 设置编码
chcp 65001 >nul

echo 1. 清理之前的构建...
if exist gradlew.bat (
    call gradlew.bat clean
) else (
    echo 错误: 未找到gradlew.bat
    pause
    exit /b 1
)
echo.

echo 2. 编译项目...
call gradlew.bat build
if %errorlevel% neq 0 (
    echo 编译失败！
    pause
    exit /b 1
)
echo.

echo 3. 创建可执行文件...
call gradlew.bat createDistributable
if %errorlevel% neq 0 (
    echo 创建可执行文件失败！
    pause
    exit /b 1
)
echo.

echo 4. 测试打包后的应用...
set "APP_DIR=build\compose\binaries\main\app\QRCodeGenerator"
set "EXE_FILE=%APP_DIR%\QRCodeGenerator.exe"

if not exist "%EXE_FILE%" (
    echo 错误: 找不到可执行文件 %EXE_FILE%
    pause
    exit /b 1
)

echo 找到可执行文件: %EXE_FILE%
echo.

echo 启动QR码生成器进行测试...
echo.
echo 测试步骤:
echo 1. 输入一些文本（例如: "Hello World"）
echo 2. 点击"生成QR码"按钮
echo 3. 检查是否成功生成QR码
echo 4. 如果仍有错误，请查看日志文件：
echo    %USERPROFILE%\.qrcode_logs\error.log
echo    %USERPROFILE%\.qrcode_logs\charset_compatibility.log
echo.

start "" "%EXE_FILE%"

echo 应用程序已启动，请进行测试。
echo 如果问题已解决，您应该能够成功生成QR码。
pause