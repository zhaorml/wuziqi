@echo off
chcp 65001 >nul
title 网络五子棋
cd /d "%~dp0"

if not exist "classes\com\wuziqi\Main.class" (
    echo 尚未编译，正在自动编译...
    call build.bat
    if not exist "classes\com\wuziqi\Main.class" (
        echo 编译失败，无法运行。
        pause
        exit /b 1
    )
)

java -cp classes com.wuziqi.Main
