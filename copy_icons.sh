#!/bin/bash

# 复制图标文件到正确位置
echo "正在复制图标文件..."

# 确保目标目录存在
mkdir -p /home/zgyi/CodeProject/Git/ComposeDesktopQrCodeTools/src/main/resources/icons

# 复制所有图标文件
cp /home/zgyi/CodeProject/Git/ComposeDesktopQrCodeTools/bin/main/icons/* /home/zgyi/CodeProject/Git/ComposeDesktopQrCodeTools/src/main/resources/icons/

echo "图标文件复制完成！"
ls -la /home/zgyi/CodeProject/Git/ComposeDesktopQrCodeTools/src/main/resources/icons/
