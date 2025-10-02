#!/bin/bash

# Protobuf 代码生成脚本
# 用于生成 Java 类文件

echo "🚀 开始生成 Protobuf Java 类文件..."

# 检查是否安装了 protoc
if ! command -v protoc &> /dev/null; then
    echo "❌ protoc 未安装，请先安装 Protocol Buffers"
    echo "   下载地址: https://github.com/protocolbuffers/protobuf/releases"
    exit 1
fi

# 设置路径
PROTO_DIR="core/src/main/proto"
JAVA_OUT_DIR="core/src/main/java"
PROTO_FILE="network_message.proto"

# 检查 proto 文件是否存在
if [ ! -f "$PROTO_DIR/$PROTO_FILE" ]; then
    echo "❌ Proto 文件不存在: $PROTO_DIR/$PROTO_FILE"
    exit 1
fi

# 创建输出目录
mkdir -p "$JAVA_OUT_DIR"

# 生成 Java 类文件
echo "📦 生成 Java 类文件..."
protoc --java_out="$JAVA_OUT_DIR" --proto_path="$PROTO_DIR" "$PROTO_DIR/$PROTO_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Java 类文件生成成功"
    echo "   输出目录: $JAVA_OUT_DIR"
    
    # 显示生成的文件
    echo "📁 生成的文件:"
    find "$JAVA_OUT_DIR" -name "*Protos.java" -type f | while read file; do
        echo "   - $file"
    done
else
    echo "❌ Java 类文件生成失败"
    exit 1
fi

# 使用 Maven 编译
echo "🔨 使用 Maven 编译项目..."
cd core
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Maven 编译成功"
else
    echo "❌ Maven 编译失败"
    exit 1
fi

echo "🎉 Protobuf 代码生成完成！"
