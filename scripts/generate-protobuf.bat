@echo off
REM Protobuf 代码生成脚本 (Windows)
REM 用于生成 Java 类文件

echo 🚀 开始生成 Protobuf Java 类文件...

REM 检查是否安装了 protoc
where protoc >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ protoc 未安装，请先安装 Protocol Buffers
    echo    下载地址: https://github.com/protocolbuffers/protobuf/releases
    pause
    exit /b 1
)

REM 设置路径
set PROTO_DIR=core\src\main\proto
set JAVA_OUT_DIR=core\src\main\java
set PROTO_FILE=network_message.proto

REM 检查 proto 文件是否存在
if not exist "%PROTO_DIR%\%PROTO_FILE%" (
    echo ❌ Proto 文件不存在: %PROTO_DIR%\%PROTO_FILE%
    pause
    exit /b 1
)

REM 创建输出目录
if not exist "%JAVA_OUT_DIR%" mkdir "%JAVA_OUT_DIR%"

REM 生成 Java 类文件
echo 📦 生成 Java 类文件...
protoc --java_out="%JAVA_OUT_DIR%" --proto_path="%PROTO_DIR%" "%PROTO_DIR%\%PROTO_FILE%"

if %errorlevel% equ 0 (
    echo ✅ Java 类文件生成成功
    echo    输出目录: %JAVA_OUT_DIR%
    
    REM 显示生成的文件
    echo 📁 生成的文件:
    for /r "%JAVA_OUT_DIR%" %%f in (*Protos.java) do (
        echo    - %%f
    )
) else (
    echo ❌ Java 类文件生成失败
    pause
    exit /b 1
)

REM 使用 Maven 编译
echo 🔨 使用 Maven 编译项目...
cd core
mvn clean compile

if %errorlevel% equ 0 (
    echo ✅ Maven 编译成功
) else (
    echo ❌ Maven 编译失败
    pause
    exit /b 1
)

echo 🎉 Protobuf 代码生成完成！
pause
