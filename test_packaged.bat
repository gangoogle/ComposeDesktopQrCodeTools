@echo off
echo ================================
echo QR Code Generator 测试工具
echo ================================
echo.

:: 设置编码
chcp 65001 >nul

set "APP_DIR=build\compose\binaries\main\app\QRCodeGenerator"
set "EXE_FILE=%APP_DIR%\QRCodeGenerator.exe"

if not exist "%EXE_FILE%" (
    echo 错误: 找不到可执行文件 %EXE_FILE%
    echo 请先运行 build_tools.bat 进行构建
    pause
    exit /b 1
)

echo 找到可执行文件: %EXE_FILE%
echo.

echo 启动QR码生成器...
echo 注意: 应用程序现在以窗口模式运行，不会显示控制台窗口
echo 如果程序出现错误，请检查以下位置的日志文件：
echo %USERPROFILE%\.qrcode_logs\error.log
echo %USERPROFILE%\.qrcode_logs\history_error.log
echo %USERPROFILE%\.qrcode_logs\charset_compatibility.log
echo.

start "" "%EXE_FILE%"

echo 应用程序已启动（无控制台窗口）。
echo 如果遇到问题，请查看日志文件获取详细错误信息。
pause