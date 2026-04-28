#!/bin/bash

# 构建 deb 包的脚本
echo "开始构建 QR Code Generator 的 deb 包..."

# 进入项目目录
cd /home/zgyi/CodeProject/Git/ComposeDesktopQrCodeTools

# 确保 gradlew 有执行权限
chmod +x ./gradlew

# 清理之前的构建
./gradlew clean

# 构建 deb 包
./gradlew packageDeb

if [ $? -eq 0 ]; then
    echo "deb 包构建成功！"
    echo "生成的文件位于: build/compose/binaries/main/deb/"
else
    echo "deb 包构建失败，请检查错误信息。"
    exit 1
fi
