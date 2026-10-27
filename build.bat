@echo off
chcp 65001 >nul
title 编译网络五子棋
cd /d "%~dp0"
echo ========================================
echo       编译网络五子棋项目
echo ========================================
echo.

if not exist "classes" mkdir classes

echo 正在编译源代码...
javac -encoding UTF-8 -d classes src\com\wuziqi\*.java src\com\wuziqi\model\*.java src\com\wuziqi\net\*.java src\com\wuziqi\ui\*.java src\com\wuziqi\util\*.java

if errorlevel 1 (
    echo.
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 编译成功！编译输出在 classes\ 目录。
echo 运行程序请双击 run.bat，或执行：java -cp classes com.wuziqi.Main
echo.
pause
