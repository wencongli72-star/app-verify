#!/bin/bash

# 获取当前目录
BASE_DIR=$(cd "$(dirname "$0")"; pwd)

# jar包文件名
JAR_FILE="app-verify-1.0.0.jar"

# nohup 启动，并使用当前目录的 application.yml
nohup java -jar "$BASE_DIR/$JAR_FILE" \
  --spring.config.location="$BASE_DIR/application.yml" \
  > "$BASE_DIR/nohup.log" 2>&1 &

# 输出 PID
echo "应用已启动，PID=$(pgrep -f $JAR_FILE)"
echo "日志文件：$BASE_DIR/nohup.log"
